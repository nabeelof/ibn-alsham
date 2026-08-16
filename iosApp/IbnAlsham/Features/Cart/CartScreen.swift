import Combine
import shared
import SwiftUI

class CartViewModelBridge: ViewModel<CartViewModel> {
    @Published private(set) var uiState = CartViewModelContractMyUiState.companion.defaultInstance()
    @Published var shouldNavigateToCheckout = false
    @Published var shouldNavigateToMenu = false
    @Published var snackbarMessage: String?

    override init(model: CartViewModel = KoinHelper().provideCartViewModel) {
        super.init(model: model)
        uiState = model.currentState
        setupObservers()
    }

    private func setupObservers() {
        model.watchState(scope: model.viewModelScope) { [weak self] state in
            self?.uiState = state
        }.asCancellable().store(in: &subscriptions)

        model.watchEffect(scope: model.viewModelScope) { [weak self] effect in
            if effect is CartViewModelContractSideEffectNavigateToCheckout {
                self?.shouldNavigateToCheckout = true
            }
            if effect is CartViewModelContractSideEffectNavigateToMenu {
                self?.shouldNavigateToMenu = true
            }
            if let snackbar = effect as? CartViewModelContractSideEffectShowSnackbar {
                self?.snackbarMessage = snackbar.message
            }
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: CartViewModelContractAction) {
        model.onAction(event: action)
    }
}

// SwiftUI View
struct CartScreen: View {
    @StateObject private var viewModel = CartViewModelBridge()

    var body: some View {
        ZStack {
            Color(hex: "FAFAFA").edgesIgnoringSafeArea(.all)

            VStack(spacing: 0) {
                // Shared App Bar would go here in actual navigation wrapper
                
                if viewModel.uiState.items.isEmpty {
                    Spacer()
                    VStack {
                        Text("السلة فارغة")
                            .font(.system(size: 20, weight: .bold))
                        
                        Button(action: { viewModel.dispatch(action: CartViewModelContractActionContinueShopping()) }) {
                            Text("تصفح المنيو")
                                .padding()
                                .background(Color(hex: "C62828"))
                                .foregroundColor(.white)
                                .cornerRadius(8)
                        }
                        .padding(.top, 16)
                    }
                    Spacer()
                } else {
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            ForEach(viewModel.uiState.items, id: \.id) { item in
                                CartItemView(
                                    item: item,
                                    onIncrease: { viewModel.dispatch(action: CartViewModelContractActionIncreaseQuantity(itemId: item.id)) },
                                    onDecrease: { viewModel.dispatch(action: CartViewModelContractActionDecreaseQuantity(itemId: item.id)) },
                                    onRemove: { viewModel.dispatch(action: CartViewModelContractActionRemoveItem(itemId: item.id)) },
                                    onEditAddons: { viewModel.dispatch(action: CartViewModelContractActionStartEditingOptions(itemId: item.id)) }
                                )
                            }
                        }
                        .padding(16)
                    }
                    
                    // Bottom Summary
                    VStack(spacing: 16) {
                        HStack {
                            Text("المجموع")
                                .font(.system(size: 16, weight: .bold))
                            Spacer()
                            Text(JodPrice.format(viewModel.uiState.subtotal))
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(Color(hex: "C62828"))
                        }
                        
                        Button(action: { viewModel.dispatch(action: CartViewModelContractActionProceedToCheckout()) }) {
                            Text("متابعة لطلب الدفع")
                                .font(.system(size: 16, weight: .bold))
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color(hex: "FFD54F"))
                                .foregroundColor(.black)
                                .cornerRadius(12)
                        }
                    }
                    .padding()
                    .background(Color.white)
                    .shadow(radius: 8)
                }
            }
        }
        .environment(\.layoutDirection, .rightToLeft)
        .sheet(isPresented: .constant(viewModel.uiState.editingItem != nil)) {
            EditAddonsSheet(state: viewModel.uiState) { viewModel.dispatch(action: $0) }
        }
        .onAppear {
            viewModel.dispatch(action: CartViewModelContractActionViewReady())
        }
    }
}

/// Sheet for changing the add-ons on an existing cart line.
private struct EditAddonsSheet: View {
    let state: CartViewModelContractMyUiState
    let dispatch: (CartViewModelContractAction) -> Void

