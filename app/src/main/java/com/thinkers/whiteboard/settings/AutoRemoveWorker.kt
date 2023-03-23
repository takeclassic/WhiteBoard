package com.thinkers.whiteboard.settings

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.database.repositories.MemoRepository
import kotlinx.coroutines.*

class AutoRemoveWorker(
    context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return autoRemoveWork()
    }

    private suspend fun autoRemoveWork(): Result =  withContext(GlobalScope.coroutineContext) {
        Log.i(TAG, "in the worker")
        val memoRepository = WhiteBoardApplication.instance!!.memoRepository
        val allMemosWithoutFavorites = memoRepository.getAllMemosWithoutFavorites()
        for (memo in allMemosWithoutFavorites) {
            Log.i(TAG, "currentTimeMillis(): ${System.currentTimeMillis()}, memo.createdTime: ${memo.createdTime}, minus: ${System.currentTimeMillis() - memo.createdTime}")
            if (System.currentTimeMillis() - memo.createdTime > TIME) {
                memoRepository.removeMemoToBin(memo)
            }
        }
        Log.i(TAG, "success!")
        Result.success()
    }

    companion object {
        const val TIME = 1209600000
        const val TAG = "AutoRemoveWorker"
    }


}
