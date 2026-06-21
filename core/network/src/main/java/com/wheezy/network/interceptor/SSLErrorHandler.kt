package com.wheezy.skyflight.core.network.interceptor

import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLException

class SSLErrorHandler {

    fun handleSSLException(throwable: Throwable): String {
        return when (throwable) {
            is SSLPeerUnverifiedException ->
                "Security alert: Certificate mismatch. Please update the app."
            is SSLException ->
                "Connection security error. Please check your connection."
            else -> throwable.message ?: "Network error"
        }
    }

    fun isSSLError(throwable: Throwable): Boolean =
        throwable is SSLPeerUnverifiedException || throwable is SSLException
}