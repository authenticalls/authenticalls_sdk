package com.authenticalls.demo.network.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class InternalVerificationStatus : Parcelable {
    WAITING_PHONE_NUMBER,
    REQUESTING_FLASHCALL,
    SERVICE_UNAVAILABLE,
    SDK_INTERNAL_ERROR,
    APPLICATION_ERROR,
    VERIFICATION_STARTED,
    VALIDATION_FAILED,
    VALIDATION_SUCCESSFULL,
    VALIDATING_FLASHCALL
}