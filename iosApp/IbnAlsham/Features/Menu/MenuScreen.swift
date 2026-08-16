import Combine
import shared
import SwiftUI

class MenuViewModelBridge: ViewModel<MenuViewModel> {
    @Published private(set) var uiState = MenuViewModelContractMyUiState.companion.defaultInstance()
    @Published var shouldNavigateToProduct = false
    @Published var selectedProductId: Int32 = 0
    @Published var shouldShowSnackbar = false
    @Published var snackbarMessage = ""

    override init(model: MenuViewModel = KoinHelper().provideMenuViewModel) {
        super.init(model: model)
        uiState = model.currentState
        setupObservers()
    }

    private func setupObservers() {
        model.watchState(scope: model.viewModelScope) { [weak self] state in
            self?.uiState = state
        }.asCancellable().store(in: &subscriptions)

        model.watchEffect(scope: model.viewModelScope) { [weak self] effect in
            if let navigateEffect = effect as? MenuViewModelContractSideEffectNavigateToProduct {
                self?.shouldNavigateToProduct = true
                self?.selectedProductId = navigateEffect.productId
            }
            if let snackbarEffect = effect as? MenuViewModelContractSideEffectShowCartSnackbar {
                self?.shouldShowSnackbar = true
                self?.snackbarMessage = snackbarEffect.message
            }
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: MenuViewModelContractAction) {
        model.onAction(event: action)
    }
}

// SwiftUI View
struct MenuScreen: View {
    @StateObject private var viewModel = MenuViewModelBridge()

    var body: some View {
        ZStack {
            Color(hex: "FAFAFA").edgesIgnoringSafeArea(.all)

            if viewModel.uiState.isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "C62828")))
            } else {
                VStack(spacing: 0) {
                    // Categories Row
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(viewModel.uiState.categories, id: \.id) { category in
                                CategoryChip(
                                    category: category,
                                    isSelected: viewModel.uiState.selectedCategoryId == category.id
                                ) {
                                    viewModel.dispatch(action: MenuViewModelContractActionCategorySelected(categoryId: category.id))
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                    }

                    // Products Circle Grid
                    ScrollView {
                        LazyVGrid(
                            columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: 3),
                            spacing: 20
                        ) {
                            ForEach(viewModel.uiState.products, id: \.id) { product in
                                ProductCircleItem(
                                    product: product,
                                    onClick: { viewModel.dispatch(action: MenuViewModelContractActionProductClicked(productId: product.id)) }
                                )
                            }
                        }
                        .padding(16)
                    }
                }
            }
        }
        .environment(\.layoutDirection, .rightToLeft) // Enforce RTL
    }
}

struct CategoryChip: View {
    let category: CategoryDomain
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(category.name)
                .font(.system(size: 16, weight: .bold))
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
                .background(isSelected ? Color(hex: "C62828") : Color.white)
                .foregroundColor(isSelected ? .white : .black)
                .cornerRadius(24)
                .shadow(color: Color.black.opacity(isSelected ? 0.2 : 0.05), radius: isSelected ? 4 : 1)
        }
    }
}

struct ProductCircleItem: View {
    let product: ProductDomain
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            VStack(spacing: 8) {
                if let urlString = product.image, let url = URL(string: urlString) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let image):
                            image.resizable().scaledToFill()
                        default:
                            Color(hex: "EEEEEE")
                        }
                    }
                    .frame(width: 96, height: 96)
                    .clipShape(Circle())
                } else {
                    Circle()
                        .fill(Color(hex: "EEEEEE"))
                        .frame(width: 96, height: 96)
                }

                Text(product.name)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(.black)
                    .multilineTextAlignment(.center)
                    .lineLimit(2)

                Text(String(format: "%.3f JOD", product.price))
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(Color(hex: "C62828"))
            }
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(.plain)
    }
}

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }

        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
