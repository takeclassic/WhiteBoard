package com.thinkers.whiteboard.utils.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.hilt.work.HiltWorker
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.thinkers.whiteboard.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor (
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
): Worker(appContext, workerParams){
    override fun doWork(): Result {
        createNotification(appContext, inputData)
        return Result.success()
    }

    private fun createNotification(
        context: Context,
        inputData: Data
    ) {
        val memoId = inputData.getInt("KEY_MEMO_ID", 0)
        Log.i("NotificationWorker", "get memoId: $memoId")
        val bundle = bundleOf("memoId" to memoId)
        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.nav_memo)
            .setArguments(bundle)
            .createPendingIntent()

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.icon_main)
            .setColor(Color.GRAY)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(memoId, builder.build())
        }
    }
}
