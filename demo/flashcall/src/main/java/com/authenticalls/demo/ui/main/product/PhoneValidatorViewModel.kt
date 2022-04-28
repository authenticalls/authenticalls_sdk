package com.authenticalls.demo.ui.main.product

import android.util.Log
import androidx.lifecycle.*
import com.authenticalls.demo.network.models.*
//import com.authenticalls.flashcall_sdk.network.models.CallDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PhoneValidatorViewModel(private val state: SavedStateHandle) : ViewModel() {
    private val logTag = "PhoneValidatorViewModel"

    // Keep the saved state keys here
    companion object {
        private const val VERIFICATION_ID_IN_PROGRESS = "verificationIdInProgress"
        private const val INTERNAL_VERIFICATION_STATUS = "internalVerificationStatus"
    }

    val verificationIdInProgress: LiveData<String?> = state.getLiveData(VERIFICATION_ID_IN_PROGRESS)

    private val _internalVerificationStatus: MutableLiveData<InternalVerificationStatus> =
        state.getLiveData(INTERNAL_VERIFICATION_STATUS)

    // The external immutable LiveData for the interval status of observed flashcall
    val internalVerificationStatus: LiveData<InternalVerificationStatus>
        get() = _internalVerificationStatus

    // The external immutable LiveData for the interval status of observed flashcall
//    var callDetails: CallDetails? = null

    init {
        // should happen only at first launch or after app cache is cleared
        if (verificationIdInProgress.value == null) {
            setVerificationIdInProgress(null)
        }

        if (_internalVerificationStatus.value == null) {
            setInternalVerificationStatus(InternalVerificationStatus.WAITING_PHONE_NUMBER)
        }
    }

    private fun setVerificationIdInProgress(newState: String?) {
        Log.w(
            logTag,
            "verificationIdInProgress update:" +
                    "${verificationIdInProgress.value} -> $newState"
        )

        state.set(VERIFICATION_ID_IN_PROGRESS, newState)
    }

    private fun setInternalVerificationStatus(newState: InternalVerificationStatus) {
        Log.w(
            logTag,
            "internalVerificationStatus update:" +
                    "${internalVerificationStatus.value} -> $newState"
        )

        state.set(INTERNAL_VERIFICATION_STATUS, newState)
    }

    /**
     * Call this function to create a new Authenticalls flashcall
     * @param body The DTO describing what parameters should be sent to the Authenticalls Web API
     */
    @Throws(RuntimeException::class)
    fun requestFlashcall(body: FlashcallRequestBody) {
        if (FlashcallDemoApiInternal.isEmulator) {
            setVerificationIdInProgress("200")
            setInternalVerificationStatus(InternalVerificationStatus.VERIFICATION_STARTED)
            return
        }

        if (!FlashcallDemoApiInternal.isReady) {
            throw RuntimeException("Flashcall API not initialized")
        }

        setVerificationIdInProgress(null)
        setInternalVerificationStatus(InternalVerificationStatus.REQUESTING_FLASHCALL)

        viewModelScope.launch {

            try {
                val flashcall: FlashcallResponse =
                    FlashcallDemoApiInternal.retrofitService.requestFlashcall(body)

                if (flashcall.error) {
                    setInternalVerificationStatus(InternalVerificationStatus.APPLICATION_ERROR)
                    // _errorCode.value = flashcall.message
                } else {
                    setVerificationIdInProgress(flashcall.verificationId)
                    setInternalVerificationStatus(InternalVerificationStatus.VERIFICATION_STARTED)
                }
            } catch (e: HttpException) {
                Log.e(logTag, "HttpException $e")
                setInternalVerificationStatus(InternalVerificationStatus.SERVICE_UNAVAILABLE)
                // _errorCode.value = e.message.toString()
            } catch (e: Exception) {
                Log.e(logTag, "Exception $e")
                setInternalVerificationStatus(InternalVerificationStatus.SDK_INTERNAL_ERROR)
                // _errorCode.value = ""
            }
        }
    }

    /**
     * Call this function to validate an Authenticalls flashcall
     * @param body The DTO describing what parameters should be sent to the Authenticalls Web API
     */
    @Throws(RuntimeException::class)
    fun validateFlashcall(body: FlashcallValidateBody) {
        if (FlashcallDemoApiInternal.isEmulator) {
            setVerificationIdInProgress(null)
            setInternalVerificationStatus(InternalVerificationStatus.VALIDATION_SUCCESSFULL)
            return
        }

        if (!FlashcallDemoApiInternal.isReady) {
            throw RuntimeException("Flashcall API not initialized")
        }

        setVerificationIdInProgress(null)
        setInternalVerificationStatus(InternalVerificationStatus.VALIDATING_FLASHCALL)

        viewModelScope.launch {

            try {
                val flashcall: FlashcallValidateResponse =
                    FlashcallDemoApiInternal.retrofitService.validateFlashcall(body)

                delay(2000)

                if (flashcall.isValid != null && flashcall.isValid) {
                    setInternalVerificationStatus(InternalVerificationStatus.VALIDATION_SUCCESSFULL)
                } else {
                    setInternalVerificationStatus(InternalVerificationStatus.VALIDATION_FAILED)
                }
            } catch (e: HttpException) {
                Log.e(logTag, "HttpException $e")
                setInternalVerificationStatus(InternalVerificationStatus.SERVICE_UNAVAILABLE)
                // _errorCode.value = e.message.toString()
            } catch (e: Exception) {
                Log.e(logTag, "Exception $e")
                setInternalVerificationStatus(InternalVerificationStatus.SDK_INTERNAL_ERROR)
                // _errorCode.value = ""
            }
        }
    }
}