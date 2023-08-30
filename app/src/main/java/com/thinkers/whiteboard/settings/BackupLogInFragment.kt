package com.thinkers.whiteboard.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
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
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.enums.AuthErrorCodes
import com.thinkers.whiteboard.common.enums.AuthInfo
import com.thinkers.whiteboard.common.enums.AuthType
import com.thinkers.whiteboard.common.utils.Utils
import com.thinkers.whiteboard.common.view.ProgressBarWithText
import com.thinkers.whiteboard.customs.EditNoteViewModel
import com.thinkers.whiteboard.customs.EditNoteViewModelFactory
import com.thinkers.whiteboard.databinding.FragmentBackupLogInBinding
import kotlinx.coroutines.launch


class BackupLogInFragment : Fragment() {
    private lateinit var viewModel: BackupLoginViewModel
    private var _binding: FragmentBackupLogInBinding? = null
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
        val progressBar = addProgressBar()
        if (isAuthExceptions(it)) {
            removeProgressBar(progressBar)
            return@OnClickListener
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAuthResult(AuthType.LOGIN).collect { res ->
                when(res) {
                    is AuthInfo.Success -> {
                        Toast.makeText(requireContext(), R.string.backup_login_sign_in_success, Toast.LENGTH_SHORT).show()
                        removeProgressBar(progressBar)
                        findNavController().navigate(R.id.action_nav_backup_login_to_nav_backup_home)
                    }
                    is AuthInfo.Failure -> {
                        removeProgressBar(progressBar)
                        if (res.errorCode == AuthErrorCodes.NETWORK) {
                            Toast.makeText(requireContext(), R.string.backup_login_network_problem, Toast.LENGTH_SHORT).show()
                        } else if (res.errorCode == AuthErrorCodes.NOT_EXIST) {
                            Toast.makeText(requireContext(), R.string.backup_login_not_exist_problem, Toast.LENGTH_SHORT).show()
                        } else if(res.errorCode == AuthErrorCodes.DEFAULT) {
                            Toast.makeText(requireContext(), R.string.backup_login_default_problem, Toast.LENGTH_SHORT).show()
                        } else if(res.errorCode == AuthErrorCodes.NOT_VERIFIED) {
                            viewModel.sendVerifyEmail {
                                findNavController().navigate(R.id.action_nav_backup_login_to_nav_backup_verify)
                            }
                        }
                    }
                }
            }
        }
    }

    private val registerListener = OnClickListener {
        val progressBar = addProgressBar()

        Log.i(TAG, "register id: ${viewModel.id}, password: ${viewModel.password}")
        if (isAuthExceptions(it)) {
            removeProgressBar(progressBar)
            return@OnClickListener
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAuthResult(AuthType.REGISTER).collect { res ->
                when(res) {
                    is AuthInfo.Success -> {
                        removeProgressBar(progressBar)
                        Toast.makeText(requireContext(), R.string.backup_login_register_success, Toast.LENGTH_SHORT).show()
                        viewModel.sendVerifyEmail {
                            findNavController().navigate(R.id.action_nav_backup_login_to_nav_backup_verify)
                        }
                    }
                    is AuthInfo.Failure -> {
                        removeProgressBar(progressBar)
                        if (res.errorCode == AuthErrorCodes.NETWORK) {
                            Toast.makeText(requireContext(), R.string.backup_login_network_problem, Toast.LENGTH_SHORT).show()
                        } else if (res.errorCode == AuthErrorCodes.ALREADY_EXIST) {
                            Toast.makeText(requireContext(), R.string.backup_login_duplicate_problem, Toast.LENGTH_SHORT).show()
                        } else if(res.errorCode == AuthErrorCodes.DEFAULT) {
                            Toast.makeText(requireContext(), R.string.backup_login_default_problem, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun addProgressBar(): ProgressBarWithText {
        val progressBar = ProgressBarWithText(requireContext())
        progressBar.setProgressBarColor(ContextCompat.getColor(requireContext(), R.color.default_icon))
        val str = getString(R.string.progressbar_waiting)
        progressBar.setText(str)
        progressBar.setTextSize(16f)
        progressBar.setTextColor(ContextCompat.getColor(requireContext(), R.color.default_icon))
        progressBar.id = View.generateViewId()
        progressBar.setLayoutColor(ContextCompat.getColor(requireContext(), R.color.default_drawable))

        binding.backupViewLayout.alpha = 0.5f
        binding.backupEmptyLayout.addView(progressBar, 0)
        binding.backupEmptyLayout.translationZ = 10f

        val set = ConstraintSet()
        set.clone(binding.backupEmptyLayout)
        set.connect(progressBar.id, ConstraintSet.TOP, binding.backupEmptyLayout.id, ConstraintSet.TOP, 0)
        set.connect(progressBar.id, ConstraintSet.BOTTOM, binding.backupEmptyLayout.id, ConstraintSet.BOTTOM, 0)
        set.connect(progressBar.id, ConstraintSet.START, binding.backupEmptyLayout.id, ConstraintSet.START, 0)
        set.connect(progressBar.id, ConstraintSet.END, binding.backupEmptyLayout.id, ConstraintSet.END, 0)
        set.applyTo(binding.backupEmptyLayout)
        return progressBar
    }

    private fun removeProgressBar(progressBar: ProgressBarWithText) {
        Log.i(TAG, "remove is called!")
        binding.backupEmptyLayout.removeView(progressBar)
        binding.backupViewLayout.alpha = 1f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            BackUpLoginViewModelFactory(
                WhiteBoardApplication.instance!!.backupUseCase
            )
        ).get(BackupLoginViewModel::class.java)
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
        _binding = FragmentBackupLogInBinding.inflate(inflater, container, false)
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

        binding.backupLoginButton.setOnClickListener(signInListener)
        binding.backupRegisterButton.setOnClickListener(registerListener)
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

    companion object {
        const val TAG = "BackupFragment"
    }
}