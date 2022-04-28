package com.authenticalls.flashcall_demo.network.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FlashcallValidateBody(
    val verificationId: String,
    val msisdn: String,
    val duration: String,
    val timestamp: String,
) : Parcelable