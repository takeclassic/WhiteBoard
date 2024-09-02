package com.thinkers.whiteboard.presentation.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.data.common.types.LoginTypes
import com.thinkers.whiteboard.utils.Utils
import com.thinkers.whiteboard.databinding.FragmentLogInBinding
import com.thinkers.whiteboard.presentation.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LogInFragment : Fragment() {
    companion object {
        const val TAG = "loginFragment"
    }

    private val viewModel: LoginViewModel by viewModels()
    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!
    private lateinit var slideUp: Animation
    private lateinit var fadeIn: Animation

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().popBackStack()
        }
    }

    private val slideUpAnimationListener = object : AnimationListener {
        override fun onAnimationStart(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            binding.loginKakaoButton.startAnimation(fadeIn)
            binding.loginGoogleButton.startAnimation(fadeIn)
            binding.loginAgreementText.startAnimation(fadeIn)
            binding.loginFindPassword.startAnimation(fadeIn)
        }

        override fun onAnimationRepeat(animation: Animation?) {}
    }

    private val fadeInAnimationListener = object : AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            binding.loginKakaoButton.visibility = View.VISIBLE
            binding.loginGoogleButton.visibility = View.VISIBLE
            binding.loginAgreementText.visibility = View.VISIBLE
            binding.loginFindPassword.visibility  = View.VISIBLE
        }

        override fun onAnimationEnd(animation: Animation?) {}

        override fun onAnimationRepeat(animation: Animation?) {}
    }

    private val idTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            p0?.let {
                viewModel.id = Editable.Factory.getInstance().newEditable(p0).toString()
            }
        }
    }

    private val passwordTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            p0?.let {
                viewModel.password = Editable.Factory.getInstance().newEditable(p0).toString()
            }
        }
    }

    private fun addProgressBar() {
        binding.loginProgressBar.visibility = View.VISIBLE
        binding.loginEmptyLayout.translationZ = 10f
        binding.loginViewLayout.alpha = 0.5f
    }

    private fun removeProgressBar() {
        binding.loginProgressBar.visibility = View.GONE
        binding.loginViewLayout.alpha = 1f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        viewModel.auth = Firebase.auth
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginCloseButton.setOnClickListener { findNavController().popBackStack() }
        binding.loginLayout.setOnTouchListener { v, event ->
            Utils.hideKeyboard(requireContext(), v)
            v.clearFocus()
            false
        }

        slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        slideUp.setAnimationListener(slideUpAnimationListener)
        fadeIn.setAnimationListener(fadeInAnimationListener)

        binding.loginLogoImageview.startAnimation(slideUp)
        binding.loginLogoTextview.startAnimation(slideUp)

        binding.loginKakaoButton.setOnClickListener { loginButtonClickListener(LoginTypes.KaKaoLogin) }
        binding.loginGoogleButton.setOnClickListener { loginButtonClickListener(LoginTypes.GoogleLogin) }
        binding.loginFindPassword.setOnClickListener {
            //findNavController().navigate(R.id.action_nav_backup_login_to_nav_send_password_reset))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy called!")
        _binding = null
    }

    private fun isAuthExceptions(view: View): Boolean {
        if(!viewModel.isEmailCorrect()) {
            Snackbar.make(view, R.string.backup_id_info_text, Snackbar.LENGTH_SHORT).show()
            return true
        }
        if (!viewModel.isPasswordCorrect()) {
            Snackbar.make(view, R.string.backup_password_info_text, Snackbar.LENGTH_LONG).show()
            return true
        }
        return false
    }

    private fun loginButtonClickListener(loginType: LoginTypes) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addProgressBar()
                viewModel
                    .doLogin(loginType, requireContext())
                    .onSuccess {
                        removeProgressBar()
                        findNavController().navigate(R.id.action_nav_backup_login_to_nav_backup_home)
                    }
                    .onFailure {
                        removeProgressBar()
                        Toast.makeText(
                            requireContext(),
                            it.localizedMessage ?: "Something was wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }
}
