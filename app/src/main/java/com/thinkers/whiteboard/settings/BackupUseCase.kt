package com.thinkers.whiteboard.settings

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BackupUseCase {
    fun createAccount(auth: FirebaseAuth,
                      id: String,
                      password: String,
                      listener: OnCompleteListener<AuthResult>) {
        CoroutineScope(Dispatchers.IO).launch {
            auth.createUserWithEmailAndPassword(id, password).addOnCompleteListener(listener)
        }
    }

    fun signIn(auth: FirebaseAuth,
               id: String,
               password: String,
               listener: OnCompleteListener<AuthResult>) {
        CoroutineScope(Dispatchers.IO).launch {
            auth.signInWithEmailAndPassword(id, password).addOnCompleteListener(listener)
        }
    }
}