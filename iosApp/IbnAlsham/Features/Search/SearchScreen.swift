import Combine
import shared
import SwiftUI

final class SearchViewModelBridge: ViewModel<SearchViewModel> {
    @Published private(set) var uiState = SearchViewModelContractMyUiState.companion.defaultInstance()
    @Published var productToOpen: Int32?
    @Published var shouldNavigateBack = false

    override init(model: SearchViewModel = KoinHelper().provideSearchViewModel) {
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
            case let navigate as SearchViewModelContractSideEffectNavigateToProduct:
                self?.productToOpen = navigate.productId
            case is SearchViewModelContractSideEffectNavigateBack:
                self?.shouldNavigateBack = true
            default:
                break
            }
        }.asCancellable().store(in: &subscriptions)
    }

    func dispatch(action: SearchViewModelContractAction) {
        model.onAction(event: action)
    }
}

struct SearchScreen: View {
    @StateObject private var viewModel = SearchViewModelBridge()

    var body: some View {
        ZStack {
            Color(hex: "F7F8FA").edgesIgnoringSafeArea(.all)

            VStack(spacing: 0) {
                searchBar

                if !viewModel.uiState.suggestions.isEmpty, !viewModel.uiState.query.isEmpty {
                    suggestionsRow
                }

                content
            }
        }
        .sheet(isPresented: .constant(viewModel.uiState.isFilterSheetOpen)) {
            FilterSheet(state: viewModel.uiState) { viewModel.dispatch(action: $0) }
        }
        .environment(\.layoutDirection, .rightToLeft)
        .onAppear {
            viewModel.dispatch(action: SearchViewModelContractActionViewReady())
        }
    }

    private var searchBar: some View {
        HStack(spacing: 8) {
            TextField(
                "ابحث في المنيو…",
                text: Binding(
                    get: { viewModel.uiState.query },
                    set: { viewModel.dispatch(action: SearchViewModelContractActionQueryChanged(query: $0)) }
                )
            )
            .textFieldStyle(.roundedBorder)
            .submitLabel(.search)
            .onSubmit { viewModel.dispatch(action: SearchViewModelContractActionSubmitSearch()) }

            Button {
                viewModel.dispatch(action: SearchViewModelContractActionOpenFilters())
            } label: {
                ZStack(alignment: .topTrailing) {
                    Image(systemName: "slider.horizontal.3")
                        .foregroundColor(viewModel.uiState.hasActiveFilters ? Color(hex: "C62828") : .black)
                        .font(.system(size: 20))

                    if viewModel.uiState.activeFilterCount > 0 {
                        Text("\(viewModel.uiState.activeFilterCount)")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(.white)
                            .padding(4)
                            .background(Color(hex: "C62828"))
                            .clipShape(Circle())
                            .offset(x: 8, y: -8)
                    }
                }
            }
        }
        .padding(12)
        .background(Color.white)
        .shadow(color: Color.black.opacity(0.05), radius: 4)
    }

    private var suggestionsRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(viewModel.uiState.suggestions.prefix(8), id: \.self) { suggestion in
                    Button {
                        viewModel.dispatch(
                            action: SearchViewModelContractActionSuggestionClicked(suggestion: suggestion)
                        )
                    } label: {
                        Text(suggestion)
                            .font(.system(size: 12))
                            .padding(.horizontal, 12).padding(.vertical, 6)
                            .background(Color(hex: "F0F0F0"))
                            .foregroundColor(.black)
                            .cornerRadius(14)
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
        }
        .background(Color.white)
    }

    @ViewBuilder
    private var content: some View {
        if viewModel.uiState.isLoading, viewModel.uiState.results.isEmpty {
            Spacer()
            ProgressView().tint(Color(hex: "C62828"))
            Spacer()
        } else if viewModel.uiState.isEmptyResult {
            Spacer()
            Text("لا توجد نتائج").foregroundColor(.gray)
            Spacer()
        } else if !viewModel.uiState.hasSearched {
            Spacer()
            Text("ابحث عن طبق أو فئة").foregroundColor(.gray)
            Spacer()
        } else {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(viewModel.uiState.results, id: \.id) { product in
                        SearchResultRow(product: product) {
                            viewModel.dispatch(
                                action: SearchViewModelContractActionProductClicked(productId: product.id)
                            )
                        }
                    }
                }
                .padding(16)
            }
        }
    }
}

private struct SearchResultRow: View {
    let product: ProductDomain
    let onTap: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: product.image ?? "")) { image in
                image.resizable().scaledToFill()
            } placeholder: {
                Rectangle().fill(Color.gray.opacity(0.15))
            }
            .frame(width: 90, height: 90)
            .clipped()
            .cornerRadius(12)

            VStack(alignment: .leading, spacing: 6) {
                Text(product.name).font(.system(size: 15, weight: .medium)).lineLimit(2)

                HStack(spacing: 8) {
                    Text(JodPrice.format(product.effectivePrice))
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(Color(hex: "C62828"))

                    if product.hasDiscount {
                        Text("خصم \(product.savedPercentage)%")
                            .font(.system(size: 11, weight: .bold))
                            .padding(.horizontal, 6).padding(.vertical, 2)
                            .background(Color(hex: "C62828").opacity(0.12))
                            .foregroundColor(Color(hex: "C62828"))
                            .cornerRadius(6)
                    }
                }
            }

            Spacer()
        }
        .padding(10)
        .background(Color.white)
        .cornerRadius(12)
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }
}

