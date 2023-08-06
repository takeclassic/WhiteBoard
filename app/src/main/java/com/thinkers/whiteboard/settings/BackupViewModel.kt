package com.thinkers.whiteboard.settings

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class BackupViewModel : ViewModel() {
    var id: String = ""
    var password: String = ""
    var auth: FirebaseAuth? = null
    val backupUseCase = BackupUseCase()

    enum class AuthType {
        LOGIN, REGISTER
    }

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

    fun getAuthResult(authType: AuthType): Flow<AuthResult?> = callbackFlow {
        val listener = OnCompleteListener {
            if (it.isSuccessful) {
                trySend(it.result)
            } else {
                trySend(null)
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