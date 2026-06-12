package com.wheezy.skyflight.feature.booking.presentation.components.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.ImageLoader
import coil.compose.AsyncImage
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.ui.R

@Composable
fun BookingHeaderSection(
    flight: FlightModel,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val (logo, arrivalText, lineImg, fromCity, fromShort, toCity, toShort) = createRefs()

        AsyncImage(
            model = flight.fullLogoUrl,
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(width = 200.dp, height = 50.dp)
                .constrainAs(logo) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = flight.arriveTime,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.constrainAs(arrivalText) {
                top.linkTo(logo.bottom, margin = 8.dp)
                centerHorizontallyTo(parent)
            }
        )

        Image(
            painter = painterResource(id = R.drawable.line_airple_blue),
            contentDescription = null,
            modifier = Modifier
                .size(width = 210.dp, height = 37.dp)
                .constrainAs(lineImg) {
                    top.linkTo(arrivalText.bottom, margin = 8.dp)
                    centerHorizontallyTo(parent)
                }
        )

        Text(
            text = flight.departureCity,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.constrainAs(fromCity) {
                top.linkTo(lineImg.top)
                end.linkTo(lineImg.start)
                start.linkTo(parent.start)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = flight.departureShort,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.constrainAs(fromShort) {
                top.linkTo(fromCity.bottom)
                bottom.linkTo(lineImg.bottom)
                centerHorizontallyTo(fromCity)
            }
        )

        Text(
            text = flight.arrivalCity,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.constrainAs(toCity) {
                top.linkTo(lineImg.top)
                start.linkTo(lineImg.end)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = flight.arrivalShort,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.constrainAs(toShort) {
                top.linkTo(toCity.bottom)
                bottom.linkTo(lineImg.bottom)
                centerHorizontallyTo(toCity)
            }
        )
    }
}