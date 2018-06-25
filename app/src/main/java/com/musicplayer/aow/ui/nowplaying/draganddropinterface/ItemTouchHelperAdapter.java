package com.musicplayer.aow.ui.nowplaying.draganddropinterface;

public interface ItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);

    void onDrop(int fromPosition, int toPosition);
}
