package com.thinkers.whiteboard.presentation.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.data.enums.AuthErrorCodes
import com.thinkers.whiteboard.data.enums.AuthInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class BackupSendPasswordResetEmailViewModel : ViewModel() {
    var emailAddress: String = ""

    fun isEmailCorrect(): Boolean {
        if (emailAddress.isNullOrEmpty()) {
            return false
        }

        val emailPattern = Patterns.EMAIL_ADDRESS
        if (emailPattern.matcher(emailAddress).matches()) {
            return true
        }
        return false
    }

    suspend fun sendPasswordResetEmail(): Flow<AuthInfo<Boolean>> {
        if (!isEmailCorrect()) {
            return flow { emit(AuthInfo.Failure(AuthErrorCodes.NOT_EMAIL_FORM)) }
        }
        return callbackFlow {
            Firebase.auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "Email sent.")
                        trySend(AuthInfo.Success(true))
                    } else {
                        task.exception?.let {
                            when (it) {
                                is FirebaseAuthInvalidUserException -> {
                                    trySend(AuthInfo.Failure(AuthErrorCodes.NOT_EXIST))
                                }
                                is FirebaseAuthInvalidCredentialsException -> {
                                    trySend(AuthInfo.Failure(AuthErrorCodes.NOT_EMAIL_FORM))
                                }
                                is FirebaseNetworkException -> {
                                    trySend(AuthInfo.Failure(AuthErrorCodes.NETWORK))
                                }
                                is FirebaseTooManyRequestsException -> {
                                    trySend(AuthInfo.Failure(AuthErrorCodes.TOO_MANY_REQUEST))
                                }
                                else -> {
                                    trySend(AuthInfo.Failure(AuthErrorCodes.DEFAULT))
                                }
                            }
                            Log.i(TAG, "exception: $it")
                        }
                    }
                }
            awaitClose { }
        }
    }

    companion object {
        const val TAG = "BackupSendPasswordResetEmailViewModel"
    }
}
