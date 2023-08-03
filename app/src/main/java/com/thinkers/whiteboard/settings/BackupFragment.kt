package com.thinkers.whiteboard.settings

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.common.utils.Utils
import com.thinkers.whiteboard.databinding.FragmentBackupBinding

class BackupFragment : Fragment() {
    private lateinit var viewModel: BackupViewModel
    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private lateinit var slideUp: Animation
    private lateinit var fadeIn: Animation

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Log.i(TAG, "it is called!")
            findNavController().popBackStack()
        }
    }

    private val slideUpAnimationListener = object : AnimationListener {
        override fun onAnimationStart(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            binding.backupIdEdittext.startAnimation(fadeIn)
            binding.backupPasswordEdittext.startAnimation(fadeIn)
            binding.backupLoginButton.startAnimation(fadeIn)
            binding.backupRegisterButton.startAnimation(fadeIn)
        }

        override fun onAnimationRepeat(animation: Animation?) {}
    }

    private val fadeInAnimationListener = object : AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            binding.backupIdEdittext.visibility = View.VISIBLE
            binding.backupPasswordEdittext.visibility = View.VISIBLE
            binding.backupLoginButton.visibility = View.VISIBLE
            binding.backupRegisterButton.visibility = View.VISIBLE
        }

        override fun onAnimationEnd(animation: Animation?) {}

        override fun onAnimationRepeat(animation: Animation?) {}
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onDetach() {
        super.onDetach()
        onBackPressedCallback.remove()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract(),
        ) { res ->
            this.onSignInResult(res)
        }
        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backupCloseButton.setOnClickListener { findNavController().popBackStack() }
        binding.backupLayout.setOnTouchListener { v, event ->
            Utils.hideKeyboard(requireContext(), v)
            v.clearFocus()
            false
        }

        slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        slideUp.setAnimationListener(slideUpAnimationListener)
        fadeIn.setAnimationListener(fadeInAnimationListener)

        binding.backupLogoImageview.startAnimation(slideUp)
        binding.backupLogoTextview.startAnimation(slideUp)

        val currentUser = auth.currentUser
        if (currentUser != null) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy called!")
        _binding = null
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            Log.i(TAG, "sign in is completed")
            val user = FirebaseAuth.getInstance().currentUser
            // ...
        } else {
            Log.i(TAG, "sign in is failed!")
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

    companion object {
        const val TAG = "BackupFragment"
    }
}