package com.thinkers.whiteboard.settings

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWebException
import com.thinkers.whiteboard.common.enums.AuthErrorCodes
import com.thinkers.whiteboard.common.enums.AuthInfo
import com.thinkers.whiteboard.common.enums.AuthType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BackupViewModel : ViewModel() {
    var id: String = ""
    var password: String = ""
    var auth: FirebaseAuth? = null
    val backupUseCase = BackupUseCase()

    fun isPasswordCorrect(): Boolean {
        val matchResult = passwordRegex.matchEntire(password) ?: return false
        if (matchResult.value != password) { return false }
        return true
    }

    fun isEmailCorrect(): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        if (emailPattern.matcher(id).matches()) {
            return true
        }
        return false
    }

    fun getAuthResult(authType: AuthType): Flow<AuthInfo> = callbackFlow {
        val listener = OnCompleteListener {
            if (it.isSuccessful) {
                trySend(AuthInfo.Success(it.result))
            } else {
                runCatching {
                    when(it.exception!!) {
                        is FirebaseAuthUserCollisionException -> {
                            trySend(AuthInfo.Failure(AuthErrorCodes.ALREADY_EXIST))
                        }
                        is FirebaseAuthInvalidUserException -> {
                            trySend(AuthInfo.Failure(AuthErrorCodes.NOT_EXIST))
                        }
                        is FirebaseAuthWebException -> {
                            trySend(AuthInfo.Failure(AuthErrorCodes.NETWORK))
                        }
                        else -> {
                            trySend(AuthInfo.Failure(AuthErrorCodes.DEFAULT))
                        }
                    }
                }.onFailure {
                    trySend(AuthInfo.Failure(AuthErrorCodes.DEFAULT))
                }
            }
        }
        when(authType) {
            AuthType.LOGIN -> {
                backupUseCase.signIn(auth!!, id, password, listener)
            }
            AuthType.REGISTER -> {
                backupUseCase.createAccount(auth!!, id, password, listener)
            }
        }
        awaitClose()
    }

    companion object {
        // MARK: 비밆번호 패턴 - 영문대소문자, 특수문자, 숫자, 10~20자
        private val passwordRegex = Regex("""^(?=.*[a-zA-Z])(?=.*[!@#${'$'}%^*+=-])(?=.*[0-9]).{10,20}${'$'}""")
        const val TAG = "BackupViewModel"
    }
}