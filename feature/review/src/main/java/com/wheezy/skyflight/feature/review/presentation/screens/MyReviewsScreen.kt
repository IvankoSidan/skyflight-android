package com.wheezy.skyflight.feature.review.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheezy.skyflight.core.model.Review
import com.wheezy.skyflight.core.ui.components.BackButton
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GlassCardDefaults
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.feature.review.presentation.components.RatingStars
import com.wheezy.skyflight.feature.review.presentation.components.RatingStarsInteractive
import com.wheezy.skyflight.feature.review.presentation.states.DeleteReviewUiState
import com.wheezy.skyflight.feature.review.presentation.states.ReviewsUiState
import com.wheezy.skyflight.feature.review.presentation.states.UpdateReviewUiState
import com.wheezy.skyflight.feature.review.presentation.viewmodels.ReviewViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    navController: NavController,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val myReviewsState by viewModel.myReviewsState.collectAsState()
    val deleteState by viewModel.deleteReviewState.collectAsState()
    val updateState by viewModel.updateReviewState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm") }

    var editingReview by remember { mutableStateOf<Review?>(null) }
    var editRating by remember { mutableIntStateOf(0) }
    var editComment by remember { mutableStateOf(TextFieldValue("")) }
    var showDeleteDialog by remember { mutableStateOf<Review?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadMyReviews()
    }

    LaunchedEffect(updateState) {
        if (updateState is UpdateReviewUiState.Success) {
            editingReview = null
            viewModel.loadMyReviews()
            viewModel.clearUpdateReviewState()
        }
    }

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteReviewUiState.Success) {
            showDeleteDialog = null
            viewModel.loadMyReviews()
            viewModel.clearDeleteReviewState()
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Review") },
            text = { Text("Are you sure you want to delete this review? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { review ->
                            viewModel.deleteReview(review.id) {
                                // handled by LaunchedEffect
                            }
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

    if (editingReview != null) {
        AlertDialog(
            onDismissRequest = { editingReview = null },
            title = { Text("Edit Review") },
            text = {
                Column {
                    Text("Rating", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    RatingStarsInteractive(
                        rating = editRating,
                        onRatingChanged = { editRating = it },
                        size = 32
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editComment,
                        onValueChange = { editComment = it },
                        label = { Text("Comment (optional)") },
                        placeholder = { Text("Share your experience...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                    Text(
                        text = "${editComment.text.length}/500",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (editComment.text.length > 500) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editRating > 0 && editComment.text.length <= 500) {
                            viewModel.updateReview(
                                reviewId = editingReview!!.id,
                                rating = editRating,
                                comment = editComment.text.takeIf { it.isNotBlank() },
                                onSuccess = {}
                            )
                        }
                    },
                    enabled = editRating > 0 && updateState !is UpdateReviewUiState.Loading
                ) {
                    if (updateState is UpdateReviewUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { editingReview = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Reviews", fontWeight = FontWeight.Bold) },
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

            when (val state = myReviewsState) {
                is ReviewsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ReviewsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadMyReviews() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is ReviewsUiState.Success -> {
                    if (state.reviews.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No reviews yet",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Your reviews will appear here",
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
                            items(
                                items = state.reviews,
                                key = { it.id }
                            ) { review ->
                                MyReviewCard(
                                    review = review,
                                    dateFormatter = dateFormatter,
                                    onEditClick = {
                                        editingReview = review
                                        editRating = review.rating
                                        editComment = TextFieldValue(review.comment ?: "")
                                    },
                                    onDeleteClick = {
                                        showDeleteDialog = review
                                    },
                                    canEdit = review.canEdit
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyReviewCard(
    review: Review,
    dateFormatter: DateTimeFormatter,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    canEdit: Boolean,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        config = GlassCardDefaults.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.airlineName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = review.createdAt.format(dateFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                RatingStars(
                    rating = review.rating,
                    size = 20
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Booking #${review.bookingId}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val commentText = review.comment
            if (!commentText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = commentText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (canEdit) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⏱️ Editing period has expired (24 hours)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 10.sp
                )
            }
        }
    }
}