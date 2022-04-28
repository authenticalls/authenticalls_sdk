package com.authenticalls.flashcall_demo.ui.main

import FlashcallDemoApi
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.authenticalls.flashcall_demo.R


class LandingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (FlashcallDemoApiInternal.isEmulator) {
            activity?.title = "Authenticalls Demo (E)";
        } else {
            activity?.title = "Authenticalls Demo";
        }

        // Inflate the layout for this fragment
        // This callback will only be called when Fragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {

                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        return inflater.inflate(R.layout.landing_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.goToNumberValidation).setOnClickListener { v ->
            v.findNavController().navigate(R.id.action_landingFragment_to_phoneValidatorFragment)
        }
    }
}