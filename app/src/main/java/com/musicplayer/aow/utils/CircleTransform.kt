package com.musicplayer.aow.utils


import android.content.Context
import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class CircleTransform(context: Context) : BitmapTransformation() {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {

    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap? {
        return getCroppedBitmap(toTransform)
    }

    fun getCroppedBitmap(imageBitmap: Bitmap): Bitmap {
        var bitmap = cropBitmap(imageBitmap)
//        Log.e("image size", " " + bitmap.height + " " + bitmap.width)
        val output = Bitmap.createBitmap(bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = -0xbdbebd
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle((bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                (bitmap.width / 2).toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    fun cropBitmap(bitmap: Bitmap) : Bitmap{
        if (bitmap.width >= bitmap.height){

            return Bitmap.createBitmap(
                    bitmap,
                    bitmap.width/2 - bitmap.height/2,
                    0,
                    bitmap.height,
                    bitmap.height
            )

        }else{

            return Bitmap.createBitmap(
                    bitmap,
                    0,
                    bitmap.height/2 - bitmap.width/2,
                    bitmap.width,
                    bitmap.width
            )
        }
    }

    fun getId(): String {
        return javaClass.name
    }
}