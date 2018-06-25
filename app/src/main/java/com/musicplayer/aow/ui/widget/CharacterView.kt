package com.musicplayer.aow.ui.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.Dimension
import android.util.AttributeSet
import android.view.View
import com.musicplayer.aow.R

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com.musicpalyer.com.musicplayer.aow
 * Date: 7/16/16
 * Time: 6:51 AM
 * Desc: CharacterView
 */
class CharacterView : View {

    // private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#CCCCCC");
    // private static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#EEEEEE");

    private var mCharacter: String? = null
    @ColorInt
    private var mCharacterTextColor: Int = 0
    private var mBackgroundRoundAsCircle: Boolean = false
    @ColorInt
    private var mBackgroundColor: Int = 0
    @Dimension
    private var mBackgroundRadius: Float = 0.toFloat()
    @Dimension
    internal var mCharacterPadding: Float = 0.toFloat()

    // Paint mPaint = new Paint();
    // Path mClipPath = new Path();
    // RectF mBackgroundRect = new RectF();

    internal lateinit var mDrawable: CharacterDrawable

    // Getters & Setters

    var character: String?
        get() = mCharacter
        set(character) {
            mDrawable.character = character
        }

    var characterTextColor: Int
        get() = mCharacterTextColor
        set(characterTextColor) {
            mDrawable.characterTextColor = characterTextColor
        }

    var isBackgroundRoundAsCircle: Boolean
        get() = mBackgroundRoundAsCircle
        set(backgroundRoundAsCircle) {
            mDrawable.isBackgroundRoundAsCircle = backgroundRoundAsCircle
        }

    var backgroundRadius: Float
        get() = mBackgroundRadius
        set(backgroundRadius) {
            mDrawable.backgroundRadius = backgroundRadius
        }

    var characterPadding: Float
        get() = mCharacterPadding
        set(characterPadding) {
            mDrawable.characterPadding = characterPadding
        }

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, 0)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CharacterView,
                defStyleAttr,
                defStyleRes
        )
        try {
            mCharacter = typedArray.getString(R.styleable.CharacterView_character)
            mCharacterTextColor = typedArray.getColor(R.styleable.CharacterView_characterTextColor, 0)
            mBackgroundRoundAsCircle = typedArray.getBoolean(R.styleable.CharacterView_backgroundRoundAsCircle, false)
            mBackgroundColor = typedArray.getColor(R.styleable.CharacterView_backgroundColor, 0)
            mBackgroundRadius = typedArray.getDimension(R.styleable.CharacterView_backgroundRadius, 0f)
            mCharacterPadding = typedArray.getDimension(R.styleable.CharacterView_characterPadding, 0f)

            mDrawable = CharacterDrawable.Builder()
                    .setCharacter(mCharacter!!)
                    .setCharacterTextColor(mCharacterTextColor)
                    .setBackgroundRoundAsCircle(mBackgroundRoundAsCircle)
                    .setBackgroundColor(mBackgroundColor)
                    .setBackgroundRadius(mBackgroundRadius)
                    .setCharacterPadding(mCharacterPadding)
                    .build()
        } finally {
            typedArray.recycle()
        }
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mDrawable.setBounds(0, 0, width, height)
        mDrawable.draw(canvas)
        /*
        mPaint.setAntiAlias(true);

        // Draw background
        mPaint.setColor(mBackgroundColor);
        mBackgroundRect.set(0, 0, getHeight(), getHeight());

        if (mBackgroundRoundAsCircle) {
            canvas.drawOval(mBackgroundRect, mPaint);
            mClipPath.addOval(mBackgroundRect, Path.Direction.CW);
        } else {
            canvas.drawRoundRect(mBackgroundRect, mBackgroundRadius, mBackgroundRadius, mPaint);
            mClipPath.addRoundRect(mBackgroundRect, mBackgroundRadius, mBackgroundRadius, Path.Direction.CW);
        }
        canvas.clipPath(mClipPath);

        // Draw text in the center of the canvas
        mPaint.setColor(mCharacterTextColor);
        mPaint.setTextSize(getHeight() - mCharacterPadding * 2);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTypeface(Typeface.DEFAULT);

        //获取paint中的字体信息  setTextSize 要在他前面
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        // 计算文字高度baseline
        float textBaseY = getHeight() - fontMetrics.bottom / 2 - mCharacterPadding;
        //获取字体的长度
        float fontWidth = mPaint.measureText(mCharacter);
        //计算文字长度的baseline
        float textBaseX = (getWidth() - fontWidth) / 2;
        canvas.drawText(mCharacter, textBaseX, textBaseY, mPaint);
        */
    }

    fun getBackgroundColor(): Int {
        return mBackgroundColor
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        mDrawable.backgroundColor = backgroundColor
    }

    companion object {

        private val DEFAULT_CHARACTER = "C"
    }
}
