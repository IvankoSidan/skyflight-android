package com.wheezy.skyflight.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.navigation.Screen
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

data class BottomMenuItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String,
    val requiresArg: Boolean = false
)

private fun prepareBottomMenu(): List<BottomMenuItem> = listOf(
    BottomMenuItem("Home", Icons.Default.Home, Screen.Main.route),
    BottomMenuItem("Flights", Icons.Default.Flight, Screen.SearchResult.route),
    BottomMenuItem("Seats", Icons.Default.EventSeat, Screen.SelectSeat.route, requiresArg = true),
    BottomMenuItem("Ticket", Icons.Default.ConfirmationNumber, Screen.TicketDetail.route)
)

@Composable
fun MyBottomBar(navController: NavHostController) {
    val items = prepareBottomMenu()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/")
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.takeIf { it >= 0 } ?: 0

    MyBottomBarUI(
        items = items,
        selectedIndex = selectedIndex,
        onItemSelected = { item ->
            if (currentRoute != item.route) {
                if (item.requiresArg) {
                    SnackbarHelper.showError("Please select a flight first")
                    return@MyBottomBarUI
                }
                try {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )
}

@Composable
private fun MyBottomBarUI(
    items: List<BottomMenuItem>,
    selectedIndex: Int,
    onItemSelected: (BottomMenuItem) -> Unit
) {
    val activeColor = Color(0xFF4A6BFF)
    val inactiveColor = Color(0xFF9A9A9A)
    val barBackground = MaterialTheme.colorScheme.surface
    val glowColor = Color(0xFF6C7BFF)

    val barHeight = 110.dp
    val indicatorSize = 56.dp

    val iconsTopDp = 36.dp
    val iconSizeDp = 24.dp

    Box(modifier = Modifier.fillMaxWidth().height(barHeight).padding(horizontal = 16.dp)) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val itemWidth = maxWidth / items.size

            val animatedCenterX by animateDpAsState(
                targetValue = (itemWidth * selectedIndex) + (itemWidth / 2),
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = ""
            )

            val infinite = rememberInfiniteTransition()
            val bobbing by infinite.animateFloat(
                initialValue = 0f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(animation = tween(1600, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
                label = ""
            )
            val bobbingDp = bobbing.dp

            val wobble by infinite.animateFloat(
                initialValue = 0f,
                targetValue = (2 * PI).toFloat(),
                animationSpec = infiniteRepeatable(animation = tween(1400, easing = LinearEasing), repeatMode = RepeatMode.Restart),
                label = ""
            )

            Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                val barTopY = 36.dp.toPx()
                val centerXPx = animatedCenterX.toPx()
                val waveWidthPx = (itemWidth * 1.2f).coerceAtLeast(100.dp).toPx()
                val controlLift = 28.dp.toPx()

                val waveStart = centerXPx - waveWidthPx / 2f
                val waveEnd = centerXPx + waveWidthPx / 2f
                val startControlX = lerp(waveStart, centerXPx, 0.25f)
                val endControlX = lerp(waveEnd, centerXPx, 0.25f)

                val topRadius = 15.dp.toPx()
                val bottomRadius = 45.dp.toPx()
                val basePath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = 0f,
                            top = barTopY,
                            right = size.width,
                            bottom = size.height,
                            topLeftCornerRadius = CornerRadius(topRadius, topRadius),
                            topRightCornerRadius = CornerRadius(topRadius, topRadius),
                            bottomLeftCornerRadius = CornerRadius(bottomRadius, bottomRadius),
                            bottomRightCornerRadius = CornerRadius(bottomRadius, bottomRadius)
                        )
                    )
                }

                val overlapY = barTopY + 25.dp.toPx()
                val wavePath = Path().apply {
                    moveTo(waveStart - 30.dp.toPx(), overlapY)
                    lineTo(waveStart, barTopY)
                    cubicTo(
                        startControlX, barTopY,
                        centerXPx - waveWidthPx * 0.18f, barTopY - controlLift,
                        centerXPx, barTopY - controlLift
                    )
                    cubicTo(
                        centerXPx + waveWidthPx * 0.18f, barTopY - controlLift,
                        endControlX, barTopY,
                        waveEnd, barTopY
                    )
                    lineTo(waveEnd + 30.dp.toPx(), overlapY)
                    close()
                }

                val finalPath = Path().apply { op(basePath, wavePath, PathOperation.Union) }
                drawPath(path = finalPath, color = barBackground)

                val bubbleCenterYDp = iconsTopDp + (iconSizeDp / 2f)
                val bubbleCenterYWithBobbingDp = bubbleCenterYDp + bobbingDp
                val bubbleCx = centerXPx
                val bubbleCy = bubbleCenterYWithBobbingDp.toPx()
                val baseRadius = indicatorSize.toPx() / 2f

                val wobbleAmp = baseRadius * 0.10f
                val topOffset = sin(wobble.toDouble()).toFloat() * wobbleAmp
                val leftOffset = sin((wobble + 1.0)).toFloat() * wobbleAmp
                val rightOffset = sin((wobble + 2.0)).toFloat() * wobbleAmp
                val bottomOffset = sin((wobble + 3.0)).toFloat() * wobbleAmp

                val rTop = baseRadius + topOffset
                val rLeft = baseRadius + leftOffset
                val rRight = baseRadius + rightOffset
                val rBottom = baseRadius + bottomOffset

                val blobPath = Path().apply {
                    moveTo(bubbleCx, bubbleCy - rTop)
                    cubicTo(
                        bubbleCx + rRight * 0.55f, bubbleCy - rTop,
                        bubbleCx + rRight, bubbleCy - rRight * 0.55f,
                        bubbleCx + rRight, bubbleCy
                    )
                    cubicTo(
                        bubbleCx + rRight, bubbleCy + rRight * 0.55f,
                        bubbleCx + rBottom * 0.55f, bubbleCy + rBottom,
                        bubbleCx, bubbleCy + rBottom
                    )
                    cubicTo(
                        bubbleCx - rLeft * 0.55f, bubbleCy + rBottom,
                        bubbleCx - rLeft, bubbleCy + rLeft * 0.55f,
                        bubbleCx - rLeft, bubbleCy
                    )
                    cubicTo(
                        bubbleCx - rLeft, bubbleCy - rLeft * 0.55f,
                        bubbleCx - rTop * 0.55f, bubbleCy - rTop,
                        bubbleCx, bubbleCy - rTop
                    )
                    close()
                }

                drawPath(
                    path = blobPath,
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.26f), Color.Transparent),
                        center = Offset(bubbleCx, bubbleCy),
                        radius = baseRadius * 2.4f
                    )
                )
            }

            val bubbleTopDp = (iconsTopDp + (iconSizeDp / 2f)) - (indicatorSize / 2f) + bobbingDp

            Box(
                modifier = Modifier
                    .offset { IntOffset((animatedCenterX - indicatorSize / 2).roundToPx(), bubbleTopDp.roundToPx()) }
                    .size(indicatorSize)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF8B99FF), Color(0xFF4A6BFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = items[selectedIndex].icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = iconsTopDp).fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    val itemCenterDp = (itemWidth * index) + (itemWidth / 2)
                    val distance = abs((animatedCenterX - itemCenterDp).value)
                    val maxDistance = itemWidth.value * 2f
                    val rawAttraction = (1f - (distance / maxDistance).coerceIn(0f, 1f))

                    val attraction by animateFloatAsState(targetValue = rawAttraction, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium), label = "")
                    val yOffsetDp = (-8).dp * attraction

                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight().clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onItemSelected(item)
                        }.offset { IntOffset(0, yOffsetDp.roundToPx()) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (!isSelected) {
                            Icon(imageVector = item.icon, contentDescription = item.label, tint = inactiveColor, modifier = Modifier.size(iconSizeDp))
                        } else {
                            Spacer(modifier = Modifier.height(iconSizeDp + 8.dp))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(text = item.label, fontSize = 12.sp, color = if (isSelected) activeColor else inactiveColor)
                    }
                }
            }
        }
    }
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t