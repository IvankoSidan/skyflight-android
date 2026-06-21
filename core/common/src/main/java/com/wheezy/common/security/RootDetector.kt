package com.wheezy.skyflight.core.common.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootDetector @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val TAG = "RootDetector"
    }

    fun isDeviceCompromised(): Boolean {
        return isRooted() || isDevelopmentBuild() || hasDangerousApps()
    }

    fun isRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        val suPaths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )

        for (path in suPaths) {
            if (File(path).exists()) {
                return true
            }
        }

        try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val result = process.inputStream.bufferedReader().readText()
            if (result.isNotBlank()) {
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for su binary", e)
        }

        return false
    }

    fun isDevelopmentBuild(): Boolean {
        return Build.FINGERPRINT.contains("test-keys") ||
                Build.FINGERPRINT.contains("dev-keys") ||
                Build.FINGERPRINT.contains("debug")
    }

    fun hasDangerousApps(): Boolean {
        val dangerousPackages = listOf(
            "com.topjohnwu.magisk",
            "com.koushikdutta.superuser",
            "eu.chainfire.supersu",
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.kingo.kingouser",
            "com.kingroot.kinguser",
            "com.kingroot.master",
            "me.weishu.kernelsu"
        )

        val packageManager = context.packageManager
        for (packageName in dangerousPackages) {
            try {
                packageManager.getPackageInfo(packageName, 0)
                return true
            } catch (_: PackageManager.NameNotFoundException) {
                // Package not found, continue checking
            }
        }
        return false
    }

    fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.PRODUCT == "sdk" ||
                Build.PRODUCT == "google_sdk" ||
                Build.PRODUCT == "sdk_x86" ||
                Build.PRODUCT == "vbox86p"
    }
}