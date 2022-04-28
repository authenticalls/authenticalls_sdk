package com.authenticalls.flashcall_demo

import android.app.Application
import android.os.Build

class FlashcallDemoApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // Detect emulator for testing purposes
        val isProbablyRunningOnEmulator: Boolean by lazy {
            return@lazy ((Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                    && Build.FINGERPRINT.endsWith(":user/release-keys")
                    && Build.MANUFACTURER == "Google" && Build.PRODUCT.startsWith("sdk_gphone_") && Build.BRAND == "google"
                    && Build.MODEL.startsWith("sdk_gphone_"))
                    || Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(
                Build.MANUFACTURER,
                ignoreCase = true
            )
                    || Build.MANUFACTURER.contains("Genymotion")
                    || Build.HOST.startsWith("Build") //MSI App Player
                    || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                    || Build.PRODUCT == "google_sdk" )
        }

        // Initialize API Service
        FlashcallDemoApi.initialize(isProbablyRunningOnEmulator)
    }
}
