package com.ibnalsham.app.ui.feature.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.ibnalsham.app.ui.feature.moreimpl.R
import com.ibnalsham.app.feature.more.presentation.viewModel.MoreViewModel
import com.ibnalsham.app.feature.more.presentation.viewModel.MoreViewModelContract
import org.koin.androidx.compose.koinViewModel

@Composable
fun MoreRoute(
    viewModel: MoreViewModel = koinViewModel(),
    onNavigateToProfile: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(MoreViewModelContract.Action.ViewReady)
        
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                MoreViewModelContract.SideEffect.NavigateToProfile -> onNavigateToProfile()
                MoreViewModelContract.SideEffect.NavigateToFavorites -> onNavigateToFavorites()
                MoreViewModelContract.SideEffect.NavigateToSplash -> onLogoutSuccess()
                is MoreViewModelContract.SideEffect.NavigateToSettings -> { /* TODO */ }
                is MoreViewModelContract.SideEffect.OpenUrl -> { /* Platform intent to open URL */ }
            }
        }
    }

    MoreScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onLanguageChange = { langCode ->
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    state: MoreViewModelContract.MyUiState,
    onAction: (MoreViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit = {},
    onLanguageChange: (String) -> Unit = {}
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { langCode ->
                onLanguageChange(langCode)
                showLanguageDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.more_title), fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E)) },
            navigationIcon = { },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        // User Header
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAction(MoreViewModelContract.Action.ClickProfile) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(state.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(state.userPhone, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
                
                Icon(if (androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl) Icons.Default.KeyboardArrowLeft else Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Options List
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp
        ) {
            Column {
                MoreItemRow(icon = Icons.Default.Person, title = stringResource(R.string.profile), onClick = { onAction(MoreViewModelContract.Action.ClickProfile) })
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFFAFAFA))
                MoreItemRow(icon = Icons.Default.Favorite, title = stringResource(R.string.favorites), onClick = { onAction(MoreViewModelContract.Action.ClickFavorites) })
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFFAFAFA))
                MoreItemRow(icon = Icons.Default.Language, title = stringResource(R.string.language), onClick = { showLanguageDialog = true })
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFFAFAFA))
                MoreItemRow(icon = Icons.Default.Settings, title = stringResource(R.string.settings), onClick = { onAction(MoreViewModelContract.Action.ClickSettings) })
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFFAFAFA))
                MoreItemRow(icon = Icons.Default.Call, title = stringResource(R.string.contact_us), onClick = { onAction(MoreViewModelContract.Action.ClickContactUs) })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout
        TextButton(
            onClick = { onAction(MoreViewModelContract.Action.Logout) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !state.isLoggingOut
        ) {
            if (state.isLoggingOut) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Red)
            } else {
                Text(stringResource(R.string.logout), color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LanguageSelectionDialog(onDismiss: () -> Unit, onLanguageSelected: (String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(stringResource(R.string.select_language), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                
                LanguageOption(stringResource(R.string.english), "en", onClick = { onLanguageSelected("en") })
                Divider(color = Color(0xFFFAFAFA))
                LanguageOption(stringResource(R.string.arabic), "ar", onClick = { onLanguageSelected("ar") })
                
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.cancel), color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun LanguageOption(title: String, code: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        if (androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags().contains(code)) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFC62828))
        }
    }
}

@Composable
fun MoreItemRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFC62828))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Icon(if (androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl) Icons.Default.KeyboardArrowLeft else Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
    }
}
