package com.thinkers.whiteboard.presentation.helpers

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.thinkers.whiteboard.data.database.AppDatabase
import com.thinkers.whiteboard.data.enums.Constants
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject
import kotlin.coroutines.resume

@AssistedFactory
interface DataBackupHelperFactory {
    fun create(scope: CoroutineScope, resultCallback: (Pair<String, Long>) -> Unit): DataBackupHelper
}

class DataBackupHelper @AssistedInject constructor(
    @Assisted private val scope: CoroutineScope,
    @Assisted private val resultCallback: (Pair<String, Long>) -> Unit
) {
    companion object {
        const val TAG = "DataBackupHelper"
    }

    private var dbFileSize = 0L
    private var dbFileTransferred = 0L

    private var walFileSize = 0L
    private var walFileTransferred = 0L

    private var shmFileSize = 0L
    private var shmFileTransferred = 0L

    var totalFileSize = 0L

    var restoreFilePath = ""

    @Inject lateinit var dbInstance: AppDatabase

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

            Log.i(TAG, "walFileTransferred: $walFileTransferred, shmFileTransferred: $shmFileTransferred, dbFileTransferred: $dbFileTransferred, add: ${walFileTransferred + shmFileTransferred + dbFileTransferred}, total: $totalFileSize")

            val result = runCatching {
                (walFileTransferred + shmFileTransferred + dbFileTransferred) * 100 / totalFileSize
            }.getOrDefault(0)
            Log.i(TAG, "result: $result")
            resultCallback.invoke(Pair(fileName, result))
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
        val fileRefStr = "${Constants.commonFolderName}/$uid/$fileName"
        val fileRef: StorageReference = Firebase.storage.reference.child(fileRefStr)

        val file = File(path)
        getFileSize(file)
        val fileUri = Uri.fromFile(file)
        val fileUploadTask = fileRef.putFile(fileUri)

        val result = suspendCancellableCoroutine { cont ->
            fileUploadTask.addOnProgressListener { taskSnapShot ->
                updateTransferredData(fileName, taskSnapShot.bytesTransferred)
            }.addOnFailureListener {
                Log.i(TAG, "fileUploadTask failed! reason: $it")
                cont.resume(-1)
            }.addOnSuccessListener { taskSnapshot ->
                Log.i(
                    TAG,
                    "fileUploadTask succeed! snapshot: ${taskSnapshot.metadata}"
                )
                cont.resume(1)
            }
        }

        return result
    }

    suspend fun doDownload(downloadPath: String, downloadFileName: String): Int {
        val storage = Firebase.storage
        var storageRef = storage.reference
        val fileRefStr = "${Constants.commonFolderName}/${Firebase.auth.uid}/$downloadFileName"
        var fileRef: StorageReference = storageRef.child(fileRefStr)

        val fileName = when(downloadFileName) {
            Constants.backupFileName -> Constants.originalFileName
            Constants.backupWalFileName -> Constants.originalWalFileName
            Constants.backupShmFileName -> Constants.originalShmFileName
            else -> ""
        }

        restoreFilePath = downloadPath
        val path = "$downloadPath/$fileName"
        val file = File(path)

        val result = suspendCancellableCoroutine { cont ->
            fileRef.getFile(file).addOnProgressListener { taskSnapShot ->
                updateTransferredData(fileName, taskSnapShot.bytesTransferred)
            }.addOnFailureListener {
                Log.i(TAG, "file download failed! reason: $it")
                cont.resume(-1)
            }.addOnSuccessListener { taskSnapshot ->
                Log.i(TAG, "file download succeed! $taskSnapshot")
                cont.resume(1)
            }
        }

        return result
    }

    suspend fun doDelete(fileName: String): Int {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val fileRefStr = "${Constants.commonFolderName}/${Firebase.auth.uid}/$fileName"
        val fileRef: StorageReference = storageRef.child(fileRefStr)
        val result = suspendCancellableCoroutine<Int> { cont ->
            fileRef.delete().addOnSuccessListener {
                Log.i(TAG, "file delete success!")
                cont.resume(1)
            }.addOnFailureListener {
                Log.i(TAG, "file delete failed! reason: $it")
                cont.resume(-1)
            }
        }

        return result
    }

    fun doRestore() {
        val originalPath = dbInstance.openHelper.readableDatabase.path
        val originalFile = File(originalPath)
        Log.i(TAG, "original path: $originalPath, exist: ${originalFile.exists()}")

        val originalWalPath = dbInstance.openHelper.readableDatabase.path + "-wal"
        val originalWalFile = File(originalWalPath)

        val originalShmPath = dbInstance.openHelper.readableDatabase.path + "-shm"
        val originalShmFile = File(originalShmPath)

        dbInstance.close()

        val path = "$restoreFilePath/${Constants.originalFileName}"
        val file = File(path)

        val pathWal = "$restoreFilePath/${Constants.originalWalFileName}"
        val fileWal = File(pathWal)

        val pathShm = "$restoreFilePath/${Constants.originalShmFileName}"
        val fileShm = File(pathShm)
        Log.i(TAG, "backup path: $path, exist: ${file.exists()}")

        val res = file.copyTo(originalFile, true)
        Log.i(TAG, "result: ${res.exists()}, ${res.absolutePath}")

        val res2 = fileWal.copyTo(originalWalFile, true)
        Log.i(TAG, "result: ${res2.exists()}, ${res2.absolutePath}")

        val res3 = fileShm.copyTo(originalShmFile, true)
        Log.i(TAG, "result: ${res3.exists()}, ${res3.absolutePath}")
    }

    suspend fun checkFileUpdate(fileName: String): Result<StorageMetadata> {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val fileRefStr = "${Constants.commonFolderName}/${Firebase.auth.uid}/$fileName"
        val fileRef: StorageReference = storageRef.child(fileRefStr)

        val result = suspendCancellableCoroutine<Result<StorageMetadata>> { cont ->
            fileRef.metadata.addOnSuccessListener {
                cont.resume(Result.success(it))
            }.addOnFailureListener {
                Log.i(TAG, "failed to check file metadata : $it")
                cont.resume(Result.failure(it))
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
