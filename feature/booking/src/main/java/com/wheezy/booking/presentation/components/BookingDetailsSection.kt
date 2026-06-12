package com.wheezy.skyflight.feature.booking.presentation.components.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wheezy.skyflight.core.model.FlightModel

@Composable
fun BookingDetailsSection(
    flight: FlightModel,
    bookingDateText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "From", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = flight.departureCity,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Booking date", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = bookingDateText,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = "To", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = flight.arrivalCity,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Flight time", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = flight.departureTime,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}