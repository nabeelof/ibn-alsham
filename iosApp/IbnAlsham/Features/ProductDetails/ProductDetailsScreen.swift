import Combine
import shared
import SwiftUI

class ProductDetailsViewModelBridge: ViewModel<ProductDetailsViewModel> {
    @Published private(set) var uiState = ProductDetailsViewModelContractMyUiState.companion.defaultInstance()
    @Published var shouldNavigateBack = false
    @Published var snackbarMessage = ""
    @Published var showSnackbar = false

    override init(model: ProductDetailsViewModel = KoinHelper().provideProductDetailsViewModel) {
        super.init(model: model)
        uiState = model.currentState
        setupObservers()
    }

    private func setupObservers() {
        model.watchState(scope: model.viewModelScope) { [weak self] state in
            self?.uiState = state
        }.asCancellable().store(in: &subscriptions)

        model.watchEffect(scope: model.viewModelScope) { [weak self] effect in
            if effect is ProductDetailsViewModelContractSideEffectNavigateBack {
                self?.shouldNavigateBack = true
            }
            if let snack = effect as? ProductDetailsViewModelContractSideEffectShowSnackbar {
                self?.snackbarMessage = snack.message
                self?.showSnackbar = true
            }
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: ProductDetailsViewModelContractAction) {
        model.onAction(event: action)
    }
}

struct ProductDetailsScreen: View {
    let productId: Int32
    @StateObject private var viewModel = ProductDetailsViewModelBridge()

    var body: some View {
        ZStack {
            Color(hex: "FAFAFA").edgesIgnoringSafeArea(.all)
            
            if viewModel.uiState.isLoading {
                ProgressView().progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "C62828")))
            } else {
                VStack(spacing: 0) {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 0) {
                            // Header Image
                            Rectangle()
                                .fill(Color.gray.opacity(0.3))
                                .frame(height: 250)
                            
                            // Info
                            VStack(alignment: .leading, spacing: 12) {
                                HStack(alignment: .top) {
                                    Text(viewModel.uiState.name)
                                        .font(.system(size: 22, weight: .bold))
                                        .foregroundColor(.black)
                                    Spacer()
                                    VStack(alignment: .trailing, spacing: 2) {
                                        Text(JodPrice.format(viewModel.uiState.effectivePrice))
                                            .font(.system(size: 20, weight: .bold))
                                            .foregroundColor(Color(hex: "C62828"))
                                        
                                        if viewModel.uiState.basePrice > viewModel.uiState.effectivePrice {
                                            Text(JodPrice.format(viewModel.uiState.basePrice))
                                                .font(.system(size: 14))
                                                .foregroundColor(.gray)
                                                .strikethrough()
                                        }
                                    }
                                }
                                
                                Text(viewModel.uiState.description_) // note: `description` is reserved in Swift, KMM maps it to `description_`
                                    .font(.system(size: 15))
                                    .foregroundColor(.gray)
                                    .lineSpacing(4)
                            }
                            .padding(16)
                            
                            Rectangle().fill(Color(hex: "EEEEEE")).frame(height: 8)
                            
                            // Add-on groups: radio when a single choice is allowed,
                            // checkboxes otherwise, with the admin's rule spelled out.
                            if !viewModel.uiState.optionGroups.isEmpty {
                                VStack(alignment: .leading, spacing: 16) {
                                    Text("خيارات إضافية")
                                        .font(.system(size: 18, weight: .bold))

                                    ForEach(viewModel.uiState.optionGroups, id: \.id) { group in
                                        OptionGroupSection(
                                            group: group,
                                            selectedIds: viewModel.uiState.selectedOptionIds,
                                            isUnsatisfied: viewModel.uiState.unsatisfiedGroupIds
                                                .contains(KotlinInt(int: group.id))
                                        ) { optionId in
                                            viewModel.dispatch(
                                                action: ProductDetailsViewModelContractActionToggleOption(
                                                    groupId: group.id,
                                                    optionId: optionId
                                                )
                                            )
                                        }
                                    }
                                }
                                .padding(16)
                            }
                        }
                    }
                    
