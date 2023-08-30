package com.thinkers.whiteboard.settings

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.MainActivityViewModel
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.common.enums.AuthErrorCodes
import com.thinkers.whiteboard.common.enums.AuthInfo
import com.thinkers.whiteboard.common.enums.AuthType
import com.thinkers.whiteboard.database.repositories.MemoRepository
import com.thinkers.whiteboard.database.repositories.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class BackupLoginViewModel(private val backupUseCase: BackupUseCase) : ViewModel() {
    var id: String = ""
    var password: String = ""
    var auth: FirebaseAuth? = null

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

    fun sendVerifyEmail(callback: (() -> Unit)? = null) {
        backupUseCase.sendVerifyEmail(callback)
    }

    fun getAuthResult(authType: AuthType): Flow<AuthInfo> = callbackFlow {
        val listener = OnCompleteListener {
            if (it.isSuccessful) {
                if (authType == AuthType.LOGIN) {
                    FirebaseAuth.getInstance().currentUser?.let { user ->
                        if (user.isEmailVerified) {
                            trySend(AuthInfo.Success(it.result))
                            return@OnCompleteListener
                        }
                    }
                    trySend(AuthInfo.Failure(AuthErrorCodes.NOT_VERIFIED))
                } else {
                    trySend(AuthInfo.Success(it.result))
                }
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

class BackUpLoginViewModelFactory(
    private val backupUseCase: BackupUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BackupLoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BackupLoginViewModel(backupUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
