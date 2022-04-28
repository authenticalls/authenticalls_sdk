package com.authenticalls.flashcall_demo.network.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FlashcallRequestBody(
    val msisdn: String,
) : Parcelable