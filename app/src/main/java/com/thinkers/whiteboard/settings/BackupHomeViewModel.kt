package com.thinkers.whiteboard.settings

import android.util.Log
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
    }

    private val resultCallback: (Double) -> Unit = { result ->
        _uploadedSize.value = result.toLong()
    }

    private val _totalSize = MutableStateFlow(0L)
    val totalSize = _totalSize.asStateFlow()

    private val _uploadedSize = MutableStateFlow(0L)
    val uploadedSize: StateFlow<Long> = _uploadedSize.asStateFlow()

    private val _metaSize = MutableSharedFlow<Long>()
    val metaSize: SharedFlow<Long> = _metaSize.asSharedFlow()

    private val _uploadDate = MutableSharedFlow<Long>()
    val uploadDate: SharedFlow<Long> = _uploadDate.asSharedFlow()

    private val useCase = BackupHomeUseCase(viewModelScope, resultCallback)

    var dialogTitle = ""

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

    suspend fun checkUpdates() {
        viewModelScope.launch {
            Log.i(TAG, "in")
            val dbMetadata = async { useCase.checkFileUpdate(Constants.backupFileName) }.await()
            val walMetadata = async { useCase.checkFileUpdate(Constants.backupWalFileName) }.await()
            val shmMetadata = async { useCase.checkFileUpdate(Constants.backupShmFileName) }.await()

            val totalSize = dbMetadata.sizeBytes + walMetadata.sizeBytes + shmMetadata.sizeBytes
            _metaSize.emit(totalSize)
            _uploadDate.emit(dbMetadata.updatedTimeMillis)
            Log.i(TAG, "size: ${totalSize}, date: ${dbMetadata.updatedTimeMillis}, currenttime: ${System.currentTimeMillis()}")
        }
    }

}
