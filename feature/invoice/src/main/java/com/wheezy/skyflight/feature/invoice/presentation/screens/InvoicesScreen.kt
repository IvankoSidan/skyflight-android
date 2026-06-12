package com.wheezy.skyflight.feature.invoice.presentation.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheezy.skyflight.core.ui.components.BackButton
import com.wheezy.skyflight.core.ui.components.EmptyStateScreen
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.invoice.presentation.components.InvoiceCard
import com.wheezy.skyflight.feature.invoice.presentation.states.DownloadInvoiceState
import com.wheezy.skyflight.feature.invoice.presentation.states.InvoicesState
import com.wheezy.skyflight.feature.invoice.presentation.viewmodels.InvoiceViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoicesState by viewModel.invoicesState.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val scrollState = rememberLazyListState()

    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInvoices(0)
    }

    // Автоматическая загрузка следующих страниц
    LaunchedEffect(scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index, invoicesState) {
        val state = invoicesState
        if (state is InvoicesState.Success && !isLoadingMore) {
            val lastVisibleIndex = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = state.data.invoices.size

            if (lastVisibleIndex >= totalItems - 3 && currentPage < state.data.totalPages - 1) {
                isLoadingMore = true
                currentPage++
                viewModel.loadInvoices(currentPage)
            }
        }
    }

    LaunchedEffect(invoicesState) {
        isLoadingMore = false
        if (invoicesState is InvoicesState.Success) {
            totalPages = (invoicesState as InvoicesState.Success).data.totalPages
        }
    }

    // Обработка скачивания
    LaunchedEffect(downloadState) {
        when (val state = downloadState) {
            is DownloadInvoiceState.Success -> {
                saveAndOpenPdf(context, state.bytes)
                viewModel.clearDownloadState()
            }
            is DownloadInvoiceState.Error -> {
                SnackbarHelper.showError(state.message)
                viewModel.clearDownloadState()
            }
            else -> {}
        }
    }

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Invoices", fontWeight = FontWeight.Bold) },
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

            when (val state = invoicesState) {
                is InvoicesState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Loading invoices...", fontSize = 12.sp)
                        }
                    }
                }

                is InvoicesState.Error -> {
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
                            Button(onClick = { viewModel.loadInvoices(0) }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is InvoicesState.Success -> {
                    val invoices = state.data.invoices

                    if (invoices.isEmpty()) {
                        EmptyStateScreen(
                            message = "No invoices found.\nInvoices will appear here after your first payment.",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(invoices) { invoice ->
                                InvoiceCard(
                                    invoice = invoice,
                                    onDownloadClick = {
                                        viewModel.downloadInvoice(invoice.id)
                                    }
                                )
                            }

                            if (state.data.currentPage < state.data.totalPages - 1) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
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

private fun saveAndOpenPdf(context: Context, pdfData: ByteArray) {
    try {
        val fileName = "invoice_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(pdfData)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Open PDF"))
    } catch (e: Exception) {
        SnackbarHelper.showError("Failed to save PDF: ${e.message}")
    }
}