package com.authenticalls.flashcall_demo.ui.main.product

import FlashcallDemoApiInternal
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.authenticalls.flashcall_demo.R
import com.authenticalls.flashcall_demo.databinding.PhoneValidatorFragmentBinding
import com.authenticalls.flashcall_demo.network.models.FlashcallRequestBody
import com.authenticalls.flashcall_demo.network.models.InternalVerificationStatus
import com.authenticalls.flashcall_sdk.network.models.FlashcallSDKStatus
import com.authenticalls.flashcall_sdk.ui.FlashcallViewModel
import com.fredporciuncula.phonemoji.PhonemojiTextInputEditText
import kotlinx.coroutines.launch


class PhoneValidatorFragment : Fragment() {
    private val viewModel: PhoneValidatorViewModel by activityViewModels()
    private val flashcallViewModel: FlashcallViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Set title
        if (FlashcallDemoApiInternal.isEmulator) activity?.title =
            "Phone Validator Demo (E)" else activity?.title = "Phone Validator Demo"

        // This creates an instance of the PhoneValidatorFragmentBinding binding class for the fragment to use
        val binding = PhoneValidatorFragmentBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the [FlashcallViewModel]
        binding.flashcallViewModel = flashcallViewModel

        // Setting event for Validate Phone Number Button
        binding.validatePhoneNumber.setOnClickListener(this::sendNumberForValidation)

        // This callback will only be called when Fragment is at least Started.
        // This sets back button behavior
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    flashcallViewModel.resetObservedFlashcall()
                    view?.findNavController()
                        ?.navigate(R.id.action_phoneValidatorFragment_to_landingFragment)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        viewModel.internalVerificationStatus.observe(viewLifecycleOwner) { newState: InternalVerificationStatus ->
            // Add SDK listener when starting to request a flashcall as
            if (newState == InternalVerificationStatus.REQUESTING_FLASHCALL) {
                // Just started request
            }

            if (newState == InternalVerificationStatus.APPLICATION_ERROR) {
                Toast.makeText(context, "Application communication error", Toast.LENGTH_SHORT)
                    .show()
            }

            if (newState == InternalVerificationStatus.SDK_INTERNAL_ERROR) {
                Toast.makeText(context, "Application communication error", Toast.LENGTH_SHORT)
                    .show()
            }

            if (newState == InternalVerificationStatus.SERVICE_UNAVAILABLE) {
                Toast.makeText(context, "Server communication error", Toast.LENGTH_SHORT)
                    .show()
            }

            if (newState == InternalVerificationStatus.VERIFICATION_STARTED) {
                view?.findNavController()
                    ?.navigate(R.id.action_phoneValidatorFragment_to_flashcallFragment)
                flashcallViewModel.addListener()
            }
        }

        flashcallViewModel.internalFlashcallStatus.observe(viewLifecycleOwner) { newState: FlashcallSDKStatus ->
            if (newState == FlashcallSDKStatus.CALL_FINISHED) {
                viewModel.callDetails = flashcallViewModel.callDetails
                flashcallViewModel.resetObservedFlashcall()
                flashcallViewModel.removeListener()
                view?.findNavController()
                    ?.navigate(R.id.action_flashcallFragment_to_phoneValidatorResultFragment)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val msisdnField =
            requireView().findViewById<PhonemojiTextInputEditText>(R.id.phoneNumberFieldText)
        // just try to get phone number
        val telephonyManager =
            activity?.applicationContext?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (!(ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_PHONE_NUMBERS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            val mSimPhoneNumber1 = telephonyManager.line1Number
            if (mSimPhoneNumber1 !== null) {
                msisdnField.setInternationalPhoneNumber(mSimPhoneNumber1.toString())
            }
        } else {
            // We just tried, no permissions
        }
    }

    private fun sendNumberForValidation(view: View) {
        lifecycleScope.launch {
            val msisdn =
                requireView().findViewById<PhonemojiTextInputEditText>(R.id.phoneNumberFieldText)
            val body = FlashcallRequestBody(msisdn.text.toString())

            viewModel.requestFlashcall(body)
        }
    }

    private fun permissionWrapper(
        permission: String,
        grantedCallback: () -> Unit,
        deniedCallback: () -> Unit
    ) {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    grantedCallback()
                } else {
                    deniedCallback()
                }
            }

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            grantedCallback()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }
}
