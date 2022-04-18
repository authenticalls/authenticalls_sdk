package com.authenticalls.flashcall_sdk.network.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class FlashcallSDKStatus : Parcelable {
    IDLE,
    CALL_AUTO_HANGUP_SUCCESS,
    CALL_AUTO_HANGUP_FAILED,
    CHECK_TIMED_OUT,
    CALL_RINGING,
    CALL_HANGUP,
    CALL_FINISHED,
    WAITING_FOR_FLASHCALL,
}