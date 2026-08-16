import Combine
import shared
import SwiftUI

final class NotificationsViewModelBridge: ViewModel<NotificationsViewModel> {
    @Published private(set) var uiState = NotificationsViewModelContractMyUiState.companion.defaultInstance()
    @Published var orderToOpen: Int32?
    @Published var productToOpen: Int32?
    @Published var shouldNavigateToAuth = false
    @Published var snackbarMessage: String?

    override init(model: NotificationsViewModel = KoinHelper().provideNotificationsViewModel) {
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
            case let order as NotificationsViewModelContractSideEffectNavigateToOrder:
                self?.orderToOpen = order.orderId
            case let product as NotificationsViewModelContractSideEffectNavigateToProduct:
                self?.productToOpen = product.productId
            case is NotificationsViewModelContractSideEffectRequireSignIn:
                self?.shouldNavigateToAuth = true
            case let snackbar as NotificationsViewModelContractSideEffectShowSnackbar:
                self?.snackbarMessage = snackbar.message
            default:
                break
            }
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: NotificationsViewModelContractAction) {
        model.onAction(event: action)
    }
}

struct NotificationsScreen: View {
    @StateObject private var viewModel = NotificationsViewModelBridge()

    var body: some View {
        ZStack {
            Color(hex: "FAFAFA").edgesIgnoringSafeArea(.all)

            if viewModel.uiState.isLoading {
                ProgressView().tint(Color(hex: "C62828"))
            } else if viewModel.uiState.requiresLogin {
                EmptyStateView(
                    systemImage: "bell.slash",
                    message: "الرجاء تسجيل الدخول للمتابعة",
                    actionTitle: "إعادة المحاولة",
                    action: { viewModel.dispatch(action: NotificationsViewModelContractActionRefresh()) }
                )
            } else if viewModel.uiState.isEmpty {
                EmptyStateView(
                    systemImage: "bell",
                    message: "لا توجد إشعارات",
                    actionTitle: "إعادة المحاولة",
                    action: { viewModel.dispatch(action: NotificationsViewModelContractActionRefresh()) }
                )
            } else {
                ScrollView {
                    LazyVStack(spacing: 10) {
                        ForEach(viewModel.uiState.notifications, id: \.id) { notification in
                            NotificationRowView(notification: notification) {
                                viewModel.dispatch(
                                    action: NotificationsViewModelContractActionNotificationClicked(id: notification.id)
                                )
                            }
                        }
                    }
                    .padding(16)
                }
            }
        }
        .navigationTitle("الإشعارات")
        .toolbar {
            ToolbarItemGroup(placement: .navigationBarTrailing) {
                if viewModel.uiState.hasUnread {
                    Button {
                        viewModel.dispatch(action: NotificationsViewModelContractActionMarkAllRead())
                    } label: {
                        Image(systemName: "checkmark.circle")
                    }
                }
                if !viewModel.uiState.notifications.isEmpty {
                    Button {
                        viewModel.dispatch(action: NotificationsViewModelContractActionClearAll())
                    } label: {
                        Image(systemName: "trash")
                    }
                }
            }
        }
        .environment(\.layoutDirection, .rightToLeft)
        .onAppear {
            viewModel.dispatch(action: NotificationsViewModelContractActionViewReady())
        }
    }
}

private struct NotificationRowView: View {
    let notification: NotificationDomain
    let onTap: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: iconName)
                .frame(width: 40, height: 40)
                .background(Color(hex: "C62828").opacity(0.12))
                .clipShape(Circle())
                .foregroundColor(Color(hex: "C62828"))

            VStack(alignment: .leading, spacing: 4) {
                Text(notification.title)
                    .font(.system(size: 15, weight: notification.isRead ? .medium : .bold))

                Text(notification.body)
                    .font(.system(size: 13))
                    .foregroundColor(.gray)

                Text(notification.createdAtHuman)
                    .font(.system(size: 11))
                    .foregroundColor(.gray)
            }

            Spacer()
        }
        .padding(14)
        // Unread rows are tinted so they stand out at a glance.
        .background(notification.isRead ? Color.white : Color(hex: "C62828").opacity(0.06))
        .cornerRadius(14)
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }

    private var iconName: String {
        switch notification.type {
        case .order: return "bag"
        case .promotion: return "tag"
        default: return "bell"
        }
    }
}
