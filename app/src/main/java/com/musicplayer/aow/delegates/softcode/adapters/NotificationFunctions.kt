package com.musicplayer.aow.delegates.softcode.adapters

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.main.MainActivity


class NotificationFunctions (var context: Context) {

    fun sendNotification(title: String?, msg: String?) {
        val notification = NotificationCompat.Builder(context)
                .setTicker("MusiXPlay")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .build()
        // hide the notification after its selected
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager!!.notify(0, notification)
    }

    fun downloadSuccessNotification(title: String?, location:String?, msg: String?) {

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("location", location)
        }

        val pi = PendingIntent.getActivity(context, 0, intent, 0)
        val notification = NotificationCompat.Builder(context)
                .setTicker("MusiXPlay")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(msg)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
        // hide the notification after its selected
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager!!.notify(0, notification)
    }

}