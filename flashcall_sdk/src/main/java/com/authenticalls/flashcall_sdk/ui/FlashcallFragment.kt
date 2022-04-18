package com.authenticalls.flashcall_sdk.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.authenticalls.flashcall_sdk.R
import com.authenticalls.flashcall_sdk.databinding.FlashcallFragmentBinding
import com.authenticalls.flashcall_sdk.network.models.FlashcallSDKStatus

class FlashcallFragment : Fragment() {
    private val logTag = "FlashcallFragment"

    /**
     * Use the 'by activityViewModels()' Kotlin property delegate
     * from the fragment-ktx artifact
     */
    private val viewModel: FlashcallViewModel by activityViewModels()

    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the FlashcallFragment
     * to enable Data Binding to observe LiveData
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // This creates an instance of the FlashcallFragmentBinding binding class for the fragment to use
        val binding = FlashcallFragmentBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the FlashcallViewModel
        binding.viewModel = viewModel

        // This callback will only be called when Fragment is at least Started.
        // This sets back button behavior
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    viewModel.resetObservedFlashcall()
                    view?.findNavController()?.popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        viewModel.internalFlashcallStatus.observe(viewLifecycleOwner) { newState: FlashcallSDKStatus ->

            val flashcallProgress = view?.findViewById<TextView>(R.id.flashcallProgress)

            if (newState == FlashcallSDKStatus.WAITING_FOR_FLASHCALL) {
                if (flashcallProgress != null) {
                    flashcallProgress.text = "Waiting for call..."
                }
            }

            if (newState == FlashcallSDKStatus.CALL_FINISHED) {
                if (flashcallProgress != null) {
                    flashcallProgress.text = "Call details available..."
                    Handler(Looper.getMainLooper()).postDelayed({
                        view?.findNavController()?.popBackStack()
                    }, 1000)
                }
            }

            if (newState == FlashcallSDKStatus.CALL_RINGING) {
                if (flashcallProgress != null) {
                    flashcallProgress.text = "Ringing..."
                }
            }

            if (newState == FlashcallSDKStatus.CALL_HANGUP) {
                if (flashcallProgress != null) {
                    flashcallProgress.text = "Hanged up..."
                }
            }

            if (newState == FlashcallSDKStatus.CHECK_TIMED_OUT) {
                if (flashcallProgress != null) {
                    flashcallProgress.text = "Timed out..."
                }
            }

        }

        viewModel.addListener()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()
    }

}