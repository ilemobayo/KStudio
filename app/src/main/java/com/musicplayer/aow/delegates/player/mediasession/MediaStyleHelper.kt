package com.musicplayer.aow.delegates.player.mediasession

import android.content.Context
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

/**
 * Helper APIs for constructing MediaStyle notifications
 */
object MediaStyleHelper {
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

        val builder = NotificationCompat.Builder(context, "musixplay")
        builder.setContentTitle(description.title)
                .setContentText(description.subtitle)
                .setSubText(description.description)
                .setLargeIcon(description.iconBitmap)
                .setContentIntent(controller.sessionActivity)
                .setDeleteIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_STOP)
                )
                //.setColor(Injection.provideContext()!!.resources.getColor(R.color.white))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        return builder
    }
}