                    // Bottom Bar
                    HStack(spacing: 16) {
                        // Quantity
                        HStack(spacing: 16) {
                            Button(action: { viewModel.dispatch(action: ProductDetailsViewModelContractActionDecrementQuantity()) }) {
                                Text("-")
                                    .font(.system(size: 20, weight: .bold))
                                    .frame(width: 32, height: 32)
                                    .background(Color.white)
                                    .clipShape(Circle())
                                    .foregroundColor(.black)
                            }
                            
                            Text("\(viewModel.uiState.quantity)")
                                .font(.system(size: 18, weight: .bold))
                            
                            Button(action: { viewModel.dispatch(action: ProductDetailsViewModelContractActionIncrementQuantity(maxAllowed: 10)) }) {
                                Image(systemName: "plus")
                                    .font(.system(size: 14, weight: .bold))
                                    .frame(width: 32, height: 32)
                                    .background(Color.white)
                                    .clipShape(Circle())
                                    .foregroundColor(.black)
                            }
                        }
                        .padding(8)
                        .background(Color(hex: "FAFAFA"))
                        .cornerRadius(24)
                        
                        Button(action: {
                            viewModel.dispatch(action: ProductDetailsViewModelContractActionAddToCart())
                        }) {
                            HStack {
                                if viewModel.uiState.isAddingToCart {
                                    ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                                } else {
                                    Text("إضافة • " + JodPrice.format(viewModel.uiState.calculatedTotal))
                                        .font(.system(size: 16, weight: .bold))
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .background(viewModel.uiState.isAddToCartEnabled ? Color(hex: "C62828") : Color.gray)
                            .foregroundColor(.white)
                            .cornerRadius(12)
                        }
                        .disabled(!viewModel.uiState.isAddToCartEnabled)
                    }
                    .padding(16)
                    .background(Color.white)
                    .shadow(color: Color.black.opacity(0.1), radius: 4, y: -2)
                }
            }
        }
        .environment(\.layoutDirection, .rightToLeft)
        .onAppear {
            viewModel.dispatch(action: ProductDetailsViewModelContractActionViewReady(productId: productId))
        }
    }
}


/// One add-on group rendered with the right control and its selection rule.
private struct OptionGroupSection: View {
    let group: OptionGroupDomain
    let selectedIds: Set<KotlinInt>
    let isUnsatisfied: Bool
    let onToggle: (Int32) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 8) {
                Text(group.name).font(.system(size: 16, weight: .semibold))
                Text(group.isRequired ? "إلزامي" : "اختياري")
                    .font(.system(size: 11, weight: .semibold))
                    .padding(.horizontal, 6).padding(.vertical, 2)
                    .background((group.isRequired ? Color(hex: "C62828") : Color.gray).opacity(0.12))
                    .foregroundColor(group.isRequired ? Color(hex: "C62828") : .gray)
                    .cornerRadius(6)
            }

            if let rule = ruleText {
                Text(rule)
                    .font(.system(size: 12))
                    .foregroundColor(isUnsatisfied ? Color(hex: "C62828") : .gray)
            }

            ForEach(group.options, id: \.id) { option in
                Button {
                    onToggle(option.id)
                } label: {
                    HStack {
                        Image(systemName: iconName(for: option))
                            .foregroundColor(
                                selectedIds.contains(KotlinInt(int: option.id)) ? Color(hex: "C62828") : .gray
                            )
                            .font(.system(size: 20))

                        Text(option.name).foregroundColor(.black).font(.system(size: 16))

                        Spacer()

                        Text(option.isFree ? "مجاناً" : JodPrice.formatSigned(option.price))
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(option.isFree ? Color(hex: "2E7D32") : Color(hex: "C62828"))
                    }
                    .padding(.vertical, 8)
                }
                .disabled(!option.isUserChangeable)
            }
        }
    }

    private var ruleText: String? {
        if group.isSingleChoice { return "اختر واحداً" }
        if let max = group.maxSelections { return "اختر حتى \(max)" }
        if group.minSelections > 0 { return "اختر \(group.minSelections) على الأقل" }
        return nil
    }

    private func iconName(for option: OptionDomain) -> String {
        let selected = selectedIds.contains(KotlinInt(int: option.id))
        if group.isSingleChoice {
            return selected ? "largecircle.fill.circle" : "circle"
        }
        return selected ? "checkmark.square.fill" : "square"
    }
}
