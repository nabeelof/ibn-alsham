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
                            
                            // Featured Products
                            VStack(alignment: .leading, spacing: 12) {
                                Text("الأكثر مبيعاً")
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
