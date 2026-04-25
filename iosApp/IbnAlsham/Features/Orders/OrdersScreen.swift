import Combine
import shared
import SwiftUI

class OrdersViewModelBridge: ViewModel<OrdersViewModel> {
    @Published private(set) var uiState = OrdersViewModelContractMyUiState.companion.defaultInstance()
    @Published var shouldNavigateToOrderDetails = false
    @Published var selectedOrderId: Int32 = 0

    override init(model: OrdersViewModel = KoinHelper().provideOrdersViewModel) {
        super.init(model: model)
        uiState = model.currentState
        setupObservers()
    }

    private func setupObservers() {
        model.watchState(scope: model.viewModelScope) { [weak self] state in
            self?.uiState = state
        }.asCancellable().store(in: &subscriptions)

        model.watchEffect(scope: model.viewModelScope) { [weak self] effect in
            if let navigate = effect as? OrdersViewModelContractSideEffectNavigateToOrderDetails {
                self?.shouldNavigateToOrderDetails = true
                self?.selectedOrderId = navigate.orderId
            }
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: OrdersViewModelContractAction) {
        model.onAction(event: action)
    }
}

struct OrdersScreen: View {
    @StateObject private var viewModel = OrdersViewModelBridge()

    var body: some View {
        ZStack {
            Color(hex: "FAFAFA").edgesIgnoringSafeArea(.all)

            VStack(spacing: 0) {
                // Header
                HStack {
                    Text("طلباتي")
                        .font(.system(size: 20, weight: .bold))
                    Spacer()
                }
                .padding()
                .background(Color.white)
                
                // Tabs
                HStack(spacing: 0) {
                    TabButton(
                        title: "الطلبات الحالية", 
                        isSelected: viewModel.uiState.selectedTab == .current
                    ) { viewModel.dispatch(action: OrdersViewModelContractActionSelectTab(tab: .current)) }
                    
                    TabButton(
                        title: "الطلبات السابقة", 
                        isSelected: viewModel.uiState.selectedTab == .previous
                    ) { viewModel.dispatch(action: OrdersViewModelContractActionSelectTab(tab: .previous)) }
                }
                .background(Color.white)
                .shadow(color: Color.black.opacity(0.05), radius: 2, y: 2)

                if viewModel.uiState.isLoading {
                    Spacer()
                    ProgressView().progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "C62828")))
                    Spacer()
                } else if viewModel.uiState.displayedOrders.isEmpty {
                    Spacer()
                    Text("لا توجد طلبات هنا")
                        .foregroundColor(.gray)
                        .font(.system(size: 16))
                    Spacer()
                } else {
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            ForEach(viewModel.uiState.displayedOrders, id: \.id) { order in
                                OrderItemCard(order: order) {
                                    viewModel.dispatch(action: OrdersViewModelContractActionOrderClicked(orderId: order.id))
                                }
                            }
                        }
                        .padding(16)
                    }
                }
            }
        }
        .environment(\.layoutDirection, .rightToLeft)
        .onAppear {
            viewModel.dispatch(action: OrdersViewModelContractActionViewReady())
        }
    }
}

struct TabButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            VStack(spacing: 12) {
                Text(title)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(isSelected ? Color(hex: "C62828") : .gray)
                
                Rectangle()
                    .fill(isSelected ? Color(hex: "C62828") : Color.clear)
                    .frame(height: 3)
            }
            .padding(.top, 16)
        }
        .frame(maxWidth: .infinity)
    }
}

struct OrderItemCard: View {
    let order: OrderSummaryDomain
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            VStack(spacing: 12) {
                HStack {
                    Text(order.orderNumber)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.black)
                    
                    Spacer()
                    
                    let isCancelled = order.statusLabel.contains("ملغ")
                    Text(order.statusLabel)
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(isCancelled ? Color(hex: "C62828") : Color(hex: "F57F17"))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(isCancelled ? Color(hex: "FFFFEBEE") : Color(hex: "FFF8E1"))
                        .cornerRadius(4)
                }
                
                HStack {
                    Text("التاريخ: \(order.date)")
                        .font(.system(size: 14))
                        .foregroundColor(.gray)
                    Spacer()
                    Text("\(order.itemCount) عناصر")
                        .font(.system(size: 14))
                        .foregroundColor(.gray)
                }
                
                Divider()
                
                HStack {
                    Text("المجموع:")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(.black)
                    Spacer()
                    Text(String(format: "%.3f JOD", order.total))
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(Color(hex: "C62828"))
                }
            }
            .padding(16)
            .background(Color.white)
            .cornerRadius(12)
            .shadow(color: Color.black.opacity(0.05), radius: 2)
        }
    }
}
