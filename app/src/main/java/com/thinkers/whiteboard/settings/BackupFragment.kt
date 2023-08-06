package com.thinkers.whiteboard.settings

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.common.memo.MemoFragment
import com.thinkers.whiteboard.common.utils.Utils
import com.thinkers.whiteboard.databinding.FragmentBackupBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BackupFragment : Fragment() {
    private val viewModel: BackupViewModel by viewModels()
    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!
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
        Log.i(TAG, "id: ${viewModel.id}, password: ${viewModel.password}")
        if (isAuthExceptions(it)) { return@OnClickListener }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAuthResult(BackupViewModel.AuthType.LOGIN).collect { res ->
                   if (res == null) {
                       Toast.makeText(requireContext(), "log in failed!", Toast.LENGTH_SHORT).show()
                       Log.i(TAG, "log in failed!")
                   } else {
                       Log.i(TAG, "log in successful")
                   }
            }
        }
    }

    private val registerListener = OnClickListener {
        Log.i(TAG, "id: ${viewModel.id}, password: ${viewModel.password}")
        if (isAuthExceptions(it)) { return@OnClickListener }
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.getAuthResult(BackupViewModel.AuthType.REGISTER).collect { res ->
//                if (res == null) {
//                    Toast.makeText(requireContext(), "register failed!", Toast.LENGTH_SHORT).show()
//                    Log.i(TAG, "register failed!")
//                } else {
//                    Log.i(TAG, "register successful")
//                }
//            }
//        }
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

        binding.backupIdEdittext.addTextChangedListener(idTextWatcher)
        binding.backupPasswordEdittext.addTextChangedListener(passwordTextWatcher)

        binding.backupLoginButton.setOnClickListener(signInListener)
        binding.backupRegisterButton.setOnClickListener(registerListener)

        val currentUser = viewModel.auth?.currentUser
        if (currentUser != null) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy called!")
        _binding = null
    }

    private fun isAuthExceptions(view: View): Boolean {
        if(!viewModel.isEmailCorrect()) {
            Snackbar.make(view, "입력하신 id가 이메일 형식이 아닙니다", Snackbar.LENGTH_SHORT).show()
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