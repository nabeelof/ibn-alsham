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
                                    Text("تكلفة التوصيل: " + JodPrice.format(viewModel.uiState.deliveryFee))
                                        .foregroundColor(.gray)
                                        .font(.system(size: 14))
                                }
                            }
                            .padding()
                            .background(Color.white)
                            .cornerRadius(12)
                            .shadow(color: Color.black.opacity(0.05), radius: 2)
                        }
                        
                        // Coupon
                        CouponSection(state: viewModel.uiState) { viewModel.dispatch(action: $0) }

                        // Payment method (Jordan)
                        PaymentSection(state: viewModel.uiState) { viewModel.dispatch(action: $0) }

                        // Payment Summary
                        VStack(alignment: .leading, spacing: 12) {
                            Text("الفاتورة")
                                .font(.system(size: 16, weight: .bold))

                            HStack {
                                Text("قيمة الطلب:")
                                    .foregroundColor(.gray)
                                Spacer()
                                Text(JodPrice.format(viewModel.uiState.subtotal))
                                    .fontWeight(.bold)
                            }

                            if viewModel.uiState.discountAmount > 0 {
                                HStack {
                                    Text("الخصم:")
                                        .foregroundColor(.gray)
                                    Spacer()
                                    Text(JodPrice.format(-viewModel.uiState.discountAmount))
                                        .fontWeight(.bold)
                                        .foregroundColor(Color(hex: "2E7D32"))
                                }
                            }

                            if viewModel.uiState.orderType == .delivery {
                                HStack {
                                    Text("رسوم التوصيل:")
                                        .foregroundColor(.gray)
                                    Spacer()
                                    Text(JodPrice.format(viewModel.uiState.effectiveDeliveryFee))
                                        .fontWeight(.bold)
                                }
                            }

                            Divider()

                            HStack {
                                Text("الإجمالي المطلوب:")
                                    .fontWeight(.bold)
                                    .foregroundColor(Color(hex: "C62828"))
                                Spacer()
                                Text(JodPrice.format(viewModel.uiState.total))
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
                                Text("تأكيد الطلب بمبلغ " + JodPrice.format(viewModel.uiState.total))
                                    .font(.system(size: 16, weight: .bold))
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(
                            viewModel.uiState.canPlaceOrder ? Color(hex: "C62828") : Color.gray
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


/// Coupon entry with inline validation plus the eligible-coupon list.
private struct CouponSection: View {
    let state: CheckoutViewModelContractMyUiState
    let dispatch: (CheckoutViewModelContractAction) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("كوبون الخصم").font(.system(size: 16, weight: .bold))

            if let applied = state.appliedCoupon {
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("تم تطبيق الكوبون \(applied.code)")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(Color(hex: "2E7D32"))
                        Text(applied.title).font(.system(size: 12)).foregroundColor(.gray)
                    }
                    Spacer()
                    Button("إزالة") { dispatch(CheckoutViewModelContractActionRemoveCoupon()) }
                        .foregroundColor(Color(hex: "C62828"))
                }
                .padding(12)
                .background(Color(hex: "E8F5E9"))
                .cornerRadius(10)
            } else {
                HStack(spacing: 8) {
                    TextField(
                        "أدخل كود الخصم",
                        text: Binding(
                            get: { state.couponInput },
                            set: { dispatch(CheckoutViewModelContractActionUpdateCouponInput(code: $0)) }
                        )
                    )
                    .textFieldStyle(.roundedBorder)

                    Button("تطبيق") { dispatch(CheckoutViewModelContractActionApplyCoupon()) }
                        .padding(.horizontal, 16).padding(.vertical, 10)
                        .background(state.couponInput.isEmpty ? Color.gray.opacity(0.4) : Color(hex: "C62828"))
                        .foregroundColor(.white).cornerRadius(10)
                        .disabled(state.couponInput.isEmpty || state.isApplyingCoupon)
                }

                if let error = state.couponError {
                    Text(error).font(.system(size: 12)).foregroundColor(Color(hex: "C62828"))
                }
            }

            if !state.availableCoupons.isEmpty {
                Button("الكوبونات المتاحة") {
                    dispatch(CheckoutViewModelContractActionToggleCouponList())
                }
                .font(.system(size: 13))
                .foregroundColor(Color(hex: "C62828"))

                if state.isCouponListExpanded {
                    ForEach(state.availableCoupons, id: \.id) { coupon in
                        Button {
                            dispatch(CheckoutViewModelContractActionUpdateCouponInput(code: coupon.code))
                            dispatch(CheckoutViewModelContractActionApplyCoupon())
                        } label: {
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(coupon.code).font(.system(size: 14, weight: .bold))
                                    Text(coupon.title).font(.system(size: 12)).foregroundColor(.gray)
                                }
                                Spacer()
                                Text(discountLabel(for: coupon))
                                    .font(.system(size: 13, weight: .semibold))
                                    .foregroundColor(Color(hex: "2E7D32"))
                            }
                            .padding(.vertical, 8)
                        }
                    }
                }
            }
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.05), radius: 2)
    }

    private func discountLabel(for coupon: CouponDomain) -> String {
        coupon.discountType == .percent
            ? "خصم \(Int(coupon.discountValue))%"
            : "خصم " + JodPrice.format(coupon.discountValue)
    }
}

/// Jordan payment options: cash on delivery, CliQ transfer, card, wallet.
private struct PaymentSection: View {
    let state: CheckoutViewModelContractMyUiState
    let dispatch: (CheckoutViewModelContractAction) -> Void

    var body: some View {
        if state.paymentMethods.isEmpty {
            EmptyView()
        } else {
            VStack(alignment: .leading, spacing: 10) {
                Text("طريقة الدفع").font(.system(size: 16, weight: .bold))

                ForEach(state.paymentMethods, id: \.key) { method in
                    Button {
                        dispatch(CheckoutViewModelContractActionSelectPaymentMethod(type: method.type))
                    } label: {
                        HStack {
                            Image(systemName: state.selectedPaymentMethod == method.type
                                  ? "largecircle.fill.circle" : "circle")
                                .foregroundColor(Color(hex: "C62828"))
                            VStack(alignment: .leading, spacing: 2) {
                                Text(method.name).font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(.primary)
                                if let description = method.description_ {
                                    Text(description).font(.system(size: 12)).foregroundColor(.gray)
                                }
                            }
                            Spacer()
                        }
                        .padding(.vertical, 8)
                    }
                }

                // CliQ and other transfer methods need the customer's reference.
                if state.requiresPaymentReference {
                    if let alias = state.selectedMethod?.accountAlias {
                        Text("حوّل المبلغ إلى اسم كليك: \(alias)")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(Color(hex: "E65100"))
                    }

                    TextField(
                        "رقم الحوالة",
                        text: Binding(
                            get: { state.paymentReference },
                            set: { dispatch(CheckoutViewModelContractActionUpdatePaymentReference(reference: $0)) }
                        )
                    )
                    .textFieldStyle(.roundedBorder)
                }
            }
            .padding()
            .background(Color.white)
            .cornerRadius(12)
            .shadow(color: Color.black.opacity(0.05), radius: 2)
        }
    }
}
