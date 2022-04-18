package com.authenticalls.flashcall_sdk.network.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CallDetails(
    val msisdn: String,
    val duration: String,
    val timestamp: String,
) : Parcelable

