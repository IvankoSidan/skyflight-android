package com.wheezy.skyflight.feature.cards.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheezy.skyflight.core.ui.components.BackButton
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.feature.cards.presentation.components.CardItem
import com.wheezy.skyflight.feature.cards.presentation.states.DeleteCardState
import com.wheezy.skyflight.feature.cards.presentation.states.SavedCardsState
import com.wheezy.skyflight.feature.cards.presentation.states.SetDefaultCardState
import com.wheezy.skyflight.feature.cards.presentation.viewmodels.CardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCardsScreen(
    navController: NavController,
    viewModel: CardsViewModel = hiltViewModel()
) {
    val savedCardsState by viewModel.savedCardsState.collectAsState()
    val deleteCardState by viewModel.deleteCardState.collectAsState()
    val setDefaultCardState by viewModel.setDefaultCardState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showSetDefaultDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSavedCards()
    }

    LaunchedEffect(deleteCardState) {
        val state = deleteCardState
        if (state is DeleteCardState.Success) {
            viewModel.clearDeleteState()
        }
    }

    LaunchedEffect(setDefaultCardState) {
        val state = setDefaultCardState
        if (state is SetDefaultCardState.Success) {
            viewModel.clearSetDefaultState()
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Card") },
            text = { Text("Are you sure you want to delete this card?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCard(showDeleteDialog!!) {
                            showDeleteDialog = null
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSetDefaultDialog != null) {
        AlertDialog(
            onDismissRequest = { showSetDefaultDialog = null },
            title = { Text("Set Default Card") },
            text = { Text("Do you want to set this card as default for future payments?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setDefaultCard(showSetDefaultDialog!!) {
                            showSetDefaultDialog = null
                        }
                    }
                ) {
                    Text("Set as Default")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetDefaultDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Cards", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
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

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (val currentState = savedCardsState) {
                    is SavedCardsState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is SavedCardsState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = currentState.message,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.loadSavedCards() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is SavedCardsState.Success -> {
                        val cards = currentState.cards
                        if (cards.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No saved cards",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Add a card during checkout to save it here",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(cards) { card ->
                                    CardItem(
                                        card = card,
                                        onDeleteClick = { showDeleteDialog = card.stripePaymentMethodId },
                                        onSetDefaultClick = {
                                            if (!card.isDefault) {
                                                showSetDefaultDialog = card.stripePaymentMethodId
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}