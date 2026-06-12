package com.wheezy.skyflight.feature.invoice.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheezy.skyflight.core.ui.components.BackButton
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GradientButton
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.feature.invoice.presentation.components.InvoiceSummaryCard
import com.wheezy.skyflight.feature.invoice.presentation.states.DownloadInvoiceState
import com.wheezy.skyflight.feature.invoice.presentation.states.InvoiceDetailState
import com.wheezy.skyflight.feature.invoice.presentation.states.ResendEmailState
import com.wheezy.skyflight.feature.invoice.presentation.viewmodels.InvoiceViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    navController: NavController,
    bookingId: Long,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoiceDetailState by viewModel.invoiceDetailState.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val resendEmailState by viewModel.resendEmailState.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Загружаем инвойс при входе
    LaunchedEffect(bookingId) {
        viewModel.resetInvoices()
        viewModel.loadInvoiceByBookingId(bookingId)
    }

    // Обработка успешной отправки email
    LaunchedEffect(resendEmailState) {
        when (val state = resendEmailState) {
            is ResendEmailState.Success -> {
                showSuccessDialog = true
                delay(2000)
                showSuccessDialog = false
                viewModel.clearResendEmailState()
            }
            is ResendEmailState.Error -> {
                errorMessage = state.message
                delay(3000)
                errorMessage = null
                viewModel.clearResendEmailState()
            }
            else -> {}
        }
    }

    // Обработка скачивания
    LaunchedEffect(downloadState) {
        when (val state = downloadState) {
            is DownloadInvoiceState.Success -> {
                // Скачивание обрабатывается в InvoicesScreen
                viewModel.clearDownloadState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Details", fontWeight = FontWeight.Bold) },
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

            when (val state = invoiceDetailState) {
                is InvoiceDetailState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Loading invoice...", fontSize = 12.sp)
                        }
                    }
                }

                is InvoiceDetailState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                viewModel.resetInvoices()
                                viewModel.loadInvoiceByBookingId(bookingId)
                            }) {
                                Text("Retry")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { navController.popBackStack() }) {
                                Text("Go Back")
                            }
                        }
                    }
                }

                is InvoiceDetailState.Success -> {
                    val invoice = state.invoice

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Информация об инвойсе
                        InvoiceSummaryCard(invoice = invoice)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Кнопки действий
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Кнопка скачивания
                                GradientButton(
                                    onClick = {
                                        viewModel.downloadInvoice(invoice.id)
                                    },
                                    text = if (downloadState is DownloadInvoiceState.Loading) "Downloading..." else "Download PDF",
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    ),
                                    enabled = downloadState !is DownloadInvoiceState.Loading,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Кнопка повторной отправки email
                                GradientButton(
                                    onClick = {
                                        viewModel.resendInvoiceEmail(invoice.bookingId)
                                    },
                                    text = if (resendEmailState is ResendEmailState.Loading) "Sending..." else "Resend Email",
                                    colors = listOf(
                                        MaterialTheme.colorScheme.secondary,
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                    ),
                                    enabled = resendEmailState !is ResendEmailState.Loading,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Сообщение об ошибке
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог успеха
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Email Sent") },
            text = { Text("Invoice has been resent to your email address.") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}