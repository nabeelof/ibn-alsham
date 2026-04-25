import Combine
import shared
import SwiftUI

class CartViewModelBridge: ViewModel<CartViewModel> {
    @Published private(set) var uiState = CartViewModelContractMyUiState.companion.defaultInstance()
    @Published var shouldNavigateToCheckout = false
    @Published var shouldNavigateToMenu = false

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
                                    onRemove: { viewModel.dispatch(action: CartViewModelContractActionRemoveItem(itemId: item.id)) }
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
                            Text(String(format: "%.3f JOD", viewModel.uiState.subtotal))
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
        .onAppear {
            viewModel.dispatch(action: CartViewModelContractActionViewReady())
        }
    }
}

struct CartItemView: View {
    let item: CartItemDomain
    let onIncrease: () -> Void
    let onDecrease: () -> Void
    let onRemove: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Rectangle()
                .fill(Color.gray.opacity(0.2))
                .frame(width: 60, height: 60)
                .cornerRadius(8)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(item.name)
                    .font(.system(size: 15, weight: .bold))
                
                Text(String(format: "%.3f JOD", item.price))
                    .font(.system(size: 13))
                    .foregroundColor(.gray)
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
