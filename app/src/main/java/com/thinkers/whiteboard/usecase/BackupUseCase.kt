package com.thinkers.whiteboard.usecase

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.presentation.viewmodels.BackupLoginViewModel

class BackupUseCase {
    fun createAccount(auth: FirebaseAuth,
                      id: String,
                      password: String,
                      listener: OnCompleteListener<AuthResult>) {
        auth.createUserWithEmailAndPassword(id, password).addOnCompleteListener(listener)
    }

    fun signIn(auth: FirebaseAuth,
               id: String,
               password: String,
               listener: OnCompleteListener<AuthResult>) {
        auth.signInWithEmailAndPassword(id, password).addOnCompleteListener(listener)
    }

    fun sendVerifyEmail(callback: (() -> Unit)? = null) {
        val auth = Firebase.auth
        val user = auth.currentUser

        val url = "https://whiteboard1.page.link/verify"
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(url)
            .setAndroidPackageName("https://www.thinkers/whiteboard/verify", false, null)
            .build()

        user?.sendEmailVerification(actionCodeSettings)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(BackupLoginViewModel.TAG, "send completed")
                callback?.invoke()
            } else {
                Log.i(BackupLoginViewModel.TAG, "3 ${task.exception}")
            }
        }
    }
}