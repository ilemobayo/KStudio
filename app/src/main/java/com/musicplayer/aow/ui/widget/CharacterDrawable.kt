package com.musicplayer.aow.ui.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.DimenRes
import android.support.annotation.Dimension

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com.musicpalyer.com.musicplayer.aow
 * Date: 7/16/16
 * Time: 6:59 AM
 * Desc: CharacterDrawable
 */
class CharacterDrawable private constructor()// Avoid direct instantiate
    : Drawable() {

    // Getters & Setters

    var character: String? = null
    @ColorInt
    var characterTextColor: Int = 0
    var isBackgroundRoundAsCircle: Boolean = false
    @ColorInt
    var backgroundColor: Int = 0
    @Dimension
    var backgroundRadius: Float = 0.toFloat()
    @Dimension
    var characterPadding: Float = 0.toFloat()

    internal var mPaint = Paint()
    internal var mClipPath = Path()
    internal var mBackgroundRect = RectF()

    internal var mWidth: Int = 0
    internal var mHeight: Int = 0

    override fun draw(canvas: Canvas) {
        mWidth = bounds.right - bounds.left
        mHeight = bounds.bottom - bounds.top

        mPaint.isAntiAlias = true

        // Draw background
        mPaint.color = backgroundColor
        mBackgroundRect.set(0f, 0f, mWidth.toFloat(), mHeight.toFloat())

        if (isBackgroundRoundAsCircle) {
            canvas.drawOval(mBackgroundRect, mPaint)
            mClipPath.addOval(mBackgroundRect, Path.Direction.CW)
        } else {
            canvas.drawRoundRect(mBackgroundRect, backgroundRadius, backgroundRadius, mPaint)
            mClipPath.addRoundRect(mBackgroundRect, backgroundRadius, backgroundRadius, Path.Direction.CW)
        }
        canvas.clipPath(mClipPath)

        // Draw text in the center of the canvas
        mPaint.color = characterTextColor
        mPaint.textSize = mHeight - characterPadding * 2
        mPaint.style = Paint.Style.FILL
        mPaint.typeface = Typeface.DEFAULT

        if (character != null) {
            //获取paint中的字体信息  setTextSize 要在他前面
            val fontMetrics = mPaint.fontMetrics
            // 计算文字高度baseline
            val textBaseY = mHeight.toFloat() - fontMetrics.bottom / 2 - characterPadding
            //获取字体的长度
            val fontWidth = mPaint.measureText(character)
            //计算文字长度的baseline
            val textBaseX = (mWidth - fontWidth) / 2
            canvas.drawText(character!!, textBaseX, textBaseY, mPaint)
        }
        // Clip the circle path
        // http://stackoverflow.com/a/22829656/2290191
        // canvas.drawPath(mClipPath, mPaint);
    }

    override fun setAlpha(i: Int) {
        // TODO
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // TODO
    }

    override fun getOpacity(): Int {
        // TODO
        return PixelFormat.OPAQUE
    }

    class Builder {

        private var character: String? = null

        @ColorInt
        private var characterTextColor = DEFAULT_TEXT_COLOR

        private var backgroundRoundAsCircle: Boolean = false
        @ColorInt
        private var backgroundColor = DEFAULT_BACKGROUND_COLOR
        @Dimension
        private var backgroundRadius: Float = 0.toFloat()

        @Dimension
        private var mCharacterPadding: Float = 0.toFloat()

        fun applyStyle(): Builder {
            return this
        }

        fun setCharacter(character: Char): Builder {
            this.character = character.toString()
            return this
        }

        fun setCharacter(character: String): Builder {
            this.character = character
            return this
        }

        fun setCharacterTextColor(@ColorInt textColor: Int): Builder {
            this.characterTextColor = textColor
            return this
        }

        fun setBackgroundRoundAsCircle(roundAsCircle: Boolean): Builder {
            this.backgroundRoundAsCircle = roundAsCircle
            return this
        }

        fun setBackgroundColor(@ColorInt backgroundColor: Int): Builder {
            this.backgroundColor = backgroundColor
            return this
        }

        fun setBackgroundRadius(@Dimension backgroundRadius: Float): Builder {
            this.backgroundRadius = backgroundRadius
            return this
        }

        fun setCharacterPadding(@Dimension padding: Float): Builder {
            this.mCharacterPadding = padding
            return this
        }

        fun build(): CharacterDrawable {
            val drawable = CharacterDrawable()
            drawable.character = character
            drawable.characterTextColor = characterTextColor
            drawable.isBackgroundRoundAsCircle = backgroundRoundAsCircle
            drawable.backgroundColor = backgroundColor
            drawable.backgroundRadius = backgroundRadius
            drawable.characterPadding = mCharacterPadding
            return drawable
        }

        companion object {

            private val DEFAULT_TEXT_COLOR = Color.parseColor("#CCCCCC")
            private val DEFAULT_BACKGROUND_COLOR = Color.parseColor("#EEEEEE")
        }
    }

    companion object {

        fun create(context: Context, character: Char, roundAsCircle: Boolean, @DimenRes padding: Int): CharacterDrawable {
            return CharacterDrawable.Builder()
                    .setCharacter(character)
                    .setBackgroundRoundAsCircle(roundAsCircle)
                    .setCharacterPadding(context.resources.getDimensionPixelSize(padding).toFloat())
                    .build()
        }
    }
}
