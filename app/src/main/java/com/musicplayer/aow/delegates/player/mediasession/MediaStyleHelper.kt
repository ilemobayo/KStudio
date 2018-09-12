package com.musicplayer.aow.delegates.player.mediasession

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import android.support.v4.media.session.MediaSessionCompat
import com.musicplayer.aow.ui.main.MainActivity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaButtonReceiver.*
import android.support.v4.media.session.PlaybackStateCompat


/**
 * Helper APIs for constructing MediaStyle notifications
 */
object MediaStyleHelper {

    val CHANNEL_ID = "musixplay_media_playback_channel"

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(mContext: Context) {
        val mNotificationManager = mContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // The id of the channel.
        val id = CHANNEL_ID
        // The user-visible name of the channel.
        val name = "Musixplay Media playback"
        // The user-visible description of the channel.
        val description = "Musixplay Media playback controls"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(id, name, importance)
        // Configure the notification channel.
        mChannel.description = description
        mChannel.enableLights(true)
        mChannel.lightColor = Color.RED
        mChannel.setSound(null,null);
        mChannel.setShowBadge(true)
        mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        mNotificationManager.createNotificationChannel(mChannel)
    }
    /**
     * Build a notification using the information from the given media session. Makes heavy use
     * of [MediaMetadataCompat.getDescription] to extract the appropriate information.
     * @param context Context used to construct the notification.
     * @param mediaSession Media session to get information.
     * @return A pre-built notification with information from the given media session.
     */
    fun from(
            context: Context, mediaSession: MediaSessionCompat): NotificationCompat.Builder {
        val controller = mediaSession.controller
        val mediaMetadata = controller.metadata
        val description = mediaMetadata.description
        // The PendingIntent to launch our activity if the user selects this notification
        val contentIntent = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java), 0)

        // You only need to create the channel on API 26+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(context);
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.setContentTitle(description.title)
                .setColor(Color.WHITE)
                .setSound(null,0)
                .setContentText(description.subtitle)
                .setSubText(description.description)
                .setLargeIcon(description.iconBitmap)
                .setContentIntent(contentIntent)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        return builder
    }
}