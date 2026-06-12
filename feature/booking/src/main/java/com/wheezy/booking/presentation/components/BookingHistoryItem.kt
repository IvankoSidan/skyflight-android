package com.wheezy.skyflight.feature.booking.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.wheezy.skyflight.core.common.utils.DebounceHelper
import com.wheezy.skyflight.core.common.utils.statusColor
import com.wheezy.skyflight.core.model.Booking
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.canBeCancelled
import com.wheezy.skyflight.core.model.canBeDeleted
import com.wheezy.skyflight.core.model.canBePaid
import com.wheezy.skyflight.core.ui.R
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GlassCardDefaults
import com.wheezy.skyflight.feature.booking.presentation.components.booking.*
import java.time.format.DateTimeFormatter

@Composable
fun BookingHistoryItem(
    flight: FlightModel,
    booking: Booking,
    onCancelClick: (Booking) -> Unit,
    onPayClick: (Booking) -> Unit,
    onDeleteClick: (Booking) -> Unit,
    onReviewClick: (() -> Unit)? = null,
    onInvoiceClick: (() -> Unit)? = null,
    showReviewButton: Boolean = false,
    showInvoiceButton: Boolean = false,
    isPaying: Boolean,
    isLoading: Boolean,
    paymentError: String? = null,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = ImageLoader.Builder(LocalContext.current).build(),
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showPayDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val payDebounce = remember { DebounceHelper(500L) }
    val cancelDebounce = remember { DebounceHelper(500L) }
    val deleteDebounce = remember { DebounceHelper(500L) }

    val totalPrice = calculateTotalPrice(flight.price, booking.seatCount)
    val statusColor = booking.status.statusColor()
    val statusText = getStatusText(booking.status)

    val bookingDateText = remember(booking.bookingDate) {
        try {
            booking.bookingDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
        } catch (e: Exception) {
            booking.bookingDate.toString()
        }
    }

    // Диалоги
    PayDeleteDialog(
        visible = showPayDialog && booking.status.canBePaid(),
        onDismiss = { showPayDialog = false },
        onPayClick = {
            showPayDialog = false
            payDebounce.debounce { onPayClick(booking) }
        },
        onDeleteClick = {
            showPayDialog = false
            deleteDebounce.debounce { onDeleteClick(booking) }
        }
    )

    CancelDialog(
        visible = showCancelDialog && booking.status.canBeCancelled(),
        onDismiss = { showCancelDialog = false },
        onConfirm = {
            showCancelDialog = false
            cancelDebounce.debounce { onCancelClick(booking) }
        }
    )

    DeleteDialog(
        visible = showDeleteDialog && booking.status.canBeDeleted(),
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            showDeleteDialog = false
            deleteDebounce.debounce { onDeleteClick(booking) }
        }
    )

    GlassCard(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable(enabled = booking.status.canBePaid()) {
                if (booking.status.canBePaid()) showPayDialog = true
            },
        config = GlassCardDefaults.medium
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Левая цветная полоска статуса
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                bottomStart = 16.dp,
                                topEnd = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                        .background(statusColor)
                )

                // Основной контент
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                ) {
                    // Шапка с логотипом и маршрутом
                    BookingHeaderSection(flight, imageLoader)

                    // Детали рейса
                    BookingDetailsSection(flight, bookingDateText)

                    // Разделитель
                    Image(
                        painter = painterResource(id = R.drawable.dash_line),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )

                    // Информация о местах, классе, цене
                    BookingSeatInfoSection(flight, booking, totalPrice)

                    // Разделитель
                    Image(
                        painter = painterResource(id = R.drawable.dash_line),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )

                    // Статус и кнопки
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BookingStatusSection(statusColor, statusText)

                        BookingActionButtons(
                            booking = booking,
                            showReviewButton = showReviewButton,
                            showInvoiceButton = showInvoiceButton,
                            isPaying = isPaying,
                            isLoading = isLoading,
                            onShareClick = { shareBooking(context, flight, booking, statusText) },
                            onCopyClick = { copyBookingInfo(clipboardManager, flight, booking, statusText) },
                            onReviewClick = onReviewClick,
                            onInvoiceClick = onInvoiceClick,
                            onPayClick = { showPayDialog = true },
                            onCancelClick = { showCancelDialog = true },
                            onDeleteClick = { showDeleteDialog = true }
                        )
                    }

                    // Ошибка платежа
                    if (paymentError != null && !isLoading && !isPaying) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = paymentError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Штрихкод внизу
                    Image(
                        painter = painterResource(id = R.drawable.barcode),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
    }
}