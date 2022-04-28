package com.authenticalls.flashcall_demo.network.models

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class FlashcallResponse(
    val error: Boolean,
    val verificationId: String?,
) : Parcelable