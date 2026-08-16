import Combine
import shared
import SwiftUI

final class ProfileViewModelBridge: ViewModel<ProfileViewModel> {
    @Published private(set) var uiState = ProfileViewModelContractMyUiState.companion.defaultInstance()
    @Published var shouldNavigateToAuth = false
    @Published var shouldNavigateBack = false
    @Published var snackbarMessage: String?

    override init(model: ProfileViewModel = KoinHelper().provideProfileViewModel) {
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
            case is ProfileViewModelContractSideEffectNavigateToAuth,
                 is ProfileViewModelContractSideEffectAccountDeleted:
                self?.shouldNavigateToAuth = true
            case is ProfileViewModelContractSideEffectNavigateBack:
                self?.shouldNavigateBack = true
            case let snackbar as ProfileViewModelContractSideEffectShowSnackbar:
                self?.snackbarMessage = snackbar.message
            default:
                break
            }
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: ProfileViewModelContractAction) {
        model.onAction(event: action)
    }
}

struct ProfileScreen: View {
    @StateObject private var viewModel = ProfileViewModelBridge()

    var body: some View {
        ZStack {
            Color(hex: "FAFAFA").edgesIgnoringSafeArea(.all)

            if viewModel.uiState.isLoading {
                ProgressView().tint(Color(hex: "C62828"))
            } else if let profile = viewModel.uiState.profile {
                ScrollView {
                    VStack(spacing: 20) {
                        Image(systemName: "person.circle.fill")
                            .font(.system(size: 76))
                            .foregroundColor(Color(hex: "C62828").opacity(0.8))

                        VStack(spacing: 4) {
                            Text(profile.name).font(.system(size: 20, weight: .bold))
                            Text(profile.phone).font(.system(size: 14)).foregroundColor(.gray)
                        }

                        HStack(spacing: 12) {
                            StatCardView(label: "الطلبات", value: Int(profile.ordersCount))
                            StatCardView(label: "المفضّلة", value: Int(profile.favoritesCount))
                        }

                        if viewModel.uiState.isEditing {
                            editingSection
                        } else {
                            infoSection(profile: profile)
                        }

                        Button {
                            viewModel.dispatch(action: ProfileViewModelContractActionLogout())
                        } label: {
                            Text("تسجيل الخروج")
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.white)
                                .foregroundColor(Color(hex: "C62828"))
                                .cornerRadius(12)
                        }

                        Button {
                            viewModel.dispatch(action: ProfileViewModelContractActionRequestDeleteAccount())
                        } label: {
                            Text("حذف الحساب").foregroundColor(.red).font(.system(size: 14))
                        }
                    }
                    .padding(20)
                }
            } else {
                EmptyStateView(
                    systemImage: "person.crop.circle.badge.exclamationmark",
                    message: viewModel.uiState.requiresLogin ? "الرجاء تسجيل الدخول للمتابعة" : "حدث خطأ ما",
                    actionTitle: "إعادة المحاولة",
                    action: { viewModel.dispatch(action: ProfileViewModelContractActionRefresh()) }
                )
            }
        }
        .navigationTitle("ملفي الشخصي")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                if !viewModel.uiState.isEditing, viewModel.uiState.profile != nil {
                    Button {
                        viewModel.dispatch(action: ProfileViewModelContractActionStartEditing())
                    } label: {
                        Image(systemName: "pencil")
                    }
                }
            }
        }
        .alert("حذف الحساب", isPresented: .constant(viewModel.uiState.showDeleteDialog)) {
            Button("إلغاء", role: .cancel) {
                viewModel.dispatch(action: ProfileViewModelContractActionDismissDeleteDialog())
            }
            Button("حذف", role: .destructive) {
                viewModel.dispatch(action: ProfileViewModelContractActionConfirmDeleteAccount())
            }
        } message: {
            Text("هل تريد حذف حسابك نهائياً؟ لا يمكن التراجع عن هذه الخطوة.")
        }
        .environment(\.layoutDirection, .rightToLeft)
        .onAppear {
            viewModel.dispatch(action: ProfileViewModelContractActionViewReady())
        }
    }

    private var editingSection: some View {
        VStack(spacing: 12) {
            TextField(
                "الاسم",
                text: Binding(
                    get: { viewModel.uiState.nameInput },
                    set: { viewModel.dispatch(action: ProfileViewModelContractActionUpdateName(name: $0)) }
                )
            )
            .textFieldStyle(.roundedBorder)

            HStack(spacing: 12) {
                Button("إلغاء") {
                    viewModel.dispatch(action: ProfileViewModelContractActionCancelEditing())
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.white)
                .cornerRadius(12)

                Button("حفظ") {
                    viewModel.dispatch(action: ProfileViewModelContractActionSaveProfile())
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(viewModel.uiState.canSave ? Color(hex: "C62828") : Color.gray.opacity(0.4))
                .foregroundColor(.white)
                .cornerRadius(12)
                .disabled(!viewModel.uiState.canSave)
            }
        }
    }

    private func infoSection(profile: UserProfileDomain) -> some View {
        VStack(spacing: 8) {
            InfoRowView(label: "الاسم", value: profile.name)
            InfoRowView(label: "رقم الهاتف", value: profile.phone)
        }
    }
}

private struct StatCardView: View {
    let label: String
    let value: Int

    var body: some View {
        VStack(spacing: 4) {
            Text("\(value)").font(.system(size: 22, weight: .bold))
            Text(label).font(.system(size: 13)).foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color.white)
        .cornerRadius(14)
    }
}

private struct InfoRowView: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label).foregroundColor(.gray).font(.system(size: 14))
            Spacer()
            Text(value).fontWeight(.semibold).font(.system(size: 14))
        }
        .padding(.vertical, 10)
    }
}
