package com.ibnalsham.app.ui.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.feature.search.presentation.viewModel.SearchViewModel
import com.ibnalsham.app.feature.search.presentation.viewModel.SearchViewModelContract
import com.ibnalsham.app.ui.feature.searchimpl.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchRoute(
    viewModel: SearchViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

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
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    state: SearchViewModelContract.MyUiState,
    onAction: (SearchViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        // Automatically focus the text field when screen launches
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA)) // Premium soft background
    ) {
        // Custom Header + Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.back), tint = Color.Black)
                }
                
                TextField(
                    value = state.query,
                    onValueChange = { onAction(SearchViewModelContract.Action.QueryChanged(it)) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(52.dp)
                        .focusRequester(focusRequester),
                    placeholder = { Text(stringResource(R.string.search_products), color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, tint = Color.Gray, contentDescription = null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { onAction(SearchViewModelContract.Action.QueryChanged("")) }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear), tint = Color.Gray)
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF0F0F0),
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFFE53935)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            onAction(SearchViewModelContract.Action.SubmitSearch)
                        }
                    ),
                    singleLine = true
                )
            }
        }

        // Content
        Box(modifier = Modifier.weight(1f)) {
            if (state.isLoading && state.results.isEmpty()) {
                CircularProgressIndicator(
                    color = Color(0xFFE53935),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.hasSearched && state.results.isEmpty() && !state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.no_results_for, state.query),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.results) { product ->
                        SearchProductItem(
                            product = product,
                            onClick = { onAction(SearchViewModelContract.Action.ProductClicked(product.id)) }
                        )
                    }
                }
            }
        }
    }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
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
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = product.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${String.format("%.3f", product.price)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE53935),
                    fontSize = 14.sp
                )
            }
        }
    }
}
