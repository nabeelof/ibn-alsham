import Combine
import shared
import SwiftUI

class MoreViewModelBridge: ViewModel<MoreViewModel> {
    @Published private(set) var uiState = MoreViewModelContractMyUiState.companion.defaultInstance()
    @Published var shouldNavigateToSplash = false

    override init(model: MoreViewModel = KoinHelper().provideMoreViewModel) {
        super.init(model: model)
        uiState = model.currentState
        setupObservers()
    }

    private func setupObservers() {
        model.watchState(scope: model.viewModelScope) { [weak self] state in
            self?.uiState = state
        }.asCancellable().store(in: &subscriptions)

        model.watchEffect(scope: model.viewModelScope) { [weak self] effect in
            if effect is MoreViewModelContractSideEffectNavigateToSplash {
                self?.shouldNavigateToSplash = true
            }
            // Navigate based on effect in real iOS Navigation Router
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: MoreViewModelContractAction) {
        model.onAction(event: action)
    }
}

struct MoreScreen: View {
    @StateObject private var viewModel = MoreViewModelBridge()

    var body: some View {
        ZStack {
            Color(hex: "FAFAFA").edgesIgnoringSafeArea(.all)
            
            ScrollView {
                VStack(spacing: 24) {
                    
                    // User Header
                    Button(action: { viewModel.dispatch(action: MoreViewModelContractActionClickProfile()) }) {
                        HStack(spacing: 16) {
                            Circle()
                                .fill(Color.gray.opacity(0.3))
                                .frame(width: 60, height: 60)
                                .overlay(
                                    Image(systemName: "person.fill")
                                        .foregroundColor(.white)
                                        .font(.system(size: 30))
                                )
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text(viewModel.uiState.userName)
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(.black)
                                
                                Text(viewModel.uiState.userPhone)
                                    .font(.system(size: 14))
                                    .foregroundColor(.gray)
                            }
                            
                            Spacer()
                            
                            Image(systemName: "chevron.left") // Arabic direction
                                .foregroundColor(.gray)
                        }
                        .padding(16)
                        .background(Color.white)
                        .cornerRadius(16)
                        .shadow(color: Color.black.opacity(0.05), radius: 2)
                    }
                    .padding(.top, 16)
                    
                    // Options List
                    VStack(spacing: 0) {
                        ActionRow(icon: "heart.fill", title: "المفضلة") {
                            viewModel.dispatch(action: MoreViewModelContractActionClickFavorites())
                        }
                        Divider().padding(.leading, 48)
                        ActionRow(icon: "gearshape.fill", title: "الإعدادات") {
                            viewModel.dispatch(action: MoreViewModelContractActionClickSettings())
                        }
                        Divider().padding(.leading, 48)
                        ActionRow(icon: "phone.fill", title: "اتصل بنا المساعدة") {
                            viewModel.dispatch(action: MoreViewModelContractActionClickContactUs())
                        }
                    }
                    .background(Color.white)
                    .cornerRadius(16)
                    .shadow(color: Color.black.opacity(0.05), radius: 2)
                    
                    // Logout
                    Button(action: {
                        viewModel.dispatch(action: MoreViewModelContractActionLogout())
                    }) {
                        if viewModel.uiState.isLoggingOut {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .red))
                        } else {
                            Text("تسجيل الخروج")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.red)
                        }
                    }
                    .padding(.top, 16)
                }
                .padding(.horizontal, 16)
            }
        }
        .environment(\.layoutDirection, .rightToLeft)
        .onAppear {
            viewModel.dispatch(action: MoreViewModelContractActionViewReady())
        }
    }
}

struct ActionRow: View {
    let icon: String
    let title: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .foregroundColor(Color(hex: "C62828"))
                    .frame(width: 24, height: 24)
                
                Text(title)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.black)
                
                Spacer()
                
                Image(systemName: "chevron.left")
                    .foregroundColor(Color.gray.opacity(0.5))
            }
            .padding(16)
            .background(Color.white)
        }
    }
}
