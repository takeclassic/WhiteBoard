package com.thinkers.whiteboard

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.thinkers.whiteboard.domain.MemoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltWorker
class AutoRemoveWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {
    companion object {
        // Two weeks in millis
        const val TIME = 1209600000
        const val TAG = "AutoRemoveWorker"
    }

    @Inject lateinit var memoRepository: MemoRepository

    override suspend fun doWork(): Result {
        return autoRemoveWork()
    }

    private suspend fun autoRemoveWork(): Result =  withContext(GlobalScope.coroutineContext) {
        Log.i(TAG, "in the worker")
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
}
