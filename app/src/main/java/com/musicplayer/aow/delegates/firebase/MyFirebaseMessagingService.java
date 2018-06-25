package com.musicplayer.aow.delegates.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.musicplayer.aow.R;

/**
 * Created by Arca on 11/17/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "Refreshed token: " + FirebaseInstanceId.getInstance().getToken());
        // Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        if(remoteMessage.getData() != null){
            Log.e(TAG, "Notification Message Body: " + remoteMessage.getData());
            //notification
            sendNotification(FirebaseInstanceId.getInstance().getToken(),remoteMessage.getFrom(), remoteMessage.getData().toString());
        }else if(remoteMessage.getNotification().getBody() != null){
            Log.e(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
            //notification
            sendNotification(FirebaseInstanceId.getInstance().getToken(),remoteMessage.getFrom(), remoteMessage.getNotification().getBody());
        }else{
            //
        }

    }

    public void sendNotification(String token, String title, String msg) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.zuezhome.com/"));

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(token)
                .setSmallIcon(R.drawable.ic_notification_app_logo)
                .setContentTitle(title)
                .setContentText(msg)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        // hide the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
