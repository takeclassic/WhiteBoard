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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.data.enums.AuthErrorCodes
import com.thinkers.whiteboard.data.enums.AuthInfo
import com.thinkers.whiteboard.data.enums.AuthType
import com.thinkers.whiteboard.utils.Utils
import com.thinkers.whiteboard.databinding.FragmentLogInBinding
import com.thinkers.whiteboard.presentation.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LogInFragment : Fragment() {
    companion object {
        const val TAG = "BackupFragment"
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
            binding.backupIdEdittext.startAnimation(fadeIn)
            binding.backupPasswordEdittext.startAnimation(fadeIn)
            binding.backupLoginButton.startAnimation(fadeIn)
            binding.backupRegisterButton.startAnimation(fadeIn)
            binding.backupAgreementText.startAnimation(fadeIn)
            binding.backupFindPassword.startAnimation(fadeIn)
        }

        override fun onAnimationRepeat(animation: Animation?) {}
    }

    private val fadeInAnimationListener = object : AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            binding.backupIdEdittext.visibility = View.VISIBLE
            binding.backupPasswordEdittext.visibility = View.VISIBLE
            binding.backupLoginButton.visibility = View.VISIBLE
            binding.backupRegisterButton.visibility = View.VISIBLE
            binding.backupAgreementText.visibility = View.VISIBLE
            binding.backupFindPassword.visibility  = View.VISIBLE
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

    private val signInListener = OnClickListener {
        Log.i(TAG, "sign in id: ${viewModel.id}, password: ${viewModel.password}")
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)

        addProgressBar()
        if (isAuthExceptions(it)) {
            removeProgressBar()
            return@OnClickListener
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val res = viewModel.doLogin()
            when (res) {
                is AuthInfo.Success -> {
                    removeProgressBar()
                    findNavController().navigate(R.id.action_nav_backup_login_to_nav_backup_home)
                }
                is AuthInfo.Failure -> {
                    removeProgressBar()
                    if (res.errorCode == AuthErrorCodes.NETWORK) {
                        Toast.makeText(
                            requireContext(),
                            R.string.backup_login_network_problem,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (res.errorCode == AuthErrorCodes.NOT_EXIST) {
                        Toast.makeText(
                            requireContext(),
                            R.string.backup_login_not_exist_problem,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (res.errorCode == AuthErrorCodes.DEFAULT) {
                        Toast.makeText(
                            requireContext(),
                            R.string.backup_login_default_problem,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (res.errorCode == AuthErrorCodes.NOT_VERIFIED) {
                        viewModel.sendVerifyEmail { exception ->
                            if (exception != null) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.backup_login_default_problem,
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@sendVerifyEmail
                            }
                            findNavController().navigate(R.id.action_nav_backup_login_to_nav_backup_verify)
                        }
                    }
                }
            }
        }
    }

    private fun addProgressBar() {
        binding.backupProgressBar.visibility = View.VISIBLE
        binding.backupEmptyLayout.translationZ = 10f
        binding.backupViewLayout.alpha = 0.5f
    }

    private fun removeProgressBar() {
        binding.backupProgressBar.visibility = View.GONE
        binding.backupViewLayout.alpha = 1f
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

        binding.backupIdEdittext.addTextChangedListener(idTextWatcher)
        binding.backupPasswordEdittext.addTextChangedListener(passwordTextWatcher)
        binding.backupPasswordEdittext.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.backupLoginButton.callOnClick()
                true
            }
            false
        }

        binding.backupLoginButton.setOnClickListener(signInListener)
        binding.backupRegisterButton.setOnClickListener{
            findNavController().navigate(R.id.action_nav_login_to_nav_register)
        }
        binding.backupFindPassword.setOnClickListener {
            findNavController().navigate(R.id.action_nav_backup_login_to_nav_send_password_reset)
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
}
