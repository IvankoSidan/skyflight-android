package com.wheezy.skyflight.feature.booking.presentation.components.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wheezy.skyflight.core.model.Booking
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.ui.R
import java.math.BigDecimal

@Composable
fun BookingSeatInfoSection(
    flight: FlightModel,
    booking: Booking,
    totalPrice: BigDecimal,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Class", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = flight.classSeat,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Seats", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = booking.seatNumbers.ifEmpty { "${booking.seatCount} seats" },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Airline", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = flight.airlineName,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Price", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = String.format("$%.2f", totalPrice.toDouble()),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Image(
            painter = painterResource(id = R.drawable.qrcode),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .padding(start = 8.dp)
        )
    }
}