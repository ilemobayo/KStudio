package com.musicplayer.aow.ui.widget.java;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;

/**
 * Created by Arca on 1/23/2018.
 */

public class BottomSheetListView extends RecyclerView {
    public BottomSheetListView (Context context, AttributeSet p_attrs) {
        super (context, p_attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        if (canScrollVertically(this)) {
//            getParent().requestDisallowInterceptTouchEvent(true);
//        }
        return super.onTouchEvent(ev);
    }

    public boolean canScrollVertically (AbsListView view) {
        boolean canScroll = false;

        if (view !=null && view.getChildCount ()> 0) {
            boolean isOnTop = view.getFirstVisiblePosition() != 0 || view.getChildAt(0).getTop() != 0;
            boolean isAllItemsVisible = isOnTop && view.getLastVisiblePosition() == view.getChildCount();

            if (isOnTop || isAllItemsVisible) {
                canScroll = true;
            }
        }

        return  canScroll;
    }
}
