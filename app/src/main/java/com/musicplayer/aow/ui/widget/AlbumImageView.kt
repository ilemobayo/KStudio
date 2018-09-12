package com.musicplayer.aow.ui.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import com.musicplayer.aow.BuildConfig

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com.musicpalyer.com.musicplayer.aow
 * Date: 9/6/16
 * Time: 11:39 PM
 * Desc: AlbumImageView
 * Referenced [android.support.v4.widget.SwipeRefreshLayout]'s implementation.
 */
class AlbumImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var mShadowRadius: Int = 0

    internal var mPaint = Paint()
    internal var mMiddleRect = RectF()
    internal var mInnerRect = RectF()
    internal var mAlbumPathRect = RectF()
    internal var mAlbumTextPath = Path()

    internal var mDensity: Float = 0.toFloat()

    // Animation
    private var mRotateAnimator: ObjectAnimator? = null
    private var mLastAnimationValue: Long = 0

    init {
        init()
    }

    //    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    //    @SuppressWarnings("unused")
    //    public AlbumImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    //        super(context, attrs, defStyleAttr, defStyleRes);
    //        init();
    //    }

    private fun init() {
        mDensity = context.resources.displayMetrics.density
        val shadowXOffset = (mDensity * X_OFFSET).toInt()
        val shadowYOffset = (mDensity * Y_OFFSET).toInt()

        mShadowRadius = (mDensity * SHADOW_RADIUS).toInt()

        val circle: ShapeDrawable
        if (elevationSupported()) {
            circle = ShapeDrawable(OvalShape())
            ViewCompat.setElevation(this, SHADOW_ELEVATION * mDensity)
        } else {
            val oval = OvalShadow(mShadowRadius)
            circle = ShapeDrawable(oval)
            ViewCompat.setLayerPaint(this, circle.paint)
            circle.paint.setShadowLayer(mShadowRadius.toFloat(), shadowXOffset.toFloat(), shadowYOffset.toFloat(), KEY_SHADOW_COLOR)
            val padding = mShadowRadius
            // set padding so the inner image sits correctly within the shadow.
            setPadding(padding, padding, padding, padding)
        }
        circle.paint.isAntiAlias = true
        circle.paint.color = DEFAULT_ALBUM_COLOR
        background = circle

        mPaint.isAntiAlias = true
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.style = Paint.Style.FILL
        mPaint.color = DEFAULT_ALBUM_COLOR
        mPaint.textSize = ALBUM_CIRCLE_TEXT_SIZE * mDensity

        mRotateAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f)
        mRotateAnimator!!.duration = 3600
        mRotateAnimator!!.interpolator = LinearInterpolator()
        mRotateAnimator!!.repeatMode = ValueAnimator.RESTART
        mRotateAnimator!!.repeatCount = ValueAnimator.INFINITE
    }

    private fun elevationSupported(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= 23
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!elevationSupported()) {
            setMeasuredDimension(measuredWidth + mShadowRadius * 2, measuredHeight + mShadowRadius * 2)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint.color = MIDDLE_RECT_COLOR
        canvas.drawOval(mMiddleRect, mPaint)
        mPaint.color = INNER_RECT_COLOR
        canvas.drawOval(mInnerRect, mPaint)

        mPaint.textSize = ALBUM_CIRCLE_TEXT_SIZE * mDensity
        mPaint.color = ALBUM_CIRCLE_TEXT_COLOR
        canvas.drawTextOnPath(ALBUM_TEXT, mAlbumTextPath, 2 * mDensity, 2 * mDensity, mPaint)

        mPaint.textSize = ALBUM_CIRCLE_TEXT_SIZE_SMALL * mDensity
        canvas.drawText(APP_NAME, (width / 2).toFloat(), (height / 2).toFloat(), mPaint)
        canvas.drawText(APP_SLOGAN, (width / 2).toFloat(), height / 2 + 4 * mDensity, mPaint)
        canvas.drawText(BUILD, (width / 2).toFloat(), height / 2 + 8 * mDensity, mPaint)
        canvas.drawText(COPY_RIGHT, (width / 2).toFloat(), height / 2 + 12 * mDensity, mPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val middleRectSize = mDensity * MIDDLE_RECT_SIZE
        val innerRectSize = mDensity * INNER_RECT_SIZE
        val albumRectSize = mDensity * ALBUM_TEXT_PATH_RECT_SIZE
        mMiddleRect.set(0f, 0f, middleRectSize, middleRectSize)
        mInnerRect.set(0f, 0f, innerRectSize, innerRectSize)
        mAlbumPathRect.set(0f, 0f, albumRectSize, albumRectSize)

        mMiddleRect.offset(w / 2 - middleRectSize / 2, h / 2 - middleRectSize / 2)
        mInnerRect.offset(w / 2 - innerRectSize / 2, h / 2 - innerRectSize / 2)
        mAlbumPathRect.offset(w / 2 - albumRectSize / 2, h / 2 - albumRectSize / 2)

        mAlbumTextPath.addOval(mAlbumPathRect, Path.Direction.CW)
    }

    // Animation

    fun startRotateAnimation() {
        mRotateAnimator!!.cancel()
        mRotateAnimator!!.start()
    }

    fun cancelRotateAnimation() {
        mLastAnimationValue = 0
        mRotateAnimator!!.cancel()
    }

    fun pauseRotateAnimation() {
        mLastAnimationValue = mRotateAnimator!!.currentPlayTime
        mRotateAnimator!!.cancel()
    }

    fun resumeRotateAnimation() {
        mRotateAnimator!!.start()
        mRotateAnimator!!.currentPlayTime = mLastAnimationValue
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mRotateAnimator !=
                null) {
            mRotateAnimator!!.cancel()
            mRotateAnimator = null
        }
    }

    /**
     * Draw oval shadow below ImageView under lollipop.
     */
    private inner class OvalShadow internal constructor(shadowRadius: Int) : OvalShape() {
        private var mRadialGradient: RadialGradient? = null
        private val mShadowPaint: Paint = Paint()

        init {
            mShadowRadius = shadowRadius
            updateRadialGradient(rect().width().toInt())
        }

        override fun onResize(width: Float, height: Float) {
            super.onResize(width, height)
            updateRadialGradient(width.toInt())
        }

        override fun draw(canvas: Canvas, paint: Paint) {
            val viewWidth = this@AlbumImageView.width
            val viewHeight = this@AlbumImageView.height
            canvas.drawCircle((viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(), (viewWidth / 2).toFloat(), mShadowPaint)
            canvas.drawCircle((viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(), (viewWidth / 2 - mShadowRadius).toFloat(), paint)
        }

        private fun updateRadialGradient(diameter: Int) {
            mRadialGradient = RadialGradient((diameter / 2).toFloat(), (diameter / 2).toFloat(),
                    mShadowRadius.toFloat(), intArrayOf(FILL_SHADOW_COLOR, Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
            mShadowPaint.shader = mRadialGradient
        }
    }

    companion object {

        // private static final String TAG = "AlbumImageView";

        private val KEY_SHADOW_COLOR = 0x1E000000
        private val FILL_SHADOW_COLOR = 0x3D000000

        private val X_OFFSET = 0f
        private val Y_OFFSET = 1.75f

        private val SHADOW_RADIUS = 24f
        private val SHADOW_ELEVATION = 16

        private val DEFAULT_ALBUM_COLOR = -0xc3a088
        private val MIDDLE_RECT_COLOR = -0xb38e74
        private val INNER_RECT_COLOR = 0x4FD8D8D8

        private val ALBUM_CIRCLE_TEXT_COLOR = -0x634234

        private val ALBUM_CIRCLE_TEXT_SIZE = 3.5f
        private val ALBUM_CIRCLE_TEXT_SIZE_SMALL = 2f

        private val MIDDLE_RECT_SIZE = 80
        private val INNER_RECT_SIZE = 64
        private val ALBUM_TEXT_PATH_RECT_SIZE = 56

        private val ALBUM_TEXT = "MUSIC PLAYER"
        private val APP_NAME = "AOWPlayer"
        private val APP_SLOGAN = "Make com.musicplayer.aow simpler"
        private val COPY_RIGHT = "zuezhome © 2017"
        @SuppressLint("DefaultLocale")
        private val BUILD = String.format("build release %s-%d (%s)",
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, BuildConfig.FLAVOR)
    }
}
