package com.thinkers.whiteboard.presentation.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.*
import com.thinkers.whiteboard.data.enums.AuthAddress
import com.thinkers.whiteboard.data.enums.AuthErrorCodes
import com.thinkers.whiteboard.data.enums.AuthInfo
import com.thinkers.whiteboard.data.enums.AuthType
import com.thinkers.whiteboard.domain.CreateAccountUseCase
import com.thinkers.whiteboard.domain.SendVerifyEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val createAccountUseCase: CreateAccountUseCase,
    private val sendVerifyEmailUseCase: SendVerifyEmailUseCase
) : ViewModel() {
    companion object {
        // MARK: 비밆번호 패턴 - 영문대소문자, 특수문자, 숫자, 10~20자
        private val passwordRegex = Regex("""^(?=.*[a-zA-Z])(?=.*[!@#${'$'}%^*+=-])(?=.*[0-9]).{10,20}${'$'}""")
        private const val TAG = "RegisterViewModel"
    }

    var id = ""
    var password = ""
    var passwordAgain = ""

    fun isEmailCorrect(): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        if (emailPattern.matcher(id).matches()) {
            return true
        }
        return false
    }

    fun isPasswordCorrect(): Boolean {
        val matchResult = passwordRegex.matchEntire(password) ?: return false
        if (matchResult.value != password) { return false }
        return true
    }

    fun isPasswordSame(): Boolean {
        return password == passwordAgain
    }

    fun sendVerifyEmail(callback: ((Exception?) -> Unit)? = null) {
        sendVerifyEmailUseCase(AuthAddress.URL_VERIFY.str, AuthAddress.REDIRECT_PACKAGE_NAME.str, callback)
    }

    suspend fun doRegister(): AuthInfo<AuthResult> =
        suspendCancellableCoroutine { cont ->
            val listener = OnCompleteListener {
                if (it.isSuccessful) {
                    cont.resume(AuthInfo.Success(it.result), null)
                } else {
                    when (it.exception!!) {
                        is FirebaseAuthUserCollisionException -> {
                            cont.resume(AuthInfo.Failure(AuthErrorCodes.ALREADY_EXIST), null)
                        }
                        is FirebaseAuthWebException -> {
                            cont.resume(AuthInfo.Failure(AuthErrorCodes.NETWORK), null)
                        }
                        else -> {
                            cont.resume(AuthInfo.Failure(AuthErrorCodes.DEFAULT), null)
                        }
                    }
                }
            }
            createAccountUseCase(id, password, listener)
        }
}