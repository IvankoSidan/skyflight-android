package com.wheezy.skyflight.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import com.wheezy.skyflight.navigation.Screen

private val drawerTextSize = 16.sp

@Composable
fun SideDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    currentTheme: com.wheezy.skyflight.core.model.ThemeOption,
    onThemeSelected: (com.wheezy.skyflight.core.model.ThemeOption) -> Unit,
    onLogout: () -> Unit,
    onOpenBookingHistory: () -> Unit,
    navController: NavHostController? = null,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onClose() }
            )
        }
        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            GlassCard(
                modifier = Modifier.fillMaxHeight().width(300.dp),
                blurRadius = 25f, tintAlpha = 0.3f, strokeAlpha = 0.2f, cornerRadius = 0.dp, enableGlow = false
            ) {
                SideDrawerContent(
                    modifier = Modifier.fillMaxSize(),
                    currentTheme = currentTheme,
                    onThemeSelected = onThemeSelected,
                    onLogout = onLogout,
                    onOpenBookingHistory = onOpenBookingHistory,
                    onClose = onClose,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun SideDrawerContent(
    modifier: Modifier,
    currentTheme: com.wheezy.skyflight.core.model.ThemeOption,
    onThemeSelected: (com.wheezy.skyflight.core.model.ThemeOption) -> Unit,
    onLogout: () -> Unit,
    onOpenBookingHistory: () -> Unit,
    onClose: () -> Unit,
    navController: NavHostController? = null
) {
    Column(modifier = modifier.fillMaxHeight().padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp).clickable { onClose() }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 1.dp)

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        DrawerSectionTitle("Account")
        Spacer(modifier = Modifier.height(8.dp))
        DrawerMenuItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            text = "Log out",
            onClick = onLogout
        )
        DrawerMenuItem(
            icon = Icons.Default.History,
            text = "Booking History",
            onClick = onOpenBookingHistory
        )

        DrawerMenuItem(
            icon = Icons.Default.Share,
            text = "Referral Program",
            onClick = {
                onClose()
                navController?.navigate(Screen.Referral.route)
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Star,
            text = "Loyalty Program",
            onClick = {
                onClose()
                navController?.navigate(Screen.Loyalty.route)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        DrawerSectionTitle("Notifications")
        Spacer(modifier = Modifier.height(8.dp))
        DrawerMenuItem(
            icon = Icons.Default.Notifications,
            text = "Notification Settings",
            onClick = {
                onClose()
                navController?.navigate(Screen.NotificationSettings.route)
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Payment,
            text = "My Cards",
            onClick = {
                onClose()
                navController?.navigate(Screen.SavedCards.route)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        DrawerSectionTitle("Theme")
        Spacer(modifier = Modifier.height(8.dp))
        ThemeSelectionRow(currentTheme = currentTheme, onThemeSelected = onThemeSelected)

        Spacer(modifier = Modifier.height(24.dp))

        CurrentVersionLabel()
    }
}

@Composable
fun DrawerSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = drawerTextSize,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 24.dp)
    )
}

@Composable
fun DrawerMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = drawerTextSize,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ThemeSelectionRow(
    currentTheme: com.wheezy.skyflight.core.model.ThemeOption,
    onThemeSelected: (com.wheezy.skyflight.core.model.ThemeOption) -> Unit
) {
    val selectedBorderColor = MaterialTheme.colorScheme.primary
    val unselectedBorderColor = Color.Transparent
    val selectedTextColor = MaterialTheme.colorScheme.onSurface
    val unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(
            com.wheezy.skyflight.core.model.ThemeOption.Auto to "Auto",
            com.wheezy.skyflight.core.model.ThemeOption.Light to "Light",
            com.wheezy.skyflight.core.model.ThemeOption.Dark to "Dark"
        ).forEach { (theme, label) ->
            val isSelected = currentTheme == theme
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) selectedBorderColor else unselectedBorderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onThemeSelected(theme) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) selectedTextColor else unselectedTextColor,
                    fontSize = drawerTextSize,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CurrentVersionLabel() {
    val currentDate = remember { SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date()) }
    Box(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Version: $currentDate",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
    }
}