/// Advanced filters: category (with subcategories), JOD price range,
/// offers/featured toggles and sort order. Applied on confirm, not live.
private struct FilterSheet: View {
    let state: SearchViewModelContractMyUiState
    let dispatch: (SearchViewModelContractAction) -> Void

    @State private var minText = ""
    @State private var maxText = ""

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("الفلاتر").font(.system(size: 18, weight: .bold))

                Text("الفئة").font(.system(size: 15, weight: .semibold))
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        chip(title: "الكل", isSelected: state.draftFilter.categoryId == nil) {
                            dispatch(SearchViewModelContractActionSetCategory(categoryId: nil))
                        }
                        ForEach(state.filterCategories, id: \.id) { category in
                            chip(
                                title: category.name,
                                isSelected: state.draftFilter.categoryId?.int32Value == category.id
                            ) {
                                dispatch(
                                    SearchViewModelContractActionSetCategory(categoryId: KotlinInt(int: category.id))
                                )
                            }
                            ForEach(category.children, id: \.id) { child in
                                chip(
                                    title: "• " + child.name,
                                    isSelected: state.draftFilter.categoryId?.int32Value == child.id
                                ) {
                                    dispatch(
                                        SearchViewModelContractActionSetCategory(categoryId: KotlinInt(int: child.id))
                                    )
                                }
                            }
                        }
                    }
                }

                Text("نطاق السعر (دينار)").font(.system(size: 15, weight: .semibold))
                HStack(spacing: 12) {
                    TextField("من", text: $minText)
                        .keyboardType(.decimalPad)
                        .textFieldStyle(.roundedBorder)
                        .onChange(of: minText) { _ in pushPriceRange() }

                    TextField("إلى", text: $maxText)
                        .keyboardType(.decimalPad)
                        .textFieldStyle(.roundedBorder)
                        .onChange(of: maxText) { _ in pushPriceRange() }
                }

                Toggle("العروض فقط", isOn: Binding(
                    get: { state.draftFilter.onlyOffers },
                    set: { dispatch(SearchViewModelContractActionSetOnlyOffers(value: $0)) }
                ))

                Toggle("المميّزة فقط", isOn: Binding(
                    get: { state.draftFilter.onlyFeatured },
                    set: { dispatch(SearchViewModelContractActionSetOnlyFeatured(value: $0)) }
                ))

                Text("الترتيب").font(.system(size: 15, weight: .semibold))
                ForEach(ProductSort.values(), id: \.self) { sort in
                    Button {
                        dispatch(SearchViewModelContractActionSetSort(sort: sort))
                    } label: {
                        HStack {
                            Image(systemName: state.draftFilter.sort == sort
                                  ? "largecircle.fill.circle" : "circle")
                                .foregroundColor(Color(hex: "C62828"))
                            Text(label(for: sort)).foregroundColor(.primary)
                            Spacer()
                        }
                    }
                }

                HStack(spacing: 12) {
                    Button("مسح الفلاتر") { dispatch(SearchViewModelContractActionClearFilters()) }
                        .frame(maxWidth: .infinity).padding()
                        .background(Color(hex: "F0F0F0")).cornerRadius(26)

                    Button("تطبيق") { dispatch(SearchViewModelContractActionApplyFilters()) }
                        .frame(maxWidth: .infinity).padding()
                        .background(Color(hex: "C62828")).foregroundColor(.white).cornerRadius(26)
                }
            }
            .padding(20)
        }
        .environment(\.layoutDirection, .rightToLeft)
    }

    private func pushPriceRange() {
        dispatch(
            SearchViewModelContractActionSetPriceRange(
                min: JodPriceFormatter.shared.parse(input: minText).map { KotlinDouble(double: $0.doubleValue) },
                max: JodPriceFormatter.shared.parse(input: maxText).map { KotlinDouble(double: $0.doubleValue) }
            )
        )
    }

    private func chip(title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 13))
                .padding(.horizontal, 12).padding(.vertical, 8)
                .background(isSelected ? Color(hex: "C62828") : Color(hex: "F0F0F0"))
                .foregroundColor(isSelected ? .white : .black)
                .cornerRadius(16)
        }
    }

    private func label(for sort: ProductSort) -> String {
        switch sort {
        case .priceAsc: return "السعر: من الأقل للأعلى"
        case .priceDesc: return "السعر: من الأعلى للأقل"
        case .newest: return "الأحدث"
        case .bestSelling: return "الأكثر مبيعاً"
        default: return "الأنسب"
        }
    }
}
