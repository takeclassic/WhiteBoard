package com.thinkers.whiteboard.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.data.enums.Constants
import com.thinkers.whiteboard.presentation.helpers.DataBackupHelper
import com.thinkers.whiteboard.presentation.helpers.DataBackupHelperFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BackupHomeViewModel @Inject constructor() : ViewModel() {
    companion object {
        const val TAG = "BackupHomeViewModel"
        enum class states {
            NONE, BACK_UP, RESTORE, DELETE
        }
    }

    private val resultCallback: (Pair<String, Long>) -> Unit = { result ->
        val value = result.second

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
    var currentMetaSize = -1L

    private val _uploadDate = MutableSharedFlow<Long>()
    val uploadDate: SharedFlow<Long> = _uploadDate.asSharedFlow()

    @Inject lateinit var dataBackupHelperFactory: DataBackupHelperFactory
    private lateinit var dataBackupHelper:DataBackupHelper

    var dialogTitle = ""
    // none, backup, restore, delete
    var state = states.NONE

    private fun provideDataBackupHelper(scope: CoroutineScope, resultCallback: (Pair<String, Long>) -> Unit) {
        dataBackupHelper = dataBackupHelperFactory.create(viewModelScope, resultCallback)
    }

    suspend fun backUpDbFiles() = withContext(viewModelScope.coroutineContext) {
        provideDataBackupHelper(viewModelScope, resultCallback)

        val dbFile =
            WhiteBoardApplication.instance!!.applicationContext.getDatabasePath(Constants.originalFileName)

        _totalSize.value = dataBackupHelper.getTotalSize(dbFile)
        Log.i(TAG, "size: ${totalSize.value}")

        val res1 = async { dataBackupHelper.doBackup(dbFile.path, Constants.backupFileName) }
        val res2 = async { dataBackupHelper.doBackup(dbFile.path + "-wal", Constants.backupWalFileName) }
        val res3 = async { dataBackupHelper.doBackup(dbFile.path + "-shm", Constants.backupShmFileName) }

        val result = res1.await() + res2.await() + res3.await()
        result == 3
    }

    suspend fun restoreDbFiles(downloadPath: String) = withContext(viewModelScope.coroutineContext) {
        provideDataBackupHelper(viewModelScope, resultCallback)

        val dbFile =
            WhiteBoardApplication.instance!!.applicationContext.getDatabasePath(Constants.originalFileName)

        _totalSize.value = dataBackupHelper.getTotalSize(dbFile)

        val res1 = async { dataBackupHelper.doDownload(downloadPath, Constants.backupFileName) }
        val res2 = async { dataBackupHelper.doDownload(downloadPath, Constants.backupWalFileName) }
        val res3 = async { dataBackupHelper.doDownload(downloadPath, Constants.backupShmFileName) }

        val result = res1.await() + res2.await() + res3.await()
        if (result == 3) {
            dataBackupHelper.doRestore()
            true
        } else {
            false
        }
    }

    suspend fun checkUpdates() {
        viewModelScope.launch {
            provideDataBackupHelper(viewModelScope, resultCallback)

            val dbMetadata = async { dataBackupHelper.checkFileUpdate(Constants.backupFileName) }.await()
            val walMetadata = async { dataBackupHelper.checkFileUpdate(Constants.backupWalFileName) }.await()
            val shmMetadata = async { dataBackupHelper.checkFileUpdate(Constants.backupShmFileName) }.await()

            if (dbMetadata.isFailure || walMetadata.isFailure || shmMetadata.isFailure) {
                dataBackupHelper.totalFileSize = 0
                currentMetaSize = 0
                _metaSize.emit(0)
                _uploadDate.emit(0)
                Log.i(TAG, "file does not exist!")
                return@launch
            }

            val totalSize = dbMetadata.getOrNull()!!.sizeBytes + walMetadata.getOrNull()!!.sizeBytes + shmMetadata.getOrNull()!!.sizeBytes
            dataBackupHelper.totalFileSize = totalSize
            currentMetaSize = totalSize
            _metaSize.emit(totalSize)
            _uploadDate.emit(dbMetadata.getOrNull()!!.updatedTimeMillis)
            Log.i(TAG, "size: ${totalSize}, date: ${dbMetadata.getOrNull()!!.updatedTimeMillis}, currenttime: ${System.currentTimeMillis()}")
        }
    }

    suspend fun deleteFilesOnServer() = withContext(viewModelScope.coroutineContext) {
        provideDataBackupHelper(viewModelScope, resultCallback)

        val dbFileDeleteResult = async { dataBackupHelper.doDelete(Constants.backupFileName) }
        val walFileDeleteResult = async { dataBackupHelper.doDelete(Constants.backupWalFileName) }
        val shmFileDeleteResult = async { dataBackupHelper.doDelete(Constants.backupShmFileName) }

        val result =
            dbFileDeleteResult.await() + walFileDeleteResult.await() + shmFileDeleteResult.await()
        result == 3
    }

    fun resetState() {
        state = states.NONE
    }
}
