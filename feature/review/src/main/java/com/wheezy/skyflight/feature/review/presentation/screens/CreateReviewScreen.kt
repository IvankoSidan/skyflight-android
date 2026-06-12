package com.wheezy.skyflight.feature.review.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.ui.components.BackButton
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GradientButton
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.feature.review.presentation.components.RatingStarsInteractive
import com.wheezy.skyflight.feature.review.presentation.states.CreateReviewUiState
import com.wheezy.skyflight.feature.review.presentation.viewmodels.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReviewScreen(
    navController: NavController,
    flight: FlightModel,
    bookingId: Long,
    reviewViewModel: ReviewViewModel = hiltViewModel()
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf(TextFieldValue("")) }
    val createReviewState by reviewViewModel.createReviewState.collectAsState()

    val isLoading = createReviewState is CreateReviewUiState.Loading
    val isSuccess = createReviewState is CreateReviewUiState.Success
    val errorMessage = (createReviewState as? CreateReviewUiState.Error)?.message

    // Очищаем состояние при входе на экран
    LaunchedEffect(Unit) {
        reviewViewModel.clearCreateReviewState()
    }

    // При успешной отправке закрываем экран
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.popBackStack()
        }
    }

    // Очищаем состояние при выходе с экрана
    DisposableEffect(Unit) {
        onDispose {
            reviewViewModel.clearCreateReviewState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Write a Review") },
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Flight info
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = flight.airlineName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = "${flight.departureCity} → ${flight.arrivalCity}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = flight.flightDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Rating stars
                Text(
                    text = "How was your flight?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RatingStarsInteractive(
                    rating = selectedRating,
                    onRatingChanged = { selectedRating = it },
                    size = 40
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Comment field
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Share your experience (optional)") },
                    placeholder = { Text("What did you like? What could be improved?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    isError = comment.text.length > 500
                )

                Text(
                    text = "${comment.text.length}/500",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (comment.text.length > 500) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                GradientButton(
                    onClick = {
                        if (selectedRating > 0 && comment.text.length <= 500) {
                            reviewViewModel.createReview(
                                bookingId = bookingId,
                                rating = selectedRating,
                                comment = comment.text.takeIf { it.isNotBlank() },
                                onSuccess = {}
                            )
                        }
                    },
                    text = when {
                        isLoading -> "Submitting..."
                        isSuccess -> "Success!"
                        else -> "Submit Review"
                    },
                    enabled = selectedRating > 0 && !isLoading && comment.text.length <= 500,
                    padding = 16
                )
            }
        }
    }
}