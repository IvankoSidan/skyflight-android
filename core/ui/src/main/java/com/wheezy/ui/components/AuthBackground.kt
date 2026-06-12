package com.wheezy.skyflight.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wheezy.skyflight.core.ui.R

@Composable
fun AuthBackground(content: @Composable ColumnScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.shape),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .align(Alignment.TopCenter)
        )

        GlassCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
                .align(Alignment.Center),
            config = GlassCardDefaults.heavy.copy(
                blurRadius = 35f,
                enableGlow = true,
                strokeAlpha = 0.25f
            )
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                config = GlassCardDefaults.light.copy(
                    enableGlow = false,
                    tintAlpha = 0.15f
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    content = content
                )
            }
        }
    }
}