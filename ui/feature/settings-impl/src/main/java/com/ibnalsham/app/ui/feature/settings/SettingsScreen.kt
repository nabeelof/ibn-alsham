package com.ibnalsham.app.ui.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.ibnalsham.app.feature.settings.presentation.viewModel.AppLanguage
import com.ibnalsham.app.feature.settings.presentation.viewModel.SettingsViewModel
import com.ibnalsham.app.feature.settings.presentation.viewModel.SettingsViewModelContract
import com.ibnalsham.app.library.data.cache.ThemeMode
import com.ibnalsham.app.ui.design.components.AppCard
import com.ibnalsham.app.ui.design.components.AppTopBar
import com.ibnalsham.app.ui.design.components.SectionHeader
import com.ibnalsham.app.ui.design.motion.bouncyClickable
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.feature.settingsimpl.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onAction(SettingsViewModelContract.Action.ViewReady)

        viewModel.sideEffects.collect { effect ->
            when (effect) {
                // Changing the app locale recreates the activity, which is why
                // this is a side effect rather than plain state.
                is SettingsViewModelContract.SideEffect.ApplyLanguage ->
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(effect.tag),
                    )

                is SettingsViewModelContract.SideEffect.OpenUrl ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)))
            }
        }
    }

    SettingsScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
    )
}

/**
 * Settings.
 *
 * Appearance is the headline: the three theme modes are shown as preview cards
 * rather than a list, so the choice is visible before it is made. Picking one
 * re-themes the app immediately — [com.ibnalsham.app.library.data.cache.AppPreferences]
 * publishes the change and the composition root re-reads it.
 */
@Composable
fun SettingsScreen(
    state: SettingsViewModelContract.MyUiState,
    onAction: (SettingsViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState()),
    ) {
        AppTopBar(
            title = stringResource(R.string.settings_title),
            onNavigateBack = onNavigateBack,
        )

        SectionHeader(
            title = stringResource(R.string.settings_appearance),
            eyebrow = stringResource(R.string.settings_appearance_eyebrow),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter)
                .enterFromBottom(index = 0),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            ThemeMode.entries.forEach { mode ->
                ThemeOptionCard(
                    mode = mode,
                    selected = state.themeMode == mode,
                    onClick = { onAction(SettingsViewModelContract.Action.SelectThemeMode(mode)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(Spacing.xxl))

        SectionHeader(
            title = stringResource(R.string.settings_language),
            eyebrow = stringResource(R.string.settings_language_eyebrow),
        )

        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter)
                .enterFromBottom(index = 1),
        ) {
            AppLanguage.entries.forEachIndexed { index, language ->
                SettingsRow(
                    icon = Icons.Rounded.Language,
                    title = stringResource(
                        when (language) {
                            AppLanguage.Arabic -> R.string.settings_language_arabic
                            AppLanguage.English -> R.string.settings_language_english
                        },
                    ),
                    selected = state.language == language,
                    showDivider = index != AppLanguage.entries.lastIndex,
                    onClick = { onAction(SettingsViewModelContract.Action.SelectLanguage(language)) },
                )
            }
        }

        Spacer(Modifier.height(Spacing.xxl))

        SectionHeader(
            title = stringResource(R.string.settings_about),
            eyebrow = stringResource(R.string.settings_about_eyebrow),
        )

        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter)
                .enterFromBottom(index = 2),
        ) {
            SettingsRow(
                icon = Icons.Rounded.Shield,
                title = stringResource(R.string.settings_privacy),
                onClick = { onAction(SettingsViewModelContract.Action.ClickPrivacyPolicy) },
            )
            SettingsRow(
                icon = Icons.Rounded.Description,
                title = stringResource(R.string.settings_terms),
                onClick = { onAction(SettingsViewModelContract.Action.ClickTerms) },
                showDivider = false,
            )
        }

        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.settings_version, state.appVersion),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.navBarClearance))
    }
}

/**
 * One theme choice, shown as a miniature of what it looks like.
 *
 * The swatch uses fixed colours rather than theme tokens — it has to show the
 * *other* themes accurately while the current one is active.
 */
@Composable
private fun ThemeOptionCard(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    val border by animateColorAsState(
        targetValue = if (selected) colors.brand else colors.outline,
        animationSpec = Motion.snappy(),
        label = "themeCardBorder",
    )
    val checkScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = Motion.bouncy(),
        label = "themeCardCheck",
    )

    Column(
        modifier = modifier
            .bouncyClickable(pressedScale = 0.96f, onClick = onClick)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surface)
            .border(2.dp, border, MaterialTheme.shapes.medium)
            .padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(mode.swatch()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = mode.icon(),
                    contentDescription = null,
                    tint = mode.swatchContent(),
                    modifier = Modifier.size(24.dp),
                )
            }

            Box(
                modifier = Modifier
                    .scale(checkScale)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colors.brand),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = colors.onBrand,
                    modifier = Modifier.size(13.dp),
                )
            }
        }

        Spacer(Modifier.height(Spacing.sm))

        Text(
            text = stringResource(mode.labelRes()),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) colors.brand else colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    selected: Boolean = false,
    showDivider: Boolean = true,
) {
    val colors = AppTheme.colors

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .bouncyClickable(pressedScale = 0.99f, onClick = onClick)
                .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(Spacing.md))
                    .background(colors.brand.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = colors.brand, modifier = Modifier.size(19.dp))
            }

            Spacer(Modifier.width(Spacing.md))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )

            if (selected) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.brand)
            }
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 70.dp, end = Spacing.lg)
                    .height(1.dp)
                    .background(colors.outline),
            )
        }
    }
}

@Composable
private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.System -> R.string.settings_theme_system
    ThemeMode.Light -> R.string.settings_theme_light
    ThemeMode.Dark -> R.string.settings_theme_dark
}

private fun ThemeMode.icon(): ImageVector = when (this) {
    ThemeMode.System -> Icons.Rounded.PhoneIphone
    ThemeMode.Light -> Icons.Rounded.LightMode
    ThemeMode.Dark -> Icons.Rounded.DarkMode
}

/** Fixed preview colours — these must read correctly under either active theme. */
private fun ThemeMode.swatch(): Color = when (this) {
    ThemeMode.System -> Color(0xFF8A7D79)
    ThemeMode.Light -> Color(0xFFF7F3F0)
    ThemeMode.Dark -> Color(0xFF1E1817)
}

private fun ThemeMode.swatchContent(): Color = when (this) {
    ThemeMode.Light -> Color(0xFF15100F)
    else -> Color.White
}
