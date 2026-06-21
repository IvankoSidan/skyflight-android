package com.wheezy.skyflight.feature.review.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.wheezy.skyflight.feature.review.presentation.components.AverageRatingDisplay
import com.wheezy.skyflight.feature.review.presentation.components.RatingDistributionChart
import com.wheezy.skyflight.feature.review.presentation.components.RatingStars
import com.wheezy.skyflight.feature.review.presentation.states.ReviewsPageState
import com.wheezy.skyflight.feature.review.presentation.viewmodels.ReviewViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirlineReviewsScreen(
    navController: NavController,
    airlineName: String,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val airlineReviewsPageState by viewModel.airlineReviewsPageState.collectAsState()
    val airlineRatingState by viewModel.airlineRatingState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    var currentPage by remember { mutableIntStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }

    LaunchedEffect(airlineName) {
        viewModel.loadAirlineRating(airlineName)
    }

    LaunchedEffect(Unit) {
        viewModel.resetAirlineReviewsPage()
        viewModel.loadAirlineReviewsPaginated(airlineName, 0, 10)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Airline Reviews", fontWeight = FontWeight.Bold)
                        Text(airlineName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    BackButton(
                        onClick = { navController.popBackStack() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            WorldBackground(modifier = Modifier.align(Alignment.TopCenter))

            when (val state = airlineReviewsPageState) {
                is ReviewsPageState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Loading reviews...", fontSize = 12.sp)
                        }
                    }
                }
                is ReviewsPageState.Error -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.resetAirlineReviewsPage()
                                    viewModel.loadAirlineReviewsPaginated(airlineName, 0, 10)
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is ReviewsPageState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            when (val ratingState = airlineRatingState) {
                                is com.wheezy.skyflight.feature.review.presentation.states.AirlineRatingUiState.Success -> {
                                    val airlineRating = ratingState.rating
                                    if (airlineRating.totalReviews > 0) {
                                        GlassCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            config = GlassCardDefaults.medium
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                AverageRatingDisplay(airlineRating)
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    "Rating Distribution",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                RatingDistributionChart(
                                                    airlineRating.ratingDistribution,
                                                    airlineRating.totalReviews
                                                )
                                            }
                                        }
                                    } else {
                                        GlassCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            config = GlassCardDefaults.medium
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                    Text(
                                                        "No reviews yet",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        "Be the first!",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                is com.wheezy.skyflight.feature.review.presentation.states.AirlineRatingUiState.Loading -> {
                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        config = GlassCardDefaults.medium
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }

                        if (state.reviews.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No reviews available",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(state.reviews) { review ->
                                AirlineReviewCard(review, dateFormatter)
                            }
                        }

                        if (state.currentPage < state.totalPages - 1) {
                            item {
                                Button(
                                    onClick = {
                                        isLoadingMore = true
                                        currentPage++
                                        viewModel.loadAirlineReviewsPaginated(airlineName, currentPage, 10)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isLoadingMore
                                ) {
                                    if (isLoadingMore) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    } else {
                                        Text("Load More")
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
fun AirlineReviewCard(review: Review, dateFormatter: DateTimeFormatter, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth(), config = GlassCardDefaults.medium) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(review.userName ?: "Anonymous", fontWeight = FontWeight.Medium)
                }
                RatingStars(review.rating, size = 16)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                review.createdAt.format(dateFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            review.comment?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}