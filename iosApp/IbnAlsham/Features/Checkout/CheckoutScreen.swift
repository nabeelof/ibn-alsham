import Combine
import shared
import SwiftUI

class CheckoutViewModelBridge: ViewModel<CheckoutViewModel> {
    @Published private(set) var uiState = CheckoutViewModelContractMyUiState.companion.defaultInstance()
    @Published var shouldNavigateToOrder = false
    @Published var createdOrderId: Int32 = 0
    @Published var snackbarMessage = ""
    @Published var showSnackbar = false

    override init(model: CheckoutViewModel = KoinHelper().provideCheckoutViewModel) {
        super.init(model: model)
        uiState = model.currentState
        setupObservers()
    }

    private func setupObservers() {
        model.watchState(scope: model.viewModelScope) { [weak self] state in
            self?.uiState = state
        }.asCancellable().store(in: &subscriptions)

        model.watchEffect(scope: model.viewModelScope) { [weak self] effect in
            if let navigate = effect as? CheckoutViewModelContractSideEffectNavigateToOrderDetails {
                self?.shouldNavigateToOrder = true
                self?.createdOrderId = navigate.orderId
            }
            if let snack = effect as? CheckoutViewModelContractSideEffectShowSnackbar {
                self?.snackbarMessage = snack.message
                self?.showSnackbar = true
            }
            if effect is CheckoutViewModelContractSideEffectRequestLocationPermission {
                // Simulate acquiring location iOS
                self?.dispatch(action: CheckoutViewModelContractActionSetLocation(lat: 31.9, lng: 35.9))
            }
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: CheckoutViewModelContractAction) {
        model.onAction(event: action)
    }
}

struct CheckoutScreen: View {
    @StateObject private var viewModel = CheckoutViewModelBridge()
    @State private var addressText: String = ""

    var body: some View {
        ZStack {
            Color(hex: "FAFAFA").edgesIgnoringSafeArea(.all)
            
            VStack(spacing: 0) {
                ScrollView {
                    VStack(spacing: 24) {
                        // Order Type Toggle
                        HStack(spacing: 0) {
                            OrderTypeSegment(
                                text: "استلام من الفرع",
                                isSelected: viewModel.uiState.orderType == .pickup
                            ) { viewModel.dispatch(action: CheckoutViewModelContractActionSelectOrderType(type: .pickup)) }
                            
                            OrderTypeSegment(
                                text: "التوصيل للمنزل",
                                isSelected: viewModel.uiState.orderType == .delivery
                            ) { viewModel.dispatch(action: CheckoutViewModelContractActionSelectOrderType(type: .delivery)) }
                        }
                        .padding(4)
                        .background(Color.white)
                        .cornerRadius(12)
                        .shadow(color: Color.black.opacity(0.05), radius: 2)
                        
                        // Delivery Details
                        if viewModel.uiState.orderType == .delivery {
                            VStack(alignment: .leading, spacing: 16) {
                                Text("موقع التوصيل")
                                    .font(.system(size: 16, weight: .bold))
                                
                                TextField("معلومات العنوان (بناية، شقة)", text: $addressText)
                                    .padding()
                                    .background(Color(hex: "FAFAFA"))
                                    .cornerRadius(8)
                                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.gray.opacity(0.3), lineWidth: 1))
                                    .onChange(of: addressText) { newValue in
                                        viewModel.dispatch(action: CheckoutViewModelContractActionUpdateAddress(address: newValue))
                                    }
                                
                                if !viewModel.uiState.isDeliveryRangeValid {
                                    Text("عذرا، موقعك خارج نطاق التوصيل")
                                        .foregroundColor(Color(hex: "C62828"))
                                        .font(.system(size: 14))
                                } else if viewModel.uiState.isLoading {
                                    ProgressView()
                                        .frame(maxWidth: .infinity, alignment: .center)
                                } else if viewModel.uiState.selectedLat != nil {
                                    Text(String(format: "تكلفة التوصيل: %.3f JOD", viewModel.uiState.deliveryFee))
                                        .foregroundColor(.gray)
                                        .font(.system(size: 14))
                                }
                            }
                            .padding()
                            .background(Color.white)
                            .cornerRadius(12)
                            .shadow(color: Color.black.opacity(0.05), radius: 2)
                        }
                        
                        // Payment Summary
                        VStack(alignment: .leading, spacing: 12) {
                            Text("الفاتورة")
                                .font(.system(size: 16, weight: .bold))
                            
                            HStack {
                                Text("قيمة الطلب:")
                                    .foregroundColor(.gray)
                                Spacer()
                                Text(String(format: "%.3f JOD", viewModel.uiState.subtotal))
                                    .fontWeight(.bold)
                            }
                            
                            if viewModel.uiState.orderType == .delivery {
                                HStack {
                                    Text("رسوم التوصيل:")
                                        .foregroundColor(.gray)
                                    Spacer()
                                    Text(String(format: "%.3f JOD", viewModel.uiState.deliveryFee))
                                        .fontWeight(.bold)
                                }
                            }
                            
                            Divider()
                            
                            HStack {
                                Text("الإجمالي المطلوب:")
                                    .fontWeight(.bold)
                                    .foregroundColor(Color(hex: "C62828"))
                                Spacer()
                                Text(String(format: "%.3f JOD", viewModel.uiState.total))
                                    .fontWeight(.bold)
                                    .foregroundColor(Color(hex: "C62828"))
                            }
                        }
                        .padding()
                        .background(Color.white)
                        .cornerRadius(12)
                        .shadow(color: Color.black.opacity(0.05), radius: 2)
                    }
                    .padding(16)
                }
                
                // Bottom Button
                VStack {
                    Button(action: {
                        viewModel.dispatch(action: CheckoutViewModelContractActionPlaceOrder())
                    }) {
                        HStack {
                            if viewModel.uiState.isPlacingOrder {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Text(String(format: "تأكيد الطلب بمبلغ %.3f JOD", viewModel.uiState.total))
                                    .font(.system(size: 16, weight: .bold))
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(
                            (viewModel.uiState.isLoading || viewModel.uiState.isPlacingOrder) 
                            ? Color.gray : Color(hex: "C62828")
                        )
                        .foregroundColor(.white)
                        .cornerRadius(12)
                    }
                    .disabled(viewModel.uiState.isLoading || viewModel.uiState.isPlacingOrder)
                }
                .padding()
                .background(Color.white)
                .shadow(radius: 8)
            }
        }
        .environment(\.layoutDirection, .rightToLeft)
        .onAppear {
            viewModel.dispatch(action: CheckoutViewModelContractActionViewReady())
        }
    }
}

struct OrderTypeSegment: View {
    let text: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(text)
                .font(.system(size: 15, weight: .bold))
                .foregroundColor(isSelected ? .black : .gray)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(isSelected ? Color(hex: "FFD54F") : Color.clear)
                .cornerRadius(8)
        }
    }
}
