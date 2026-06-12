package com.wheezy.skyflight.core.ui.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DatePickerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val departureCalendar = remember { Calendar.getInstance() }
    val returnCalendar = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_WEEK, 1) } }
    var departureDate by remember { mutableStateOf(dateFormat.format(departureCalendar.time)) }
    var returnDate by remember { mutableStateOf(dateFormat.format(returnCalendar.time)) }

    Row(modifier = Modifier.fillMaxWidth()) {
        DatePickerItem(
            modifier = modifier.weight(1f),
            dateText = departureDate,
            onDateSelected = { departureDate = it },
            calendar = departureCalendar,
            context = context,
            dateFormat = dateFormat
        )
        Spacer(modifier = Modifier.width(16.dp))
        DatePickerItem(
            modifier = modifier.weight(1f),
            dateText = returnDate,
            onDateSelected = { returnDate = it },
            calendar = returnCalendar,
            context = context,
            dateFormat = dateFormat
        )
    }
}

@Composable
fun DatePickerItem(
    modifier: Modifier = Modifier,
    dateText: String,
    onDateSelected: (String) -> Unit,
    calendar: Calendar,
    context: Context,
    dateFormat: SimpleDateFormat
) {
    GlassCard(
        modifier = modifier.padding(top = 8.dp).height(56.dp).clickable {
            showDatePickerDialog(context, calendar, dateFormat, onDateSelected)
        },
        blurRadius = 15f, tintAlpha = 0.12f, strokeAlpha = 0.15f, cornerRadius = 10.dp, enableGlow = false
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp).size(20.dp)
            )
            Text(
                text = dateText,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

fun showDatePickerDialog(
    context: Context,
    calendar: Calendar,
    dateFormat: SimpleDateFormat,
    onDateSelected: (String) -> Unit
) {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    android.app.DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
        calendar.set(selectedYear, selectedMonth, selectedDay)
        onDateSelected(dateFormat.format(calendar.time))
    }, year, month, day).show()
}