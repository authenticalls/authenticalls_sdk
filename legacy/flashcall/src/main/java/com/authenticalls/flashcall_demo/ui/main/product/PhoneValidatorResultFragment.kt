package com.authenticalls.flashcall_demo.ui.main.product

import FlashcallDemoApiInternal
import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.authenticalls.flashcall_demo.R
import com.authenticalls.flashcall_demo.databinding.PhoneValidatorResultFragmentBinding
import com.authenticalls.flashcall_demo.network.models.FlashcallValidateBody
import com.authenticalls.flashcall_demo.network.models.InternalVerificationStatus
import com.authenticalls.flashcall_sdk.ui.FlashcallViewModel
import kotlinx.coroutines.launch


class PhoneValidatorResultFragment : Fragment() {
    private val logTag = "PhoneValidatorFragment"

    private val viewModel: PhoneValidatorViewModel by activityViewModels()

    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the [PhoneValidatorResultFragment]
     * to enable Data Binding to observe LiveData, and sets up the RecyclerView with an adapter.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Set title
        if (FlashcallDemoApiInternal.isEmulator) activity?.title =
            "Phone Validator Demo (E)" else activity?.title = "Phone Validator Demo"

        // This creates an instance of the PhoneValidatorFragmentBinding binding class for the fragment to use
        val binding = PhoneValidatorResultFragmentBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Set animation max frame not to disappear after animation
        binding.validationSuccessfullIcon.setMaxFrame(104);

        // This callback will only be called when Fragment is at least Started.
        // This sets back button behavior
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    view?.findNavController()
                        ?.navigate(R.id.action_phoneValidatorFragment_to_landingFragment)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // react to InternalVerificationStatus variable change
        viewModel.internalVerificationStatus.observe(viewLifecycleOwner) { newState: InternalVerificationStatus ->
            Log.w(logTag, "new InternalVerificationStatus $newState ")

            // Validation fail interface update
            if (newState == InternalVerificationStatus.VALIDATION_FAILED) {
                val validationProgressText =
                    view?.findViewById<TextView>(R.id.validationProgressText)
                val validationProgressIcon =
                    view?.findViewById<LottieAnimationView>(R.id.validationProgressIcon)
                val validationFailedIcon =
                    view?.findViewById<LottieAnimationView>(R.id.validationFailedIcon)
                val validationFailedText = view?.findViewById<TextView>(R.id.validationFailedText)
                validationProgressText?.visibility = View.GONE
                validationProgressIcon?.visibility = View.GONE
                validationFailedIcon?.visibility = View.VISIBLE
                validationFailedText?.visibility = View.VISIBLE
            }

            // Validation in progress interface update
            if (newState == InternalVerificationStatus.VALIDATING_FLASHCALL) {
                val validationProgressText =
                    view?.findViewById<TextView>(R.id.validationProgressText)
                val validationProgressIcon =
                    view?.findViewById<LottieAnimationView>(R.id.validationProgressIcon)
                validationProgressText?.visibility = View.VISIBLE
                validationProgressIcon?.visibility = View.VISIBLE
            }

            // Validation success interface update
            if (newState == InternalVerificationStatus.VALIDATION_SUCCESSFULL) {
                val validationProgressText =
                    view?.findViewById<TextView>(R.id.validationProgressText)
                val validationProgressIcon =
                    view?.findViewById<LottieAnimationView>(R.id.validationProgressIcon)
                val validationSuccessfullIcon =
                    view?.findViewById<LottieAnimationView>(R.id.validationSuccessfullIcon)
                val validationSuccessfullText =
                    view?.findViewById<TextView>(R.id.validationSuccessfullText)
                validationProgressText?.visibility = View.GONE
                validationProgressIcon?.visibility = View.GONE
                validationSuccessfullIcon?.visibility = View.VISIBLE
                validationSuccessfullText?.visibility = View.VISIBLE
            }

            // Application responded with error code
            if (newState == InternalVerificationStatus.APPLICATION_ERROR) {
                Toast.makeText(context, "Application communication error", Toast.LENGTH_SHORT)
                    .show()
            }

            // Application is not reachable
            if (newState == InternalVerificationStatus.SERVICE_UNAVAILABLE) {
                Toast.makeText(context, "Server communication error", Toast.LENGTH_SHORT)
                    .show()
            }

            // Application responded with ungandled error
            if (newState == InternalVerificationStatus.SDK_INTERNAL_ERROR) {
                Toast.makeText(context, "SDK internal error", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // try to get verificationId
        val verificationId = viewModel.verificationIdInProgress.value
        if (verificationId === null) {
            return
        }

        // prepare body for validation
        val body = FlashcallValidateBody(
            verificationId,
            viewModel.callDetails!!.msisdn,
            viewModel.callDetails!!.duration,
            viewModel.callDetails!!.timestamp
        )

        lifecycleScope.launch {
            viewModel.validateFlashcall(body)
        }
    }
}