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
                                        Text(String(format: "%.3f JOD", viewModel.uiState.effectivePrice))
                                            .font(.system(size: 20, weight: .bold))
                                            .foregroundColor(Color(hex: "C62828"))
                                        
                                        if viewModel.uiState.basePrice > viewModel.uiState.effectivePrice {
                                            Text(String(format: "%.3f JOD", viewModel.uiState.basePrice))
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
                            
                            // Options
                            if !viewModel.uiState.options.isEmpty {
                                VStack(alignment: .leading, spacing: 16) {
                                    Text("خيارات إضافية")
                                        .font(.system(size: 18, weight: .bold))
                                    
                                    ForEach(viewModel.uiState.options, id: \.id) { option in
                                        Button(action: {
                                            viewModel.dispatch(action: ProductDetailsViewModelContractActionToggleOption(optionId: option.id))
                                        }) {
                                            HStack {
                                                Image(systemName: viewModel.uiState.selectedOptionIds.contains(option.id as NSNumber) ? "checkmark.square.fill" : "square")
                                                    .foregroundColor(viewModel.uiState.selectedOptionIds.contains(option.id as NSNumber) ? Color(hex: "C62828") : .gray)
                                                    .font(.system(size: 20))
                                                
                                                Text(option.name)
                                                    .foregroundColor(.black)
                                                    .font(.system(size: 16))
                                                
                                                Spacer()
                                                
                                                if option.price > 0 {
                                                    Text(String(format: "+%.3f JOD", option.price))
                                                        .foregroundColor(Color(hex: "C62828"))
                                                        .font(.system(size: 14, weight: .semibold))
                                                } else if option.isRequired {
                                                    Text("إلزامي")
                                                        .foregroundColor(.gray)
                                                        .font(.system(size: 12))
                                                }
                                            }
                                            .padding(.vertical, 8)
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
                                    Text(String(format: "إضافة • %.3f JOD", viewModel.uiState.calculatedTotal))
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
