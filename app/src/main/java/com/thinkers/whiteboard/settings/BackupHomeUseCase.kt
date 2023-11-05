package com.thinkers.whiteboard.settings

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.thinkers.whiteboard.common.enums.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.coroutines.resume

class BackupHomeUseCase(
    private val scope: CoroutineScope,
    private val resultCallback: (Double) -> Unit
) {
    companion object {
        const val TAG = "BackupHomeUseCase"
    }

    private var dbFileSize = 0L
    private var dbFileTransferred = 0L

    private var walFileSize = 0L
    private var walFileTransferred = 0L

    private var shmFileSize = 0L
    private var shmFileTransferred = 0L

    private var totalFileSize = 0L

    private val dataTransferredChannel = Channel<Pair<String, Long>>()
    private val channelJob = scope.launch(Dispatchers.Default) {
        for (data in dataTransferredChannel) {
            val fileName = data.first
            val byteTransferred = data.second

            Log.i(TAG, "fileName: $fileName, data: $byteTransferred")

            if (fileName.contains("wal")) {
                walFileTransferred = byteTransferred
            } else if (fileName.contains("shm")) {
                shmFileTransferred = byteTransferred
            } else {
                dbFileTransferred = byteTransferred
            }

            val result = (walFileTransferred.toDouble() + shmFileTransferred.toDouble() + dbFileTransferred.toDouble()) /  totalFileSize.toDouble() * 100
            resultCallback.invoke(result)
        }
    }

    suspend fun getTotalSize(file: File) = withContext(Dispatchers.IO) {
        val path = Paths.get(file.path)
        val pathWal = Paths.get(file.path + "-wal")
        val pathShm = Paths.get(file.path + "-shm")

        totalFileSize = Files.size(path) + Files.size(pathWal) + Files.size(pathShm)
        totalFileSize
    }

    suspend fun doBackup(path: String, fileName: String): Int {
        val uid = Firebase.auth.uid
        val fileRefStr = "users/$uid/$fileName"
        val fileRef: StorageReference = Firebase.storage.reference.child(fileRefStr)

        val file = File(path)
        getFileSize(file)
        val fileUri = Uri.fromFile(file)
        val fileUploadTask = fileRef.putFile(fileUri)

        val result = suspendCancellableCoroutine { cont ->
            fileUploadTask.addOnProgressListener { taskSnapShot ->
                updateTransferredData(fileName, taskSnapShot.bytesTransferred)
            }.addOnFailureListener {
                Log.i(BackupHomeViewModel.TAG, "fileUploadTask failed! reason: $it")
                cont.resume(-1)
            }.addOnSuccessListener { taskSnapshot ->
                Log.i(
                    BackupHomeViewModel.TAG,
                    "fileUploadTask succeed! snapshot: ${taskSnapshot.metadata}"
                )
                cont.resume(1)
            }
        }

        return result
    }

    suspend fun checkFileUpdate(fileName: String): StorageMetadata {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val fileRefStr = "${Constants.commonFolderName}/${Firebase.auth.uid}/$fileName"
        val fileRef: StorageReference = storageRef.child(fileRefStr)

        val result = suspendCancellableCoroutine { cont ->
            fileRef.metadata.addOnSuccessListener {
                cont.resume(it)
            }
        }
        return result
    }

    private fun getFileSize(file: File) {
        val size = file.length()

        if (file.name.contains("wal")) {
            walFileSize = size
        } else if (file.name.contains("shm")) {
            shmFileSize = size
        } else {
            dbFileSize = size
        }
    }

    private fun updateTransferredData(fileName: String, byteTransferred: Long) {
        scope.launch {
            dataTransferredChannel.send(Pair(fileName, byteTransferred))
        }
    }
}