    var body: some View {
        guard let item = state.editingItem else { return AnyView(EmptyView()) }

        return AnyView(
            VStack(alignment: .leading, spacing: 12) {
                Text(item.name).font(.system(size: 18, weight: .bold))
                Text("الإضافات").font(.system(size: 13)).foregroundColor(.gray)

                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        ForEach(item.optionGroups, id: \.id) { group in
                            AddonGroupView(group: group, selectedIds: state.editingSelectedOptionIds) { optionId in
                                dispatch(
                                    CartViewModelContractActionToggleEditedOption(
                                        groupId: group.id,
                                        optionId: optionId
                                    )
                                )
                            }
                        }
                    }
                }

                HStack {
                    Text("المجموع الفرعي").foregroundColor(.gray)
                    Spacer()
                    Text(JodPrice.format(state.editingUnitTotal * Double(item.quantity)))
                        .font(.system(size: 18, weight: .bold))
                }

                HStack(spacing: 12) {
                    Button("إلغاء") { dispatch(CartViewModelContractActionCancelEditingOptions()) }
                        .frame(maxWidth: .infinity).padding()
                        .background(Color(hex: "F0F0F0")).cornerRadius(26)

                    Button("تأكيد") { dispatch(CartViewModelContractActionConfirmEditedOptions()) }
                        .frame(maxWidth: .infinity).padding()
                        .background(state.isEditingSelectionValid ? Color(hex: "C62828") : Color.gray.opacity(0.4))
                        .foregroundColor(.white).cornerRadius(26)
                        .disabled(!state.isEditingSelectionValid)
                }
            }
            .padding(20)
            .environment(\.layoutDirection, .rightToLeft)
        )
    }
}

private struct AddonGroupView: View {
    let group: OptionGroupDomain
    let selectedIds: Set<KotlinInt>
    let onToggle: (Int32) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(group.name).font(.system(size: 15, weight: .semibold))
                Text(group.isRequired ? "إلزامي" : "اختياري")
                    .font(.system(size: 11))
                    .foregroundColor(group.isRequired ? Color(hex: "C62828") : .gray)
            }

            ForEach(group.options, id: \.id) { option in
                Button {
                    onToggle(option.id)
                } label: {
                    HStack {
                        Image(systemName: iconName(for: option))
                            .foregroundColor(Color(hex: "C62828"))
                        Text(option.name).font(.system(size: 15)).foregroundColor(.primary)
                        Spacer()
                        Text(option.isFree ? "مجاناً" : JodPrice.formatSigned(option.price))
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(option.isFree ? Color(hex: "2E7D32") : Color(hex: "C62828"))
                    }
                }
                .disabled(!option.isUserChangeable)
            }
        }
    }

    private func iconName(for option: OptionDomain) -> String {
        let selected = selectedIds.contains(KotlinInt(int: option.id))
        if group.isSingleChoice {
            return selected ? "largecircle.fill.circle" : "circle"
        }
        return selected ? "checkmark.square.fill" : "square"
    }
}

struct CartItemView: View {
    let item: CartItemDomain
    let onIncrease: () -> Void
    let onDecrease: () -> Void
    let onRemove: () -> Void
    let onEditAddons: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Rectangle()
                .fill(Color.gray.opacity(0.2))
                .frame(width: 60, height: 60)
                .cornerRadius(8)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(item.name)
                    .font(.system(size: 15, weight: .bold))

                if !item.selectedOptions.isEmpty {
                    Text(item.optionsLabel)
                        .font(.system(size: 12))
                        .foregroundColor(.gray)
                        .lineLimit(2)
                }

                HStack(spacing: 8) {
                    // Unit total already folds in the selected add-ons.
                    Text(JodPrice.format(item.unitTotal))
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(Color(hex: "C62828"))

                    if item.canEditOptions {
                        Button("تعديل الإضافات", action: onEditAddons)
                            .font(.system(size: 12))
                            .foregroundColor(Color(hex: "C62828"))
                    }
                }
            }
            
            Spacer()
            
            // Quantity Selector
            HStack {
                Button(action: onDecrease) {
                    Image(systemName: "minus")
                        .frame(width: 28, height: 28)
                        .background(Color.white)
                        .clipShape(Circle())
                        .foregroundColor(.black)
                }
                
                Text("\(item.quantity)")
                    .font(.system(size: 14, weight: .bold))
                    .padding(.horizontal, 8)
                
                Button(action: onIncrease) {
                    Image(systemName: "plus")
                        .frame(width: 28, height: 28)
                        .background(Color.white)
                        .clipShape(Circle())
                        .foregroundColor(.black)
                }
            }
            .padding(4)
            .background(Color(hex: "FAFAFA"))
            .cornerRadius(24)
        }
        .padding(12)
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.05), radius: 2)
    }
}
