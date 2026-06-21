package com.wheezy.skyflight.core.common.utils

import java.time.Duration

object DelayConstants {
    val SHORT_DELAY: Duration = Duration.ofMillis(100)
    val LONG_DELAY: Duration = Duration.ofSeconds(1)
    val VERY_LONG_DELAY: Duration = Duration.ofSeconds(5)
    val EXTRA_LONG_DELAY: Duration = Duration.ofSeconds(30)
    val HEARTBEAT_INTERVAL: Duration = Duration.ofSeconds(15)
    val RECONNECT_DELAY: Duration = Duration.ofSeconds(5)
}