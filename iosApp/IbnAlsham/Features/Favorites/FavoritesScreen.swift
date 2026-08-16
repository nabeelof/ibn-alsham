import Combine
import shared
import SwiftUI

final class FavoritesViewModelBridge: ViewModel<FavoritesViewModel> {
    @Published private(set) var uiState = FavoritesViewModelContractMyUiState.companion.defaultInstance()
    @Published var productToOpen: Int32?
    @Published var shouldNavigateToMenu = false
    @Published var shouldNavigateToAuth = false
    @Published var snackbarMessage: String?

    override init(model: FavoritesViewModel = KoinHelper().provideFavoritesViewModel) {
        super.init(model: model)
        uiState = model.currentState
        setupObservers()
    }

    private func setupObservers() {
        model.watchState(scope: model.viewModelScope) { [weak self] state in
            self?.uiState = state
        }.asCancellable().store(in: &subscriptions)

        model.watchEffect(scope: model.viewModelScope) { [weak self] effect in
            switch effect {
            case let navigate as FavoritesViewModelContractSideEffectNavigateToProduct:
                self?.productToOpen = navigate.productId
            case is FavoritesViewModelContractSideEffectNavigateToMenu:
                self?.shouldNavigateToMenu = true
            case is FavoritesViewModelContractSideEffectRequireSignIn:
                self?.shouldNavigateToAuth = true
            case let snackbar as FavoritesViewModelContractSideEffectShowSnackbar:
                self?.snackbarMessage = snackbar.message
            default:
                break
            }
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: FavoritesViewModelContractAction) {
        model.onAction(event: action)
    }
}

struct FavoritesScreen: View {
    @StateObject private var viewModel = FavoritesViewModelBridge()

    var body: some View {
        ZStack {
            Color(hex: "FAFAFA").edgesIgnoringSafeArea(.all)

            if viewModel.uiState.isLoading {
                ProgressView().tint(Color(hex: "C62828"))
            } else if viewModel.uiState.requiresLogin {
                EmptyStateView(
                    systemImage: "person.crop.circle.badge.exclamationmark",
                    message: "الرجاء تسجيل الدخول للمتابعة",
                    actionTitle: "إعادة المحاولة",
                    action: { viewModel.dispatch(action: FavoritesViewModelContractActionRefresh()) }
                )
            } else if viewModel.uiState.isEmpty {
                EmptyStateView(
                    systemImage: "heart",
                    message: "لا توجد أصناف في المفضّلة",
                    actionTitle: "تصفّح المنيو",
                    action: { viewModel.dispatch(action: FavoritesViewModelContractActionGoToMenu()) }
                )
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.uiState.products, id: \.id) { product in
                            FavoriteRowView(
                                product: product,
                                isRemoving: viewModel.uiState.removingProductId?.int32Value == product.id,
                                onTap: {
                                    viewModel.dispatch(
                                        action: FavoritesViewModelContractActionProductClicked(productId: product.id)
                                    )
                                },
                                onRemove: {
                                    viewModel.dispatch(
                                        action: FavoritesViewModelContractActionRemoveFavorite(productId: product.id)
                                    )
                                },
                                onAddToCart: {
                                    viewModel.dispatch(
                                        action: FavoritesViewModelContractActionAddToCart(productId: product.id)
                                    )
                                }
                            )
                        }
                    }
                    .padding(16)
                }
            }
        }
        .navigationTitle("المفضّلة")
        .environment(\.layoutDirection, .rightToLeft)
        .onAppear {
            viewModel.dispatch(action: FavoritesViewModelContractActionViewReady())
        }
    }
}

private struct FavoriteRowView: View {
    let product: ProductDomain
    let isRemoving: Bool
    let onTap: () -> Void
    let onRemove: () -> Void
    let onAddToCart: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: product.image ?? "")) { image in
                image.resizable().scaledToFill()
            } placeholder: {
                Rectangle().fill(Color.gray.opacity(0.15))
            }
            .frame(width: 72, height: 72)
            .cornerRadius(10)

            VStack(alignment: .leading, spacing: 4) {
                Text(product.name)
                    .font(.system(size: 15, weight: .semibold))

                if let category = product.categoryName {
                    Text(category)
                        .font(.system(size: 12))
                        .foregroundColor(.gray)
                }

                HStack(spacing: 8) {
                    Text(JodPrice.format(product.effectivePrice))
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(Color(hex: "C62828"))

                    if product.hasDiscount {
                        Text(JodPrice.format(product.price))
                            .font(.system(size: 12))
                            .strikethrough()
                            .foregroundColor(.gray)
                    }
                }
            }

            Spacer()

            VStack(spacing: 8) {
                Button(action: onRemove) {
                    Image(systemName: isRemoving ? "heart" : "heart.fill")
                        .foregroundColor(Color(hex: "C62828"))
                }
                .disabled(isRemoving)

                Button(action: onAddToCart) {
                    Image(systemName: "plus")
                        .frame(width: 32, height: 32)
                        .background(Color(hex: "C62828").opacity(0.1))
                        .clipShape(Circle())
                        .foregroundColor(Color(hex: "C62828"))
                }
            }
        }
        .padding(12)
        .background(Color.white)
        .cornerRadius(14)
        .shadow(color: Color.black.opacity(0.05), radius: 2)
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }
}

struct EmptyStateView: View {
    let systemImage: String
    let message: String
    let actionTitle: String
    let action: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: systemImage)
                .font(.system(size: 56))
                .foregroundColor(.gray.opacity(0.5))

            Text(message)
                .font(.system(size: 15))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)

            Button(action: action) {
                Text(actionTitle)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(Color(hex: "C62828"))
                    .foregroundColor(.white)
                    .cornerRadius(24)
            }
        }
        .padding(32)
    }
}
