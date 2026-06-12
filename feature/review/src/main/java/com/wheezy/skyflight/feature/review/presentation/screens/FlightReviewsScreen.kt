package com.wheezy.skyflight.feature.review.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheezy.skyflight.core.model.Review
import com.wheezy.skyflight.core.ui.components.BackButton
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GlassCardDefaults
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.feature.review.presentation.components.RatingDistributionChart
import com.wheezy.skyflight.feature.review.presentation.components.RatingStars
import com.wheezy.skyflight.feature.review.presentation.states.AirlineRatingUiState
import com.wheezy.skyflight.feature.review.presentation.states.ReviewsPageState
import com.wheezy.skyflight.feature.review.presentation.viewmodels.ReviewViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightReviewsScreen(
    navController: NavController,
    flightId: Long,
    airlineName: String,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val reviewsPageState by viewModel.reviewsPageState.collectAsState()
    val airlineRatingState by viewModel.airlineRatingState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val scrollState = rememberLazyListState()

    var currentPage by remember { mutableIntStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }

    LaunchedEffect(airlineName) {
        viewModel.loadAirlineRating(airlineName)
    }

    LaunchedEffect(flightId) {
        viewModel.resetReviewsPage()
        currentPage = 0
        viewModel.loadFlightReviewsPaginated(flightId, 0)
    }

    LaunchedEffect(scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index, reviewsPageState) {
        val state = reviewsPageState
        if (state is ReviewsPageState.Success && !isLoadingMore) {
            val lastVisibleIndex = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = state.reviews.size

            if (lastVisibleIndex >= totalItems - 3 && currentPage < state.totalPages - 1) {
                isLoadingMore = true
                currentPage++
                viewModel.loadFlightReviewsPaginated(flightId, currentPage)
            }
        }
    }

    LaunchedEffect(reviewsPageState) {
        isLoadingMore = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Reviews",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = airlineName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
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

            when (val state = reviewsPageState) {
                is ReviewsPageState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ReviewsPageState.Error -> {
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
                                viewModel.resetReviewsPage()
                                currentPage = 0
                                viewModel.loadFlightReviewsPaginated(flightId, 0)
                                viewModel.loadAirlineRating(airlineName)
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is ReviewsPageState.Success -> {
                    val reviews = state.reviews

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            when (val ratingState = airlineRatingState) {
                                is AirlineRatingUiState.Loading -> {
                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        config = GlassCardDefaults.medium
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Loading rating...", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                                is AirlineRatingUiState.Success -> {
                                    val airlineRating = ratingState.rating
                                    if (airlineRating.totalReviews > 0) {
                                        GlassCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            config = GlassCardDefaults.medium
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = airlineRating.averageRatingFormatted,
                                                    style = MaterialTheme.typography.displayLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )

                                                RatingStars(
                                                    rating = airlineRating.starsCount,
                                                    size = 24
                                                )

                                                Text(
                                                    text = "Based on ${airlineRating.totalReviews} reviews",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Spacer(modifier = Modifier.height(16.dp))

                                                Text(
                                                    text = "Rating Distribution",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                RatingDistributionChart(
                                                    ratingDistribution = airlineRating.ratingDistribution,
                                                    totalReviews = airlineRating.totalReviews
                                                )
                                            }
                                        }
                                    } else {
                                        GlassCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            config = GlassCardDefaults.medium
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "No reviews yet",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "Be the first to review this airline!",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                is AirlineRatingUiState.Error -> {
                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        config = GlassCardDefaults.medium
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = ratingState.message,
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            TextButton(onClick = {
                                                viewModel.loadAirlineRating(airlineName)
                                            }) {
                                                Text("Retry")
                                            }
                                        }
                                    }
                                }
                                else -> Unit
                            }
                        }

                        if (reviews.isEmpty() && (airlineRatingState is AirlineRatingUiState.Success && (airlineRatingState as AirlineRatingUiState.Success).rating.totalReviews == 0)) {
                            // Already showed "No reviews yet"
                        } else if (reviews.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Loading reviews...", fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            items(
                                items = reviews,
                                key = { it.id }
                            ) { review ->
                                ReviewCard(
                                    review = review,
                                    dateFormatter = dateFormatter
                                )
                            }

                            if (state.currentPage < state.totalPages - 1) {
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

@Composable
fun ReviewCard(
    review: Review,
    dateFormatter: DateTimeFormatter,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = review.userName ?: "Anonymous",
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                RatingStars(
                    rating = review.rating,
                    size = 16
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.createdAt.format(dateFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val comment = review.comment
            if (!comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}