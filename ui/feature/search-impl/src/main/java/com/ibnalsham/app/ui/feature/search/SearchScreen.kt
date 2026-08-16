package com.ibnalsham.app.ui.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ibnalsham.app.core.priceFormatter.JodPriceFormatter
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.feature.domain.ProductSort
import com.ibnalsham.app.feature.search.presentation.viewModel.SearchViewModel
import com.ibnalsham.app.feature.search.presentation.viewModel.SearchViewModelContract
import com.ibnalsham.app.ui.design.components.jodPrice
import com.ibnalsham.app.ui.feature.searchimpl.R
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import androidx.compose.foundation.lazy.itemsIndexed
import com.ibnalsham.app.ui.design.components.LoadingState
import com.ibnalsham.app.ui.design.components.ProductRow
import com.ibnalsham.app.ui.design.components.StateView
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Spacing
import org.koin.androidx.compose.koinViewModel

// Local alias so this screen's many call sites stay short.
private val BrandRed = com.ibnalsham.app.ui.design.theme.Ember

@Composable
fun SearchRoute(
    viewModel: SearchViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (Int) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(SearchViewModelContract.Action.ViewReady)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is SearchViewModelContract.SideEffect.NavigateBack -> onNavigateBack()
                is SearchViewModelContract.SideEffect.NavigateToProduct -> onNavigateToProduct(effect.productId)
            }
        }
    }

    SearchScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
    )

    if (state.isFilterSheetOpen) {
        FilterSheet(state = state, onAction = viewModel::onAction)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    state: SearchViewModelContract.MyUiState,
    onAction: (SearchViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(DesignR.string.back),
                            tint = Color.Black,
                        )
                    }

                    TextField(
                        value = state.query,
                        onValueChange = { onAction(SearchViewModelContract.Action.QueryChanged(it)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text(stringResource(R.string.search_products), color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, tint = Color.Gray, contentDescription = null) },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { onAction(SearchViewModelContract.Action.ClearQuery) }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear),
                                        tint = Color.Gray,
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.colors.surfaceSunken,
                            unfocusedContainerColor = AppTheme.colors.surfaceSunken,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = BrandRed,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                onAction(SearchViewModelContract.Action.SubmitSearch)
                            }
                        ),
                        singleLine = true,
                    )

                    // Filter entry point, badged with how many filters are on.
                    BadgedBox(
                        badge = {
                            if (state.activeFilterCount > 0) {
                                Badge(containerColor = BrandRed) {
                                    Text("${state.activeFilterCount}", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { onAction(SearchViewModelContract.Action.OpenFilters) }) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = stringResource(DesignR.string.search_filters),
                                tint = if (state.hasActiveFilters) BrandRed else Color.Black,
                            )
                        }
                    }
                }

                if (state.suggestions.isNotEmpty() && state.query.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.suggestions.take(8).forEach { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    keyboardController?.hide()
                                    onAction(SearchViewModelContract.Action.SuggestionClicked(suggestion))
                                },
                                label = { Text(suggestion, fontSize = 12.sp) },
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading && state.results.isEmpty() -> LoadingState()

                state.isEmptyResult -> EmptyMessage(
                    text = if (state.query.isBlank()) {
                        stringResource(DesignR.string.search_no_results)
                    } else {
                        stringResource(R.string.no_results_for, state.query)
                    }
                )

                !state.hasSearched -> EmptyMessage(text = stringResource(DesignR.string.search_start_hint))

                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = Spacing.gutter,
                        end = Spacing.gutter,
                        top = Spacing.md,
                        bottom = Spacing.navBarClearance,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    itemsIndexed(state.results, key = { _, product -> product.id }) { index, product ->
                        ProductRow(
                            product = product,
                            onClick = { onAction(SearchViewModelContract.Action.ProductClicked(product.id)) },
                            modifier = Modifier.enterFromBottom(index = index),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    StateView(
        icon = Icons.Default.Search,
        title = text,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
fun SearchProductItem(product: ProductDomain, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = product.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFF1E1E1E),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = jodPrice(product.effectivePrice),
                        fontWeight = FontWeight.Bold,
                        color = BrandRed,
                        fontSize = 14.sp,
                    )
                    if (product.hasDiscount) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = BrandRed.copy(alpha = 0.12f)) {
                            Text(
                                text = stringResource(DesignR.string.offer_percent_off, product.savedPercentage),
                                color = BrandRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Advanced filters: category (with subcategories), JOD price range,
 * offers/featured toggles, and sort order. Applied on confirm, not live.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    state: SearchViewModelContract.MyUiState,
    onAction: (SearchViewModelContract.Action) -> Unit,
) {
    val draft = state.draftFilter
    var minText by remember(draft.minPrice) {
        mutableStateOf(draft.minPrice?.let { JodPriceFormatter.formatAmount(it) } ?: "")
    }
    var maxText by remember(draft.maxPrice) {
        mutableStateOf(draft.maxPrice?.let { JodPriceFormatter.formatAmount(it) } ?: "")
    }

    ModalBottomSheet(
        onDismissRequest = { onAction(SearchViewModelContract.Action.CloseFilters) },
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                stringResource(DesignR.string.search_filters),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E1E1E),
            )

            // Category
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(DesignR.string.search_filter_category),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = draft.categoryId == null,
                    onClick = { onAction(SearchViewModelContract.Action.SetCategory(null)) },
                    label = { Text(stringResource(DesignR.string.home_all)) },
                )
                state.filterCategories.forEach { category ->
                    CategoryChips(
                        category = category,
                        selectedId = draft.categoryId,
                        onSelect = { onAction(SearchViewModelContract.Action.SetCategory(it)) },
                    )
                }
            }

            // Price range (JOD)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(DesignR.string.search_filter_price),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minText,
                    onValueChange = {
                        minText = it
                        onAction(
                            SearchViewModelContract.Action.SetPriceRange(
                                min = JodPriceFormatter.parse(it),
                                max = JodPriceFormatter.parse(maxText),
                            )
                        )
                    },
                    label = { Text(stringResource(DesignR.string.search_filter_min)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = maxText,
                    onValueChange = {
                        maxText = it
                        onAction(
                            SearchViewModelContract.Action.SetPriceRange(
                                min = JodPriceFormatter.parse(minText),
                                max = JodPriceFormatter.parse(it),
                            )
                        )
                    },
                    label = { Text(stringResource(DesignR.string.search_filter_max)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            // Toggles
            Spacer(modifier = Modifier.height(8.dp))
            ToggleRow(
                label = stringResource(DesignR.string.search_filter_only_offers),
                checked = draft.onlyOffers,
                onCheckedChange = { onAction(SearchViewModelContract.Action.SetOnlyOffers(it)) },
            )
            ToggleRow(
                label = stringResource(DesignR.string.search_filter_only_featured),
                checked = draft.onlyFeatured,
                onCheckedChange = { onAction(SearchViewModelContract.Action.SetOnlyFeatured(it)) },
            )

            // Sort
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(DesignR.string.search_filter_sort),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            ProductSort.entries.forEach { sort ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onAction(SearchViewModelContract.Action.SetSort(sort)) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = draft.sort == sort,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = BrandRed),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(sortLabel(sort), fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { onAction(SearchViewModelContract.Action.ClearFilters) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                ) {
                    Text(stringResource(DesignR.string.search_clear_filters))
                }
                Button(
                    onClick = { onAction(SearchViewModelContract.Action.ApplyFilters) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                    shape = RoundedCornerShape(26.dp),
                ) {
                    Text(stringResource(DesignR.string.apply), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** A category chip followed by chips for each of its subcategories. */
@Composable
private fun CategoryChips(
    category: CategoryDomain,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
) {
    FilterChip(
        selected = selectedId == category.id,
        onClick = { onSelect(category.id) },
        label = { Text(category.name) },
    )
    category.children.forEach { child ->
        FilterChip(
            selected = selectedId == child.id,
            onClick = { onSelect(child.id) },
            label = { Text("• ${child.name}") },
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = BrandRed),
        )
    }
}

@Composable
private fun sortLabel(sort: ProductSort): String = stringResource(
    when (sort) {
        ProductSort.DEFAULT -> DesignR.string.search_sort_default
        ProductSort.PRICE_ASC -> DesignR.string.search_sort_price_asc
        ProductSort.PRICE_DESC -> DesignR.string.search_sort_price_desc
        ProductSort.NEWEST -> DesignR.string.search_sort_newest
        ProductSort.BEST_SELLING -> DesignR.string.search_sort_best_selling
    }
)
