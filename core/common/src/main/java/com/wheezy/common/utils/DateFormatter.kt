package com.wheezy.skyflight.core.common.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.format(): String {
    return this.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
}