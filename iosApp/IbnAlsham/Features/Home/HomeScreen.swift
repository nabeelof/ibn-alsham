import Combine
import shared
import SwiftUI

class HomeViewModelBridge: ViewModel<HomeViewModel> {
    @Published private(set) var uiState = HomeViewModelContractMyUiState.companion.defaultInstance()
    @Published var shouldNavigateToProduct = false
    @Published var selectedProductId: Int32 = 0
    @Published var shouldNavigateToCategory = false
    @Published var selectedCategoryId: Int32 = 0

    override init(model: HomeViewModel = KoinHelper().provideHomeViewModel) {
        super.init(model: model)
        uiState = model.currentState
        setupObservers()
    }

    private func setupObservers() {
        model.watchState(scope: model.viewModelScope) { [weak self] state in
            self?.uiState = state
        }.asCancellable().store(in: &subscriptions)

        model.watchEffect(scope: model.viewModelScope) { [weak self] effect in
            if let navigate = effect as? HomeViewModelContractSideEffectNavigateToProduct {
                self?.shouldNavigateToProduct = true
                self?.selectedProductId = navigate.productId
            }
            if let navigate = effect as? HomeViewModelContractSideEffectNavigateToCategory {
                self?.shouldNavigateToCategory = true
                self?.selectedCategoryId = navigate.categoryId
            }
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: HomeViewModelContractAction) {
        model.onAction(event: action)
    }
}

struct HomeScreen: View {
    @StateObject private var viewModel = HomeViewModelBridge()

    var body: some View {
        ZStack {
            Color(hex: "FAFAFA").edgesIgnoringSafeArea(.all)

            VStack(spacing: 0) {
                // Shared App Bar would be in Navigation View
                HStack {
                    Text("ابن الشام")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(Color(hex: "C62828"))
                    Spacer()
                }
                .padding()
                .background(Color.white)
                .shadow(color: Color.black.opacity(0.1), radius: 2, y: 2)

                if viewModel.uiState.isLoading {
                    Spacer()
                    ProgressView().progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "C62828")))
                    Spacer()
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 24) {
                            // In-flight order status card
                            if let order = viewModel.uiState.activeOrder {
                                ActiveOrderCard(order: order) {
                                    viewModel.dispatch(action: HomeViewModelContractActionActiveOrderClicked())
                                }
                                .padding(.horizontal, 16)
                                .padding(.top, 16)
                            }

                            // Banners Proxy
                            if !viewModel.uiState.banners.isEmpty {
                                Rectangle()
                                    .fill(Color.gray.opacity(0.3))
                                    .frame(height: 180)
                                    .cornerRadius(16)
                                    .overlay(
                                        Text(viewModel.uiState.banners[0].title ?? "")
                                            .font(.headline)
                                            .foregroundColor(.black)
                                    )
                                    .padding(.horizontal, 16)
                                    .padding(.top, 16)
                            }
                            
                            // Categories
                            VStack(alignment: .leading, spacing: 12) {
                                Text("ماذا تشتهي اليوم؟")
                                    .font(.system(size: 18, weight: .bold))
                                    .padding(.horizontal, 16)
                                
                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: 16) {
                                        ForEach(viewModel.uiState.categories, id: \.id) { category in
                                            Button(action: {
                                                viewModel.dispatch(action: HomeViewModelContractActionCategoryClicked(categoryId: category.id))
                                            }) {
                                                VStack {
                                                    Circle()
                                                        .fill(Color.gray.opacity(0.2))
                                                        .frame(width: 60, height: 60)
                                                    
                                                    Text(category.name)
                                                        .font(.system(size: 14, weight: .bold))
                                                        .foregroundColor(.black)
                                                }
                                            }
                                        }
                                    }
                                    .padding(.horizontal, 16)
                                }
                            }
                            
                            // Special offers — percentage and fixed-amount discounts
                            if !viewModel.uiState.specialOffers.isEmpty {
                                ProductSection(
                                    title: "العروض الخاصة",
                                    products: viewModel.uiState.specialOffers
                                ) { productId in
                                    viewModel.dispatch(
                                        action: HomeViewModelContractActionProductClicked(productId: productId)
                                    )
                                }
                            }

