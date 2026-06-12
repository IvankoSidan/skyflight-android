package com.wheezy.skyflight.feature.loyalty.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheezy.skyflight.core.ui.components.BackButton
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.feature.loyalty.presentation.components.TierCard
import com.wheezy.skyflight.feature.loyalty.presentation.states.PointsBalanceState
import com.wheezy.skyflight.feature.loyalty.presentation.states.TiersState
import com.wheezy.skyflight.feature.loyalty.presentation.states.TransactionsState
import com.wheezy.skyflight.feature.loyalty.presentation.viewmodels.LoyaltyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoyaltyScreen(
    navController: NavController,
    viewModel: LoyaltyViewModel = hiltViewModel()
) {
    val pointsBalanceState by viewModel.pointsBalanceState.collectAsState()
    val transactionsState by viewModel.transactionsState.collectAsState()
    val tiersState by viewModel.tiersState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPointsBalance()
        viewModel.loadTransactions()
        viewModel.loadTiers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loyalty Program", fontWeight = FontWeight.Bold) },
                navigationIcon = { BackButton(onClick = { navController.popBackStack() }) },
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
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        when (val state = pointsBalanceState) {
                            is PointsBalanceState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            is PointsBalanceState.Success -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${state.data.balance}",
                                        style = MaterialTheme.typography.displayLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Points Balance",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = state.data.tier,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "${state.data.cashbackPercent}% cashback",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }

                                    if (state.data.nextTier != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "${state.data.pointsToNextTier} points to ${state.data.nextTier}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        LinearProgressIndicator(
                                            progress = { state.data.progressToNextTier },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(MaterialTheme.shapes.small)
                                        )
                                    }
                                }
                            }
                            is PointsBalanceState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = state.message,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = { viewModel.loadPointsBalance() }) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                when (val state = transactionsState) {
                    is TransactionsState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is TransactionsState.Success -> {
                        if (state.transactions.isEmpty()) {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No transactions yet",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(state.transactions.take(5)) { transaction ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = transaction.type,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = transaction.description ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = transaction.createdAt.take(10),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Text(
                                            text = if (transaction.amount > 0) "+${transaction.amount}" else "${transaction.amount}",
                                            color = if (transaction.amount > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (state.transactions.size > 5) {
                                item {
                                    TextButton(
                                        onClick = {
                                            viewModel.loadTransactions()
                                            navController.navigate("points_history")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("View All Transactions")
                                    }
                                }
                            }
                        }
                    }
                    is TransactionsState.Error -> {
                        item {
                            Column {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                                Button(
                                    onClick = { viewModel.loadTransactions() },
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Membership Tiers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                when (val state = tiersState) {
                    is TiersState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is TiersState.Success -> {
                        items(state.tiers) { tier ->
                            TierCard(tier = tier, isCurrent = false)
                        }
                    }
                    is TiersState.Error -> {
                        item {
                            Column {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                                Button(
                                    onClick = { viewModel.loadTiers() },
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                item {
                    TextButton(
                        onClick = { viewModel.resetAllStates() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Refresh All Data")
                    }
                }
            }
        }
    }
}