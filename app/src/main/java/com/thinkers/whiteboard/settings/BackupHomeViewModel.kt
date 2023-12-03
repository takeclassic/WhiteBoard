package com.thinkers.whiteboard.settings

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.enums.Constants
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class BackupHomeViewModel : ViewModel() {
    companion object {
        const val TAG = "BackupHomeViewModel"
        enum class states {
            NONE, BACK_UP, RESTORE, DELETE
        }
    }

    private val resultCallback: (Pair<String, Double>) -> Unit = { result ->
        val value = result.second.toLong()

        if (result.first == Constants.backupFileName
            || result.first == Constants.backupWalFileName
            || result.first == Constants.backupShmFileName) {
            _uploadedSize.value = value
        } else {
            _downloadSize.value = value
        }
    }

    private val _totalSize = MutableStateFlow(0L)
    val totalSize = _totalSize.asStateFlow()

    private val _uploadedSize = MutableStateFlow(0L)
    val uploadedSize: StateFlow<Long> = _uploadedSize.asStateFlow()

    private val _downloadSize = MutableStateFlow(0L)
    val downloadSize: StateFlow<Long> = _downloadSize.asStateFlow()

    private val _metaSize = MutableSharedFlow<Long>()
    val metaSize: SharedFlow<Long> = _metaSize.asSharedFlow()
    var currentMetaSize = 0L

    private val _uploadDate = MutableSharedFlow<Long>()
    val uploadDate: SharedFlow<Long> = _uploadDate.asSharedFlow()

    private val useCase = BackupHomeUseCase(viewModelScope, resultCallback)

    var dialogTitle = ""
    // none, backup, restore, delete
    var state = Companion.states.NONE

    suspend fun backUpDbFiles() = withContext(viewModelScope.coroutineContext) {
        val dbFile =
            WhiteBoardApplication.instance!!.applicationContext.getDatabasePath(Constants.originalFileName)

        _totalSize.value = useCase.getTotalSize(dbFile)
        Log.i(TAG, "size: ${totalSize.value}")

        val res1 = async { useCase.doBackup(dbFile.path, Constants.backupFileName) }
        val res2 = async { useCase.doBackup(dbFile.path + "-wal", Constants.backupWalFileName) }
        val res3 = async { useCase.doBackup(dbFile.path + "-shm", Constants.backupShmFileName) }

        val result = res1.await() + res2.await() + res3.await()
        result == 3
    }

    suspend fun restoreDbFiles(downloadPath: String) = withContext(viewModelScope.coroutineContext) {
        val res1 = async { useCase.doDownload(downloadPath, Constants.backupFileName) }
        val res2 = async { useCase.doDownload(downloadPath, Constants.backupWalFileName) }
        val res3 = async { useCase.doDownload(downloadPath, Constants.backupShmFileName) }

        val result = res1.await() + res2.await() + res3.await()
        if (result == 3) {
            useCase.doRestore()
            true
        } else {
            false
        }
    }

    suspend fun checkUpdates() {
        viewModelScope.launch {
            Log.i(TAG, "in")
            val dbMetadata = async { useCase.checkFileUpdate(Constants.backupFileName) }.await()
            val walMetadata = async { useCase.checkFileUpdate(Constants.backupWalFileName) }.await()
            val shmMetadata = async { useCase.checkFileUpdate(Constants.backupShmFileName) }.await()

            if (dbMetadata.isFailure || walMetadata.isFailure || shmMetadata.isFailure) {
                useCase.totalFileSize = 0
                currentMetaSize = 0
                _metaSize.emit(0)
                _uploadDate.emit(0)
                Log.i(TAG, "file does not exist!")
                return@launch
            }

            val totalSize = dbMetadata.getOrNull()!!.sizeBytes + walMetadata.getOrNull()!!.sizeBytes + shmMetadata.getOrNull()!!.sizeBytes
            useCase.totalFileSize = totalSize
            currentMetaSize = totalSize
            _metaSize.emit(totalSize)
            _uploadDate.emit(dbMetadata.getOrNull()!!.updatedTimeMillis)
            Log.i(TAG, "size: ${totalSize}, date: ${dbMetadata.getOrNull()!!.updatedTimeMillis}, currenttime: ${System.currentTimeMillis()}")
        }
    }

    suspend fun deleteFilesOnServer() = withContext(viewModelScope.coroutineContext) {
        val dbFileDeleteResult = async { useCase.doDelete(Constants.backupFileName) }
        val walFileDeleteResult = async { useCase.doDelete(Constants.backupWalFileName) }
        val shmFileDeleteResult = async { useCase.doDelete(Constants.backupShmFileName) }

        val result =
            dbFileDeleteResult.await() + walFileDeleteResult.await() + shmFileDeleteResult.await()
        result == 3
    }

    fun resetState() {
        state = Companion.states.NONE
    }
}
