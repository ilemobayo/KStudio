package com.musicplayer.aow.delegates.player.mediasession;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.Player;

import java.util.List;

public class PlayerNotificationManager extends com.google.android.exoplayer2.ui.PlayerNotificationManager{



    public PlayerNotificationManager(Context context, String channelId, int notificationId, MediaDescriptionAdapter mediaDescriptionAdapter) {
        super(context, channelId, notificationId, mediaDescriptionAdapter);
    }


    public PlayerNotificationManager(Context context, String channelId, int notificationId, MediaDescriptionAdapter mediaDescriptionAdapter, @Nullable CustomActionReceiver customActionReceiver) {
        super(context, channelId, notificationId, mediaDescriptionAdapter, customActionReceiver);
    }


}
