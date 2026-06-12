package com.wheezy.skyflight.core.common.provider

interface TokenProvider {
    fun getToken(): String?
}