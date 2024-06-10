package com.thinkers.whiteboard.domain

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thinkers.whiteboard.data.enums.AuthInfo
import javax.inject.Inject

class CreateAccountUseCase @Inject constructor(){
    operator fun invoke(id: String,
                        password: String,
                        listener: OnCompleteListener<AuthResult>) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(id, password).addOnCompleteListener(listener)
    }
}

class SignInUseCase @Inject constructor(){
    operator fun invoke(
        id: String,
        password: String,
        listener: OnCompleteListener<AuthResult>
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(id, password).addOnCompleteListener(listener)
    }
}

class SendVerifyEmailUseCase @Inject constructor(){
    companion object {
        const val TAG = "SendVerifyEmailUseCase"
    }

    operator fun invoke(url: String, packageName: String, callback: ((Exception?) -> Unit)? = null) {
        val auth = Firebase.auth
        val user = auth.currentUser

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(url)
            .setAndroidPackageName(packageName, false, null)
            .build()

        user?.sendEmailVerification(actionCodeSettings)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "send completed")
                callback?.invoke(null)
            } else {
                Log.i(TAG, "3 ${task.exception}")
                callback?.invoke(task.exception)
            }
        }
    }
}

class SaveDataToCloudUseCase @Inject constructor() {
    companion object {
        const val TAG = "SaveDataToCloudUseCase"
    }
    operator fun invoke(tableName: String, phoneNumber: String) {
        val db = Firebase.firestore

        val userInfo = hashMapOf("phoneNumber" to phoneNumber)
        db.collection(tableName)
            .add(userInfo)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }
}

class ReadDataFromCloudUseCase @Inject constructor() {
    companion object {
        const val TAG = "ReadDataFromCloudUseCase"
    }

    operator fun invoke(tableName: String, key: String) {
        val db = Firebase.firestore
        db.collection(tableName)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
}

