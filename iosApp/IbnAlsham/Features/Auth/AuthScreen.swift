import Combine
import shared
import SwiftUI

class AuthViewModelBridge: ViewModel<AuthViewModel> {
    @Published private(set) var uiState = AuthViewModelContractMyUiState.companion.defaultInstance()
    @Published var shouldNavigateToHome = false
    @Published var snackbarMessage = ""
    @Published var showSnackbar = false

    override init(model: AuthViewModel = KoinHelper().provideAuthViewModel) {
        super.init(model: model)
        uiState = model.currentState
        setupObservers()
    }

    private func setupObservers() {
        model.watchState(scope: model.viewModelScope) { [weak self] state in
            self?.uiState = state
        }.asCancellable().store(in: &subscriptions)

        model.watchEffect(scope: model.viewModelScope) { [weak self] effect in
            if effect is AuthViewModelContractSideEffectNavigateToHome {
                self?.shouldNavigateToHome = true
            }
            if let snack = effect as? AuthViewModelContractSideEffectShowSnackbar {
                self?.snackbarMessage = snack.message
                self?.showSnackbar = true
            }
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: AuthViewModelContractAction) {
        model.onAction(event: action)
    }
}

// SwiftUI View
struct AuthScreen: View {
    @StateObject private var viewModel = AuthViewModelBridge()
    @State private var phoneInput: String = ""
    @State private var otpInput: String = ""
    @State private var nameInput: String = ""

    var body: some View {
        ZStack {
            Color(hex: "FAFAFA").edgesIgnoringSafeArea(.all)

            VStack(spacing: 0) {
                Spacer().frame(height: 60)

                // App Logo
                ZStack {
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color(hex: "C62828"))
                        .frame(width: 120, height: 120)

                    Text("ابن الشام")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(Color(hex: "FFD54F"))
                }

                Spacer().frame(height: 48)

                switch viewModel.uiState.step {
                case .phone:
                    phoneStep
                case .otp:
                    otpStep
                case .register:
                    registerStep
                default:
                    EmptyView()
                }

                Spacer()
            }
            .padding(.horizontal, 24)
        }
        .environment(\.layoutDirection, .rightToLeft)
        // sync local state with view model state if needed, though iOS often handles this fine natively
    }

    private var phoneStep: some View {
        VStack(spacing: 24) {
            VStack(spacing: 8) {
                Text("تسجيل الدخول")
                    .font(.system(size: 24, weight: .bold))
                Text("الرجاء إدخال رقم هاتفك للمتابعة")
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.center)
            }

            TextField("رقم الهاتف", text: $phoneInput)
                .keyboardType(.phonePad)
                .padding()
                .background(Color.white)
                .cornerRadius(8)
                .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.gray.opacity(0.3), lineWidth: 1))
                .onChange(of: phoneInput) { newValue in
                    viewModel.dispatch(action: AuthViewModelContractActionUpdatePhone(phone: newValue))
                }

            Button(action: {
                viewModel.dispatch(action: AuthViewModelContractActionRequestOtp(phone: viewModel.uiState.phoneInput))
            }) {
                HStack {
                    if viewModel.uiState.isLoading {
                        ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("إرسال رمز التفعيل")
                            .font(.system(size: 16, weight: .bold))
                    }
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(viewModel.uiState.isLoading ? Color.gray : Color(hex: "C62828"))
                .foregroundColor(.white)
                .cornerRadius(8)
            }
            .disabled(viewModel.uiState.isLoading)
        }
    }

    private var otpStep: some View {
        VStack(spacing: 24) {
            VStack(spacing: 8) {
                Text("التحقق من الرقم")
                    .font(.system(size: 24, weight: .bold))
                Text("أدخل الرمز المرسل إلى\n\(viewModel.uiState.phoneInput)")
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.center)
            }

            TextField("رمز التحقق", text: $otpInput)
                .keyboardType(.numberPad)
                .multilineTextAlignment(.center)
                .padding()
                .background(Color.white)
                .cornerRadius(8)
                .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.gray.opacity(0.3), lineWidth: 1))
                .onChange(of: otpInput) { newValue in
                    viewModel.dispatch(action: AuthViewModelContractActionUpdateOtp(otp: newValue))
                    if newValue.count == 4 {
                        viewModel.dispatch(action: AuthViewModelContractActionVerifyOtp(phone: viewModel.uiState.phoneInput, otp: newValue))
                    }
                }
                .frame(maxWidth: 150)

            if viewModel.uiState.isLoading {
                ProgressView().progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "C62828")))
            } else {
                if viewModel.uiState.timerSeconds > 0 {
                    Text("إعادة إرسال الرمز خلال \(viewModel.uiState.timerSeconds) ثانية")
                        .foregroundColor(.gray)
                        .font(.system(size: 14))
                } else {
                    Button(action: { viewModel.dispatch(action: AuthViewModelContractActionResendOtp()) }) {
                        Text("إعادة إرسال الرمز")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(Color(hex: "C62828"))
                    }
                }
            }

            Button(action: { viewModel.dispatch(action: AuthViewModelContractActionBackToPhone()) }) {
                Text("تعديل رقم الهاتف")
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
            }
            .padding(.top, 8)
        }
    }

    private var registerStep: some View {
        VStack(spacing: 24) {
            VStack(spacing: 8) {
                Text("إكمال البيانات")
                    .font(.system(size: 24, weight: .bold))
                Text("أهلاً بك في حسابك الجديد")
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.center)
            }

            TextField("الاسم الكامل", text: $nameInput)
                .padding()
                .background(Color.white)
                .cornerRadius(8)
                .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.gray.opacity(0.3), lineWidth: 1))
                .onChange(of: nameInput) { newValue in
                    viewModel.dispatch(action: AuthViewModelContractActionUpdateName(name: newValue))
                }

            Button(action: {
                viewModel.dispatch(action: AuthViewModelContractActionRegister(
                    phone: viewModel.uiState.phoneInput,
                    otp: viewModel.uiState.otpInput,
                    name: viewModel.uiState.nameInput
                ))
            }) {
                HStack {
                    if viewModel.uiState.isLoading {
                        ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("إنشاء الحساب")
                            .font(.system(size: 16, weight: .bold))
                    }
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(viewModel.uiState.isLoading ? Color.gray : Color(hex: "C62828"))
                .foregroundColor(.white)
                .cornerRadius(8)
            }
            .disabled(viewModel.uiState.isLoading)
        }
    }
}
