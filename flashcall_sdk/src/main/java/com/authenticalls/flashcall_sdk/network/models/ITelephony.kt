package com.android.internal.telephony;

interface ITelephony {
    fun silenceRinger()
    fun endCall(): Boolean
}