package com.thinkers.whiteboard.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.data.enums.AuthErrorCodes
import com.thinkers.whiteboard.data.enums.AuthInfo
import com.thinkers.whiteboard.databinding.FragmentRegisterBinding
import com.thinkers.whiteboard.presentation.viewmodels.RegisterViewModel
import com.thinkers.whiteboard.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    companion object {
        const val TAG = "RegisterFragment"
    }

    private val viewModel: RegisterViewModel by viewModels()
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().popBackStack()
        }
    }

    private val idTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable?) {
            s?.let {
                viewModel.id = Editable.Factory.getInstance().newEditable(it).toString()
            }
        }
    }

    private val passwordTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable?) {
            s?.let {
                viewModel.password = Editable.Factory.getInstance().newEditable(it).toString()
            }
        }
    }

    private val rePasswordTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            s?.let {
                viewModel.passwordAgain = Editable.Factory.getInstance().newEditable(it).toString()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerLayout.setOnTouchListener { v, event ->
            Utils.hideKeyboard(requireContext(), v)
            v.clearFocus()
            false
        }

        binding.registerConfirmButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    if (!viewModel.isEmailCorrect()) {
                        Snackbar.make(
                            it,
                            R.string.backup_id_info_text,
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    if (!viewModel.isPasswordCorrect()) {
                        Snackbar.make(
                            it,
                            R.string.wrong_password_typing,
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    if (!viewModel.isPasswordSame()) {
                        Snackbar.make(
                            it,
                            R.string.not_same_password_typing,
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    addProgressBar()
                    val result = viewModel.doRegister()
                    Log.i(TAG, "result: $result")
                    when (result) {
                        is AuthInfo.Success -> {
                            viewModel.sendVerifyEmail { exception ->
                                removeProgressBar()
                                if (exception != null) {
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.backup_login_default_problem,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@sendVerifyEmail
                                }
                                findNavController().navigate(R.id.action_nav_register_to_nav_backup_verify)
                            }
                        }
                        is AuthInfo.Failure -> {
                            removeProgressBar()
                            if (result.errorCode == AuthErrorCodes.NETWORK) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.backup_login_network_problem,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (result.errorCode == AuthErrorCodes.ALREADY_EXIST) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.backup_login_duplicate_problem,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (result.errorCode == AuthErrorCodes.DEFAULT) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.backup_login_default_problem,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }

        binding.registerEmailEdittext.addTextChangedListener(idTextWatcher)
        binding.registerPasswordEdittext.addTextChangedListener(passwordTextWatcher)
        binding.registerRePasswordEdittext.addTextChangedListener(rePasswordTextWatcher)
        binding.registerBackButton.setOnClickListener {
            Utils.hideKeyboard(requireContext(), it)
            findNavController().popBackStack()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onDetach() {
        super.onDetach()
        onBackPressedCallback.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addProgressBar() {
        binding.registerProgressbar.visibility = View.VISIBLE
        binding.registerEmptyLayout.translationZ = 10f
        binding.registerViewLayout.alpha = 0.5f
    }

    private fun removeProgressBar() {
        binding.registerProgressbar.visibility = View.GONE
        binding.registerViewLayout.alpha = 1f
    }
}