                            if !viewModel.uiState.mostOrdered.isEmpty {
                                ProductSection(
                                    title: "الأكثر مبيعاً",
                                    products: viewModel.uiState.mostOrdered
                                ) { productId in
                                    viewModel.dispatch(
                                        action: HomeViewModelContractActionProductClicked(productId: productId)
                                    )
                                }
                            }

                            // Featured Products
                            VStack(alignment: .leading, spacing: 12) {
                                Text("منتجات مميّزة")
                                    .font(.system(size: 18, weight: .bold))
                                    .padding(.horizontal, 16)
                                
                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: 16) {
                                        ForEach(viewModel.uiState.featuredProducts, id: \.id) { product in
                                            Button(action: {
                                                viewModel.dispatch(action: HomeViewModelContractActionProductClicked(productId: product.id))
                                            }) {
                                                VStack(alignment: .leading) {
                                                    Rectangle()
                                                        .fill(Color.gray.opacity(0.2))
                                                        .frame(width: 160, height: 120)
                                                    
                                                    VStack(alignment: .leading, spacing: 4) {
                                                        Text(product.name)
                                                            .font(.system(size: 15, weight: .bold))
                                                            .foregroundColor(.black)
                                                            .lineLimit(1)
                                                        
                                                        Text(String(format: "%.3f JOD", product.price))
                                                            .font(.system(size: 14, weight: .bold))
                                                            .foregroundColor(Color(hex: "C62828"))
                                                    }
                                                    .padding(12)
                                                }
                                                .background(Color.white)
                                                .cornerRadius(12)
                                                .shadow(color: Color.black.opacity(0.05), radius: 4)
                                                .frame(width: 160)
                                            }
                                        }
                                    }
                                    .padding(.horizontal, 16)
                                    .padding(.bottom, 16)
                                }
                            }
                        }
                    }
                }
            }
        }
        .environment(\.layoutDirection, .rightToLeft)
        .onAppear {
            viewModel.dispatch(action: HomeViewModelContractActionViewReady())
        }
    }
}


/// Status card for the order currently in flight. Tapping it opens tracking.
struct ActiveOrderCard: View {
    let order: ActiveOrderDomain
    let onTap: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "bag.fill")
                .frame(width: 44, height: 44)
                .background(Color.white.opacity(0.2))
                .clipShape(Circle())
                .foregroundColor(.white)

            VStack(alignment: .leading, spacing: 2) {
                Text("طلبك الحالي")
                    .font(.system(size: 12))
                    .foregroundColor(.white.opacity(0.85))
                Text(order.statusLabel.isEmpty ? order.orderNumber : order.statusLabel)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                if let eta = order.estimatedTime {
                    Text("حوالي \(eta) دقيقة")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.85))
                }
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text(JodPrice.format(order.total))
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(.white)
                Text("تتبّع الطلب")
                    .font(.system(size: 12))
                    .foregroundColor(.white.opacity(0.85))
            }
        }
        .padding(16)
        .background(Color(hex: "C62828"))
        .cornerRadius(16)
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }
}

/// A titled horizontal carousel of product cards.
struct ProductSection: View {
    let title: String
    let products: [ProductDomain]
    let onProductTap: (Int32) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.system(size: 18, weight: .bold))
                .padding(.horizontal, 16)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 16) {
                    ForEach(products, id: \.id) { product in
                        Button {
                            onProductTap(product.id)
                        } label: {
                            VStack(alignment: .leading, spacing: 8) {
                                AsyncImage(url: URL(string: product.image ?? "")) { image in
                                    image.resizable().scaledToFill()
                                } placeholder: {
                                    Rectangle().fill(Color.gray.opacity(0.15))
                                }
                                .frame(width: 150, height: 150)
                                .clipped()
                                .cornerRadius(12)

                                Text(product.name)
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.black)
                                    .lineLimit(2)

                                HStack(spacing: 6) {
                                    Text(JodPrice.format(product.effectivePrice))
                                        .font(.system(size: 14, weight: .bold))
                                        .foregroundColor(Color(hex: "C62828"))

                                    if product.hasDiscount {
                                        Text(JodPrice.format(product.price))
                                            .font(.system(size: 11))
                                            .strikethrough()
                                            .foregroundColor(.gray)
                                    }
                                }
                            }
                            .frame(width: 150)
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
    }
}
