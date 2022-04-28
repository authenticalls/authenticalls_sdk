package com.authenticalls.demo.network.models

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class FlashcallValidateResponse(
    val error: Boolean,
    val isValid: Boolean?,
) : Parcelable