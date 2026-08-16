package com.ibnalsham.app.ui.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.ui.design.motion.bouncyClickable
import com.ibnalsham.app.ui.design.theme.AppTextStyles
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing

data class NavItem(
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String,
    val badgeCount: Int = 0,
    /** Guests may tap it, but the destination raises a gate. Shown with a marker. */
    val requiresAuth: Boolean = false,
)

/**
 * Floating pill navigation.
 *
 * Detached from the screen edge so content scrolls visibly underneath — the old
 * opaque full-width bar cut the layout in half and fought the edge-to-edge food
 * imagery this design leans on.
 *
 * The selected item animates a filled pill behind its icon and reveals its label;
 * unselected items stay icon-only, which is what lets four items fit at this width.
 */
@Composable
fun FloatingNavBar(
    items: List<NavItem>,
    selectedRoute: String?,
    onSelect: (NavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.gutter, vertical = Spacing.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black.copy(alpha = 0.4f),
                )
                .clip(CircleShape)
                .background(colors.surface)
                .padding(Spacing.xs),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                NavBarItem(
                    item = item,
                    selected = item.route == selectedRoute,
                    onClick = { onSelect(item) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    val tint by animateColorAsState(
        targetValue = if (selected) colors.onBrand else colors.textSecondary,
        animationSpec = Motion.snappy(),
        label = "navTint",
    )
    // Animating the colour rather than an alpha float: these springs overshoot
    // slightly, and Color.copy(alpha = ...) throws on a value below zero.
    val pillColor by animateColorAsState(
        targetValue = if (selected) colors.brand else colors.brand.copy(alpha = 0f),
        animationSpec = Motion.snappy(),
        label = "navPillColor",
    )
    val pillScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.6f,
        animationSpec = Motion.snappy(),
        label = "navPillScale",
    )
    val iconLift by animateDpAsState(
        targetValue = if (selected) (-2).dp else 0.dp,
        animationSpec = Motion.bouncy(),
        label = "navLift",
    )

    Column(
        modifier = modifier
            .bouncyClickable(pressedScale = 0.9f, onClick = onClick)
            .padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            // The pill lives behind the icon and fades rather than resizing, so
            // neighbouring items never shift as selection moves.
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 34.dp)
                    .scale(pillScale)
                    .clip(CircleShape)
                    .background(pillColor),
            )

            Box(modifier = Modifier.offset(y = iconLift)) {
                Icon(
                    imageVector = if (selected) item.selectedIcon else item.icon,
                    contentDescription = item.label,
                    tint = tint,
                    modifier = Modifier.size(22.dp),
                )
                CountBadge(
                    count = item.badgeCount,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
        }

        Spacer(Modifier.height(Spacing.xxs))

        Text(
            text = item.label,
            style = AppTextStyles.badge,
            color = if (selected) colors.brand else colors.textTertiary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
