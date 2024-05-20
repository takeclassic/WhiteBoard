package com.thinkers.whiteboard.domain

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class CreateAccountUseCase @Inject constructor(){
    operator fun invoke(auth: FirebaseAuth,
                        id: String,
                        password: String,
                        listener: OnCompleteListener<AuthResult>) {
        auth.createUserWithEmailAndPassword(id, password).addOnCompleteListener(listener)
    }
}

class SignInUseCase @Inject constructor(){
    operator fun invoke(
        auth: FirebaseAuth,
        id: String,
        password: String,
        listener: OnCompleteListener<AuthResult>
    ) {
        auth.signInWithEmailAndPassword(id, password).addOnCompleteListener(listener)
    }
}

class SendVerifyEmailUseCase @Inject constructor(){
    companion object {
        const val TAG = "SendVerifyEmailUseCase"
    }

    operator fun invoke(url: String, packageName: String, callback: (() -> Unit)? = null) {
        val auth = Firebase.auth
        val user = auth.currentUser

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(url)
            .setAndroidPackageName(packageName, false, null)
            .build()

        user?.sendEmailVerification(actionCodeSettings)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "send completed")
                callback?.invoke()
            } else {
                Log.i(TAG, "3 ${task.exception}")
            }
        }
    }
}

