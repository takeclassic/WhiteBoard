package com.thinkers.whiteboard.utils.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.util.Log
import androidx.work.*
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

class NotificationHelper {
    companion object {
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.channel_name)
                val descriptionText = context.getString(R.string.channel_description)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                mChannel.description = descriptionText

                val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(mChannel)
            }
        }

        fun startNotificationWorker(
            memoId: Int,
            alarmTime: Long
        ) {
            Log.i(TAG, "startNotificationWorker alarmTime: $alarmTime memoId: $memoId")
            val myData = Data.Builder().putInt("KEY_MEMO_ID", memoId).build()
            Log.i(TAG, "set memoId: ${myData.getInt("KEY_MEMO_ID", -1)}")

            val notificationWorkRequest =
                OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(alarmTime, TimeUnit.MILLISECONDS)
                    .setInputData(myData)
                    .build()

            WorkManager
                .getInstance(WhiteBoardApplication.context())
                .enqueueUniqueWork(
                    memoId.toString(),
                    ExistingWorkPolicy.REPLACE,
                    notificationWorkRequest
                )
        }

        fun cancelNotificationWorker(memoId: Int) {
            Log.i(TAG, "cancelNotificationWorker memoId: $memoId")

            WorkManager
                .getInstance(WhiteBoardApplication.context())
                .cancelUniqueWork(memoId.toString())
        }

        const val CHANNEL_ID = "1"
        private const val TAG = "NotificationHelper"
    }
}
