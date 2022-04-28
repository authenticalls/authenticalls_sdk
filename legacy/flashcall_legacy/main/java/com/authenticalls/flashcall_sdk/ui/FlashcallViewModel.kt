package com.authenticalls.flashcall_sdk.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.lifecycle.*
import com.android.internal.telephony.ITelephony
import com.authenticalls.flashcall_sdk.network.models.CallDetails
import com.authenticalls.flashcall_sdk.network.models.FlashcallSDKStatus
import kotlinx.coroutines.*
import java.lang.reflect.Method

/**
 * [FlashcallViewModel] that is attached to [FlashcallFragment].
 * It should be also shared with other fragments that need to create flashcalls
 */
class FlashcallViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {
    private val logTag = "FlashcallViewModel"

    private val context = getApplication<Application>().applicationContext
    private var listener: MyPhoneStateListener? = null
    private var telephony: TelephonyManager? = null

    // Keep the saved state keys here
    companion object {
        private const val INTERNAL_T_STATUS = "internalFlashcallStatus"
    }

    var callDetails: CallDetails? = null

    private val _internalFlashcallStatus: MutableLiveData<FlashcallSDKStatus> =
        state.getLiveData(INTERNAL_T_STATUS)

    // The external immutable LiveData for the interval status of observed flashcall
    val internalFlashcallStatus: LiveData<FlashcallSDKStatus>
        get() = _internalFlashcallStatus

    // BROADCAST RECEIVER HANDLER
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (telephony !== null && listener !== null) {
                println("already_initialized")
                return
            }
            println("initialising")
            addListener(context)
        }

        fun addListener(context: Context) {
            telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            listener = MyPhoneStateListener(context, ::eventUpdate)
            telephony!!.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
        }

        fun removeListener() {
            telephony?.listen(listener, PhoneStateListener.LISTEN_NONE)
            listener = null
            telephony = null
        }

        fun eventUpdate(state: FlashcallSDKStatus, cdr: CallDetails?) {
            if (cdr != null) {
                callDetails = cdr
            }
            _internalFlashcallStatus.value = state
        }
    }

    fun removeListener() {
        try {
            broadcastReceiver.removeListener()
            context.unregisterReceiver(broadcastReceiver)
            Toast.makeText(context, "Stopped listening for calls", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalArgumentException) {
            // Listener already stopped
        }
    }

    fun addListener() {
        context.registerReceiver(
            broadcastReceiver,
            IntentFilter("android.intent.action.PHONE_STATE")
        )
        if (_internalFlashcallStatus.value == FlashcallSDKStatus.IDLE) {
            _internalFlashcallStatus.value = FlashcallSDKStatus.WAITING_FOR_FLASHCALL
        }
        Toast.makeText(context, "Started listening for calls", Toast.LENGTH_SHORT).show()
    }

    fun resetObservedFlashcall() {
        callDetails = null
        _internalFlashcallStatus.value = FlashcallSDKStatus.IDLE
    }

    init {
        _internalFlashcallStatus.value = FlashcallSDKStatus.IDLE
    }

    private class MyPhoneStateListener(
        _context: Context,
        _callback: (state: FlashcallSDKStatus, cdrEntry: CallDetails?) -> Unit
    ) :
        PhoneStateListener() {
        private val context = _context
        private val callback = _callback

        override fun onCallStateChanged(state: Int, callingNumber: String) {
            super.onCallStateChanged(state, callingNumber)
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                callback(FlashcallSDKStatus.CALL_RINGING, null)
                blockTheCall()
            }

            if (state == TelephonyManager.CALL_STATE_IDLE && callingNumber != "") {
                callback(FlashcallSDKStatus.CALL_HANGUP, null)
                getLastCallDetails(callingNumber)
            }
        }

        private fun blockTheCall() {
            var success = true
            GlobalScope.launch {
                suspend {
                    delay(2000)
                    try {
                        val tm: TelephonyManager =
                            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        val c = Class.forName(tm.javaClass.name)
                        val m: Method = c.getDeclaredMethod("getITelephony")
                        m.setAccessible(true)
                        val telephonyService = m.invoke(tm) as ITelephony
                        telephonyService.silenceRinger()
                        telephonyService.endCall()
                    } catch (e: java.lang.Exception) {
                        success = false
                        e.printStackTrace()
                    }
                    withContext(Dispatchers.Main) {
                        if (success) {
                            callback(FlashcallSDKStatus.CALL_AUTO_HANGUP_SUCCESS, null)
                        } else {
                            callback(FlashcallSDKStatus.CALL_AUTO_HANGUP_FAILED, null)
                        }
                    }
                }.invoke()
            }
        }

        private fun getLastCallDetails(callingNumber: String) {
            GlobalScope.launch {
                suspend {
                    delay(2000)
                    val managedCursor: Cursor? = context.contentResolver?.query(
                        CallLog.Calls.CONTENT_URI,
                        null,
                        CallLog.Calls.NUMBER + " = ? ",
                        arrayOf(callingNumber),
                        CallLog.Calls.DATE + " DESC limit 1;"
                    )

                    if (managedCursor !== null) {
                        val number: Int = managedCursor.getColumnIndex(CallLog.Calls.NUMBER)
                        val duration: Int = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
                        val date: Int = managedCursor.getColumnIndex(CallLog.Calls.DATE)

                        if (managedCursor.moveToFirst()) { // added line
                            val phoneNumber: String = managedCursor.getString(number)
                            val callDuration: String = managedCursor.getString(duration)
                            val callDate: String = managedCursor.getString(date)

                            withContext(Dispatchers.Main) {
                                val cdr = CallDetails(
                                    phoneNumber,
                                    callDuration,
                                    callDate,
                                )
                                callback(FlashcallSDKStatus.CALL_FINISHED, cdr)
                            }
                        }
                        managedCursor.close()
                    }
                }.invoke()
            }
        }
    }
}