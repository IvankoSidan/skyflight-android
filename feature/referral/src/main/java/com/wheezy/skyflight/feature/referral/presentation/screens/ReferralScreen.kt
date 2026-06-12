package com.wheezy.skyflight.feature.referral.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheezy.skyflight.core.ui.components.BackButton
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GradientButton
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.referral.presentation.states.ApplyReferralState
import com.wheezy.skyflight.feature.referral.presentation.states.ReferralCodeState
import com.wheezy.skyflight.feature.referral.presentation.states.ReferralInfoState
import com.wheezy.skyflight.feature.referral.presentation.viewmodels.ReferralViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralScreen(
    navController: NavController,
    viewModel: ReferralViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val referralCodeState by viewModel.referralCodeState.collectAsState()
    val referralInfoState by viewModel.referralInfoState.collectAsState()
    val applyReferralState by viewModel.applyReferralState.collectAsState()

    var showApplyDialog by remember { mutableStateOf(false) }
    var referralCodeInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("Enter Referral Code") },
            text = {
                OutlinedTextField(
                    value = referralCodeInput,
                    onValueChange = { referralCodeInput = it.uppercase() },
                    label = { Text("Referral code") },
                    placeholder = { Text("e.g., ABC12345") },
                    singleLine = true,
                    isError = applyReferralState is ApplyReferralState.Error,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (referralCodeInput.isNotBlank()) {
                            viewModel.applyReferralCode(referralCodeInput)
                            showApplyDialog = false
                        }
                    },
                    enabled = referralCodeInput.isNotBlank() && applyReferralState !is ApplyReferralState.Loading
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showApplyDialog = false
                    referralCodeInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Referral Program",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            WorldBackground(modifier = Modifier.align(Alignment.TopCenter))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Your Referral Code Section
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your Referral Code",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            when (val state = referralCodeState) {
                                is ReferralCodeState.Loading -> {
                                    CircularProgressIndicator()
                                }
                                is ReferralCodeState.Success -> {
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = state.data.code,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 4.sp,
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Text(
                                            text = "Used: ${state.data.usageCount}/${state.data.maxUses}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if (state.data.isValid) {
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            ) {
                                                Text(
                                                    text = "Active",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        } else {
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                            ) {
                                                Text(
                                                    text = "Expired",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        GradientButton(
                                            onClick = { viewModel.shareCode(context) },
                                            text = "Share",
                                            modifier = Modifier.weight(1f),
                                            padding = 8
                                        )
                                        GradientButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(state.data.code))
                                                SnackbarHelper.showInfo("Copied to clipboard")
                                            },
                                            text = "Copy",
                                            modifier = Modifier.weight(1f),
                                            padding = 8,
                                            colors = listOf(
                                                MaterialTheme.colorScheme.secondary,
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                            )
                                        )
                                    }
                                }
                                is ReferralCodeState.Error -> {
                                    Text(
                                        text = state.message,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { viewModel.loadReferralCode() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                }

                // How it works
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "How it works",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                HowItWorksItem(
                                    icon = Icons.Default.PersonAdd,
                                    text = "Invite friends"
                                )
                                HowItWorksItem(
                                    icon = Icons.Default.Code,
                                    text = "Share your code"
                                )
                                HowItWorksItem(
                                    icon = Icons.Default.Discount,
                                    text = "Get discount"
                                )
                            }
                        }
                    }
                }

                // Apply Referral Code Button
                item {
                    GradientButton(
                        onClick = { showApplyDialog = true },
                        text = "Have a referral code? Apply here",
                        padding = 16
                    )
                }

                // Your Referrals List
                item {
                    Text(
                        text = "Your Referrals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                when (val state = referralInfoState) {
                    is ReferralInfoState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is ReferralInfoState.Success -> {
                        val info = state.data

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCard(
                                    value = info.totalReferrals.toString(),
                                    label = "Friends Joined",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    value = "$${info.totalDiscountEarned / 100}",
                                    label = "Discount Earned",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (info.myReferrals.isEmpty()) {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No referrals yet.\nShare your code to invite friends!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(info.myReferrals) { referral ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = referral.email,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = referral.registeredAt,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = if (referral.status == "COMPLETED")
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = if (referral.status == "COMPLETED") "Completed" else "Pending",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is ReferralInfoState.Error -> {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = state.message,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { viewModel.loadReferrals() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HowItWorksItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.padding(4.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}