package com.musicplayer.aow.utils.images;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BitmapDraws {


    public static Drawable createFromPath(String pathName) {
        if (pathName == null) {
            return null;
        }

        try {
            Bitmap bm = BitmapFactory.decodeFile(pathName);
            if (bm != null) {
                return drawableFromBitmap(null, bm);
            }
        } finally {
            //
        }

        return null;
    }

    private static Drawable drawableFromBitmap(Resources res, Bitmap bm) {

        return new BitmapDrawable(res, bm);
    }
}
