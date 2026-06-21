package com.wheezy.skyflight.feature.search.presentation.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.ui.R
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GlassCardDefaults
import com.wheezy.skyflight.navigation.navigateToAirlineReviews

@Composable
fun FlightItem(
    item: FlightModel,
    onFlightClick: (FlightModel) -> Unit,
    navController: NavController? = null,
    imageLoader: ImageLoader
) {
    Log.d("FlightItem", "Loading image: ${item.fullLogoUrl}")

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            .clickable { onFlightClick(item) },
        config = GlassCardDefaults.medium
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (
                logo, timeTxt, airplaneIcon,
                fromCity, fromShort, toCity, toShort,
                dashLine, seatIcon, priceTxt, classTxt, airlineReviewsButton
            ) = createRefs()

            AsyncImage(
                model = item.fullLogoUrl,
                contentDescription = item.airlineName,
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(width = 200.dp, height = 50.dp)
                    .padding(top = 8.dp)
                    .constrainAs(logo) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                onError = { error ->
                    Log.e("FlightItem", "Error loading image: ${error.result.throwable}")
                }
            )

            Text(
                text = item.arriveTime,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .constrainAs(timeTxt) {
                        top.linkTo(logo.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )

            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.line_airple_blue),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(width = 210.dp, height = 37.dp)
                    .constrainAs(airplaneIcon) {
                        top.linkTo(timeTxt.bottom)
                        centerHorizontallyTo(parent)
                    }
            )

            Text(
                text = item.departureCity,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(fromCity) {
                        top.linkTo(airplaneIcon.top)
                        start.linkTo(parent.start)
                        end.linkTo(airplaneIcon.start)
                        width = Dimension.fillToConstraints
                    }
            )

            Text(
                text = item.departureShort,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.constrainAs(fromShort) {
                    top.linkTo(fromCity.bottom)
                    bottom.linkTo(airplaneIcon.bottom)
                    start.linkTo(fromCity.start)
                    end.linkTo(fromCity.end)
                }
            )

            Text(
                text = item.arrivalCity,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(toCity) {
                        top.linkTo(airplaneIcon.top)
                        start.linkTo(airplaneIcon.end)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
            )

            Text(
                text = item.arrivalShort,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.constrainAs(toShort) {
                    top.linkTo(toCity.bottom)
                    bottom.linkTo(airplaneIcon.bottom)
                    start.linkTo(toCity.start)
                    end.linkTo(toCity.end)
                }
            )

            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.dash_line),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(45.dp)
                    .constrainAs(dashLine) {
                        top.linkTo(airplaneIcon.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )

            Icon(
                imageVector = Icons.Default.Chair,
                contentDescription = "Seat",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(45.dp)
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    .constrainAs(seatIcon) {
                        top.linkTo(dashLine.bottom)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
            )

            Text(
                text = "$${"%.2f".format(item.price)}",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .constrainAs(priceTxt) {
                        top.linkTo(seatIcon.top)
                        bottom.linkTo(seatIcon.bottom)
                        end.linkTo(parent.end)
                    }
            )

            Text(
                text = item.classSeat,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .constrainAs(classTxt) {
                        top.linkTo(seatIcon.top)
                        bottom.linkTo(seatIcon.bottom)
                        start.linkTo(seatIcon.end)
                    }
            )

            if (navController != null) {
                TextButton(
                    onClick = {
                        navController.navigateToAirlineReviews(item.airlineName)
                    },
                    modifier = Modifier
                        .constrainAs(airlineReviewsButton) {
                            top.linkTo(dashLine.bottom)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                        }
                ) {
                    Text("Reviews", fontSize = 11.sp)
                }
            }
        }
    }
}