/*
 * 
 * Copyright 2013 Matt Joseph
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 * 
 * This custom view/widget was inspired and guided by:
 * 
 * HoloCircleSeekBar - Copyright 2012 Jes�s Manzano
 * HoloColorPicker - Copyright 2012 Lars Werkman (Designed by Marie Schweiz)
 * 
 * Although I did not used the code from either project directly, they were both used as 
 * reference material, and as a result, were extremely helpful.
 */

package com.musicplayer.aow.ui.widget.circularseekbar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.musicplayer.aow.R

class CircularSeekBar : View {

    /**
     * Used to scale the dp units to pixels
     */
    protected val DPTOPX_SCALE = resources.displayMetrics.density

    /**
     * Minimum touch target size in DP. 48dp is the Android design recommendation
     */
    protected val MIN_TOUCH_TARGET_DP = 48f

    /**
     * `Paint` instance used to draw the inactive circle.
     */
    protected var mCirclePaint: Paint? = null

    /**
     * `Paint` instance used to draw the circle fill.
     */
    protected var mCircleFillPaint: Paint? = null

    /**
     * `Paint` instance used to draw the active circle (represents progress).
     */
    protected var mCircleProgressPaint: Paint? = null

    /**
     * `Paint` instance used to draw the glow from the active circle.
     */
    protected var mCircleProgressGlowPaint: Paint? = null

    /**
     * `Paint` instance used to draw the center of the pointer.
     * Note: This is broken on 4.0+, as BlurMasks do not work with hardware acceleration.
     */
    protected var mPointerPaint: Paint? = null

    /**
     * `Paint` instance used to draw the halo of the pointer.
     * Note: The halo is the part that changes transparency.
     */
    protected var mPointerHaloPaint: Paint? = null

    /**
     * `Paint` instance used to draw the border of the pointer, outside of the halo.
     */
    protected var mPointerHaloBorderPaint: Paint? = null

    /**
     * The width of the circle (in pixels).
     */
    protected var mCircleStrokeWidth: Float = 0.toFloat()

    /**
     * The X radius of the circle (in pixels).
     */
    protected var mCircleXRadius: Float = 0.toFloat()

    /**
     * The Y radius of the circle (in pixels).
     */
    protected var mCircleYRadius: Float = 0.toFloat()

    /**
     * The radius of the pointer (in pixels).
     */
    protected var mPointerRadius: Float = 0.toFloat()

    /**
     * The width of the pointer halo (in pixels).
     */
    protected var mPointerHaloWidth: Float = 0.toFloat()

    /**
     * The width of the pointer halo border (in pixels).
     */
    protected var mPointerHaloBorderWidth: Float = 0.toFloat()

    /**
     * Start angle of the CircularSeekBar.
     * Note: If mStartAngle and mEndAngle are set to the same angle, 0.1 is subtracted
     * from the mEndAngle to make the circle function properly.
     */
    protected var mStartAngle: Float = 0.toFloat()

    /**
     * End angle of the CircularSeekBar.
     * Note: If mStartAngle and mEndAngle are set to the same angle, 0.1 is subtracted
     * from the mEndAngle to make the circle function properly.
     */
    protected var mEndAngle: Float = 0.toFloat()

    /**
     * `RectF` that represents the circle (or ellipse) of the seekbar.
     */
    protected var mCircleRectF = RectF()

    /**
     * Holds the color value for `mPointerPaint` before the `Paint` instance is created.
     */
    protected var mPointerColor = DEFAULT_POINTER_COLOR

    /**
     * Holds the color value for `mPointerHaloPaint` before the `Paint` instance is created.
     */
    protected var mPointerHaloColor = DEFAULT_POINTER_HALO_COLOR

    /**
     * Holds the color value for `mPointerHaloPaint` before the `Paint` instance is created.
     */
    protected var mPointerHaloColorOnTouch = DEFAULT_POINTER_HALO_COLOR_ONTOUCH

    /**
     * Holds the color value for `mCirclePaint` before the `Paint` instance is created.
     */
    protected var mCircleColor = DEFAULT_CIRCLE_COLOR

    /**
     * Holds the color value for `mCircleFillPaint` before the `Paint` instance is created.
     */
    protected var mCircleFillColor = DEFAULT_CIRCLE_FILL_COLOR

    /**
     * Holds the color value for `mCircleProgressPaint` before the `Paint` instance is created.
     */
    protected var mCircleProgressColor = DEFAULT_CIRCLE_PROGRESS_COLOR

    /**
     * Holds the alpha value for `mPointerHaloPaint`.
     */
    protected var mPointerAlpha = DEFAULT_POINTER_ALPHA

    /**
     * Holds the OnTouch alpha value for `mPointerHaloPaint`.
     */
    protected var mPointerAlphaOnTouch = DEFAULT_POINTER_ALPHA_ONTOUCH

    /**
     * Distance (in degrees) that the the circle/semi-circle makes up.
     * This amount represents the max of the circle in degrees.
     */
    protected var mTotalCircleDegrees: Float = 0.toFloat()

    /**
     * Distance (in degrees) that the current progress makes up in the circle.
     */
    protected var mProgressDegrees: Float = 0.toFloat()

    /**
     * `Path` used to draw the circle/semi-circle.
     */
    protected var mCirclePath: Path? = null

    /**
     * `Path` used to draw the progress on the circle.
     */
    protected var mCircleProgressPath: Path? = null

    /**
     * Max value that this CircularSeekBar is representing.
     */
    protected var mMax: Int = 0

    /**
     * Progress value that this CircularSeekBar is representing.
     */
    protected var mProgress: Int = 0

    /**
     * If true, then the user can specify the X and Y radii.
     * If false, then the View itself determines the size of the CircularSeekBar.
     */
    protected var mCustomRadii: Boolean = false

    /**
     * Maintain a perfect circle (equal x and y radius), regardless of view or custom attributes.
     * The smaller of the two radii will always be used in this case.
     * The default is to be a circle and not an ellipse, due to the behavior of the ellipse.
     */
    protected var mMaintainEqualCircle: Boolean = false

    /**
     * Once a user has touched the circle, this determines if moving outside the circle is able
     * to change the position of the pointer (and in turn, the progress).
     */
    protected var mMoveOutsideCircle: Boolean = false

    /**
     * Used for enabling/disabling the lock option for easier hitting of the 0 progress mark.
     */
    /**
     * Get whether the pointer locks at zero and max.
     * @return Boolean value of true if the pointer locks at zero and max, false if it does not.
     */
    /**
     * Set whether the pointer locks at zero and max or not.
     * @param 'boolean value. True if the pointer should lock at zero and max, false if it should not.
     */
    var isLockEnabled = true

    /**
     * Used for when the user moves beyond the start of the circle when moving counter clockwise.
     * Makes it easier to hit the 0 progress mark.
     */
    protected var lockAtStart = true

    /**
     * Used for when the user moves beyond the end of the circle when moving clockwise.
     * Makes it easier to hit the 100% (max) progress mark.
     */
    protected var lockAtEnd = false

    /**
     * When the user is touching the circle on ACTION_DOWN, this is set to true.
     * Used when touching the CircularSeekBar.
     */
    protected var mUserIsMovingPointer = false

    /**
     * Represents the clockwise distance from `mStartAngle` to the touch angle.
     * Used when touching the CircularSeekBar.
     */
    protected var cwDistanceFromStart: Float = 0.toFloat()

    /**
     * Represents the counter-clockwise distance from `mStartAngle` to the touch angle.
     * Used when touching the CircularSeekBar.
     */
    protected var ccwDistanceFromStart: Float = 0.toFloat()

    /**
     * Represents the clockwise distance from `mEndAngle` to the touch angle.
     * Used when touching the CircularSeekBar.
     */
    protected var cwDistanceFromEnd: Float = 0.toFloat()

    /**
     * Represents the counter-clockwise distance from `mEndAngle` to the touch angle.
     * Used when touching the CircularSeekBar.
     * Currently unused, but kept just in case.
     */
    protected var ccwDistanceFromEnd: Float = 0.toFloat()

    /**
     * The previous touch action value for `cwDistanceFromStart`.
     * Used when touching the CircularSeekBar.
     */
    protected var lastCWDistanceFromStart: Float = 0.toFloat()

    /**
     * Represents the clockwise distance from `mPointerPosition` to the touch angle.
     * Used when touching the CircularSeekBar.
     */
    protected var cwDistanceFromPointer: Float = 0.toFloat()

    /**
     * Represents the counter-clockwise distance from `mPointerPosition` to the touch angle.
     * Used when touching the CircularSeekBar.
     */
    protected var ccwDistanceFromPointer: Float = 0.toFloat()

    /**
     * True if the user is moving clockwise around the circle, false if moving counter-clockwise.
     * Used when touching the CircularSeekBar.
     */
    protected var mIsMovingCW: Boolean = false

    /**
     * The width of the circle used in the `RectF` that is used to draw it.
     * Based on either the View width or the custom X radius.
     */
    protected var mCircleWidth: Float = 0.toFloat()

    /**
     * The height of the circle used in the `RectF` that is used to draw it.
     * Based on either the View width or the custom Y radius.
     */
    protected var mCircleHeight: Float = 0.toFloat()

    /**
     * Represents the progress mark on the circle, in geometric degrees.
     * This is not provided by the user; it is calculated;
     */
    protected var mPointerPosition: Float = 0.toFloat()

    /**
     * Pointer position in terms of X and Y coordinates.
     */
    protected var mPointerPositionXY = FloatArray(2)

    /**
     * Listener.
     */
    protected var mOnCircularSeekBarChangeListener: OnCircularSeekBarChangeListener? = null

    /**
     * True if user touch input is enabled, false if user touch input is ignored.
     * This does not affect setting values programmatically.
     */
    /**
     * Get whether user touch input is accepted.
     * @return Boolean value of true if user touch input is accepted, false if user touch input is ignored.
     */
    /**
     * Set whether user touch input is accepted or ignored.
     * @param 'boolean' value. True if user touch input is to be accepted, false if user touch input is to be ignored.
     */
    var isTouchEnabled = true

    /**
     * Get the progress of the CircularSeekBar.
     * @return The progress of the CircularSeekBar.
     */
    /**
     * Set the progress of the CircularSeekBar.
     * If the progress is the same, then any listener will not receive a onProgressChanged event.
     * @param progress The progress to set the CircularSeekBar to.
     */
    var progress: Int
        get() = Math.round(mMax.toFloat() * mProgressDegrees / mTotalCircleDegrees)
        set(progress) {
            if (mProgress != progress) {
                mProgress = progress
                if (mOnCircularSeekBarChangeListener != null) {
                    mOnCircularSeekBarChangeListener!!.onProgressChanged(this, progress, false)
                }

                recalculateAll()
                invalidate()
            }
        }

    /**
     * Gets the circle color.
     * @return An integer color value for the circle
     */
    /**
     * Sets the circle color.
     * @param color the color of the circle
     */
    var circleColor: Int
        get() = mCircleColor
        set(color) {
            mCircleColor = color
            mCirclePaint!!.color = mCircleColor
            invalidate()
        }

    /**
     * Gets the circle progress color.
     * @return An integer color value for the circle progress
     */
    /**
     * Sets the circle progress color.
     * @param color the color of the circle progress
     */
    var circleProgressColor: Int
        get() = mCircleProgressColor
        set(color) {
            mCircleProgressColor = color
            mCircleProgressPaint!!.color = mCircleProgressColor
            invalidate()
        }

    /**
     * Gets the pointer color.
     * @return An integer color value for the pointer
     */
    /**
     * Sets the pointer color.
     * @param color the color of the pointer
     */
    var pointerColor: Int
        get() = mPointerColor
        set(color) {
            mPointerColor = color
            mPointerPaint!!.color = mPointerColor
            invalidate()
        }

    /**
     * Gets the pointer halo color.
     * @return An integer color value for the pointer halo
     */
    /**
     * Sets the pointer halo color.
     * @param color the color of the pointer halo
     */
    var pointerHaloColor: Int
        get() = mPointerHaloColor
        set(color) {
            mPointerHaloColor = color
            mPointerHaloPaint!!.color = mPointerHaloColor
            invalidate()
        }

    /**
     * Gets the pointer alpha value.
     * @return An integer alpha value for the pointer (0..255)
     */
    /**
     * Sets the pointer alpha.
     * @param alpha the alpha of the pointer
     */
    var pointerAlpha: Int
        get() = mPointerAlpha
        set(alpha) {
            if (alpha >= 0 && alpha <= 255) {
                mPointerAlpha = alpha
                mPointerHaloPaint!!.alpha = mPointerAlpha
                invalidate()
            }
        }

    /**
     * Gets the pointer alpha value when touched.
     * @return An integer alpha value for the pointer (0..255) when touched
     */
    /**
     * Sets the pointer alpha when touched.
     * @param alpha the alpha of the pointer (0..255) when touched
     */
    var pointerAlphaOnTouch: Int
        get() = mPointerAlphaOnTouch
        set(alpha) {
            if (alpha >= 0 && alpha <= 255) {
                mPointerAlphaOnTouch = alpha
            }
        }

    /**
     * Gets the circle fill color.
     * @return An integer color value for the circle fill
     */
    /**
     * Sets the circle fill color.
     * @param color the color of the circle fill
     */
    var circleFillColor: Int
        get() = mCircleFillColor
        set(color) {
            mCircleFillColor = color
            mCircleFillPaint!!.color = mCircleFillColor
            invalidate()
        }

    /**
     * Get the current max of the CircularSeekBar.
     * @return Synchronized integer value of the max.
     */
    /**
     * Set the max of the CircularSeekBar.
     * If the new max is less than the current progress, then the progress will be set to zero.
     * If the progress is changed as a result, then any listener will receive a onProgressChanged event.
     * @param max The new max for the CircularSeekBar.
     */
    // Check to make sure it's greater than zero
    // If the new max is less than current progress, set progress to zero
    var max: Int
        @Synchronized get() = mMax
        set(max) {
            if (max > 0) {
                if (max <= mProgress) {
                    mProgress = 0
                    if (mOnCircularSeekBarChangeListener != null) {
                        mOnCircularSeekBarChangeListener!!.onProgressChanged(this, mProgress, false)
                    }
                }
                mMax = max

                recalculateAll()
                invalidate()
            }
        }

    /**
     * Initialize the CircularSeekBar with the attributes from the XML style.
     * Uses the defaults defined at the top of this file when an attribute is not specified by the user.
     * @param attrArray TypedArray containing the attributes.
     */
    protected fun initAttributes(attrArray: TypedArray) {
        mCircleXRadius = attrArray.getDimension(R.styleable.CircularSeekBar_circle_x_radius, DEFAULT_CIRCLE_X_RADIUS * DPTOPX_SCALE)
        mCircleYRadius = attrArray.getDimension(R.styleable.CircularSeekBar_circle_y_radius, DEFAULT_CIRCLE_Y_RADIUS * DPTOPX_SCALE)
        mPointerRadius = attrArray.getDimension(R.styleable.CircularSeekBar_pointer_radius, DEFAULT_POINTER_RADIUS * DPTOPX_SCALE)
        mPointerHaloWidth = attrArray.getDimension(R.styleable.CircularSeekBar_pointer_halo_width, DEFAULT_POINTER_HALO_WIDTH * DPTOPX_SCALE)
        mPointerHaloBorderWidth = attrArray.getDimension(R.styleable.CircularSeekBar_pointer_halo_border_width, DEFAULT_POINTER_HALO_BORDER_WIDTH * DPTOPX_SCALE)
        mCircleStrokeWidth = attrArray.getDimension(R.styleable.CircularSeekBar_circle_stroke_width, DEFAULT_CIRCLE_STROKE_WIDTH * DPTOPX_SCALE)

        mPointerColor = attrArray.getColor(R.styleable.CircularSeekBar_pointer_color, DEFAULT_POINTER_COLOR)
        mPointerHaloColor = attrArray.getColor(R.styleable.CircularSeekBar_pointer_halo_color, DEFAULT_POINTER_HALO_COLOR)
        mPointerHaloColorOnTouch = attrArray.getColor(R.styleable.CircularSeekBar_pointer_halo_color_ontouch, DEFAULT_POINTER_HALO_COLOR_ONTOUCH)
        mCircleColor = attrArray.getColor(R.styleable.CircularSeekBar_circle_color, DEFAULT_CIRCLE_COLOR)
        mCircleProgressColor = attrArray.getColor(R.styleable.CircularSeekBar_circle_progress_color, DEFAULT_CIRCLE_PROGRESS_COLOR)
        mCircleFillColor = attrArray.getColor(R.styleable.CircularSeekBar_circle_fill, DEFAULT_CIRCLE_FILL_COLOR)

        mPointerAlpha = Color.alpha(mPointerHaloColor)

        mPointerAlphaOnTouch = attrArray.getInt(R.styleable.CircularSeekBar_pointer_alpha_ontouch, DEFAULT_POINTER_ALPHA_ONTOUCH)
        if (mPointerAlphaOnTouch > 255 || mPointerAlphaOnTouch < 0) {
            mPointerAlphaOnTouch = DEFAULT_POINTER_ALPHA_ONTOUCH
        }

        mMax = attrArray.getInt(R.styleable.CircularSeekBar_max, DEFAULT_MAX)
        mProgress = attrArray.getInt(R.styleable.CircularSeekBar_progress, DEFAULT_PROGRESS)
        mCustomRadii = attrArray.getBoolean(R.styleable.CircularSeekBar_use_custom_radii, DEFAULT_USE_CUSTOM_RADII)
        mMaintainEqualCircle = attrArray.getBoolean(R.styleable.CircularSeekBar_maintain_equal_circle, DEFAULT_MAINTAIN_EQUAL_CIRCLE)
        mMoveOutsideCircle = attrArray.getBoolean(R.styleable.CircularSeekBar_move_outside_circle, DEFAULT_MOVE_OUTSIDE_CIRCLE)
        isLockEnabled = attrArray.getBoolean(R.styleable.CircularSeekBar_lock_enabled, DEFAULT_LOCK_ENABLED)

        // Modulo 360 right now to avoid constant conversion
        mStartAngle = (360f + attrArray.getFloat(R.styleable.CircularSeekBar_start_angle, DEFAULT_START_ANGLE) % 360f) % 360f
        mEndAngle = (360f + attrArray.getFloat(R.styleable.CircularSeekBar_end_angle, DEFAULT_END_ANGLE) % 360f) % 360f

        if (mStartAngle == mEndAngle) {
            //mStartAngle = mStartAngle + 1f;
            mEndAngle = mEndAngle - .1f
        }
    }

    /**
     * Initializes the `Paint` objects with the appropriate styles.
     */
    protected fun initPaints() {
        mCirclePaint = Paint()
        mCirclePaint!!.isAntiAlias = true
        mCirclePaint!!.isDither = true
        mCirclePaint!!.color = mCircleColor
        mCirclePaint!!.strokeWidth = mCircleStrokeWidth
        mCirclePaint!!.style = Paint.Style.STROKE
        mCirclePaint!!.strokeJoin = Paint.Join.ROUND
        mCirclePaint!!.strokeCap = Paint.Cap.ROUND

        mCircleFillPaint = Paint()
        mCircleFillPaint!!.isAntiAlias = true
        mCircleFillPaint!!.isDither = true
        mCircleFillPaint!!.color = mCircleFillColor
        mCircleFillPaint!!.style = Paint.Style.FILL

        mCircleProgressPaint = Paint()
        mCircleProgressPaint!!.isAntiAlias = true
        mCircleProgressPaint!!.isDither = true
        mCircleProgressPaint!!.color = mCircleProgressColor
        mCircleProgressPaint!!.strokeWidth = mCircleStrokeWidth
        mCircleProgressPaint!!.style = Paint.Style.STROKE
        mCircleProgressPaint!!.strokeJoin = Paint.Join.ROUND
        mCircleProgressPaint!!.strokeCap = Paint.Cap.ROUND

        mCircleProgressGlowPaint = Paint()
        mCircleProgressGlowPaint!!.set(mCircleProgressPaint)
        mCircleProgressGlowPaint!!.maskFilter = BlurMaskFilter(5f * DPTOPX_SCALE, BlurMaskFilter.Blur.NORMAL)

        mPointerPaint = Paint()
        mPointerPaint!!.isAntiAlias = true
        mPointerPaint!!.isDither = true
        mPointerPaint!!.style = Paint.Style.FILL
        mPointerPaint!!.color = mPointerColor
        mPointerPaint!!.strokeWidth = mPointerRadius

        mPointerHaloPaint = Paint()
        mPointerHaloPaint!!.set(mPointerPaint)
        mPointerHaloPaint!!.color = mPointerHaloColor
        mPointerHaloPaint!!.alpha = mPointerAlpha
        mPointerHaloPaint!!.strokeWidth = mPointerRadius + mPointerHaloWidth

        mPointerHaloBorderPaint = Paint()
        mPointerHaloBorderPaint!!.set(mPointerPaint)
        mPointerHaloBorderPaint!!.strokeWidth = mPointerHaloBorderWidth
        mPointerHaloBorderPaint!!.style = Paint.Style.STROKE

    }

    /**
     * Calculates the total degrees between mStartAngle and mEndAngle, and sets mTotalCircleDegrees
     * to this value.
     */
    protected fun calculateTotalDegrees() {
        mTotalCircleDegrees = (360f - (mStartAngle - mEndAngle)) % 360f // Length of the entire circle/arc
        if (mTotalCircleDegrees <= 0f) {
            mTotalCircleDegrees = 360f
        }
    }

    /**
     * Calculate the degrees that the progress represents. Also called the sweep angle.
     * Sets mProgressDegrees to that value.
     */
    protected fun calculateProgressDegrees() {
        mProgressDegrees = mPointerPosition - mStartAngle // Verified
        mProgressDegrees = if (mProgressDegrees < 0) 360f + mProgressDegrees else mProgressDegrees // Verified
    }

    /**
     * Calculate the pointer position (and the end of the progress arc) in degrees.
     * Sets mPointerPosition to that value.
     */
    protected fun calculatePointerAngle() {
        val progressPercent = mProgress.toFloat() / mMax.toFloat()
        mPointerPosition = progressPercent * mTotalCircleDegrees + mStartAngle
        mPointerPosition = mPointerPosition % 360f
    }

    protected fun calculatePointerXYPosition() {
        var pm = PathMeasure(mCircleProgressPath, false)
        var returnValue = pm.getPosTan(pm.length, mPointerPositionXY, null)
        if (!returnValue) {
            pm = PathMeasure(mCirclePath, false)
            returnValue = pm.getPosTan(0f, mPointerPositionXY, null)
        }
    }

    /**
     * Initialize the `Path` objects with the appropriate values.
     */
    protected fun initPaths() {
        mCirclePath = Path()
        mCirclePath!!.addArc(mCircleRectF, mStartAngle, mTotalCircleDegrees)

        mCircleProgressPath = Path()
        mCircleProgressPath!!.addArc(mCircleRectF, mStartAngle, mProgressDegrees)
    }

    /**
     * Initialize the `RectF` objects with the appropriate values.
     */
    protected fun initRects() {
        mCircleRectF.set(-mCircleWidth, -mCircleHeight, mCircleWidth, mCircleHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.translate((this.width / 2).toFloat(), (this.height / 2).toFloat())

        canvas.drawPath(mCirclePath, mCirclePaint)
        canvas.drawPath(mCircleProgressPath, mCircleProgressGlowPaint)
        canvas.drawPath(mCircleProgressPath, mCircleProgressPaint)

        canvas.drawPath(mCirclePath, mCircleFillPaint)

        canvas.drawCircle(mPointerPositionXY[0], mPointerPositionXY[1], mPointerRadius + mPointerHaloWidth, mPointerHaloPaint)
        canvas.drawCircle(mPointerPositionXY[0], mPointerPositionXY[1], mPointerRadius, mPointerPaint)
        if (mUserIsMovingPointer) {
            canvas.drawCircle(mPointerPositionXY[0], mPointerPositionXY[1], mPointerRadius + mPointerHaloWidth + mPointerHaloBorderWidth / 2f, mPointerHaloBorderPaint)
        }
    }

    protected fun setProgressBasedOnAngle(angle: Float) {
        mPointerPosition = angle
        calculateProgressDegrees()
        mProgress = Math.round(mMax.toFloat() * mProgressDegrees / mTotalCircleDegrees)
    }

    protected fun recalculateAll() {
        calculateTotalDegrees()
        calculatePointerAngle()
        calculateProgressDegrees()

        initRects()

        initPaths()

        calculatePointerXYPosition()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = View.getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        if (mMaintainEqualCircle) {
            val min = Math.min(width, height)
            setMeasuredDimension(min, min)
        } else {
            setMeasuredDimension(width, height)
        }

        // Set the circle width and height based on the view for the moment
        mCircleHeight = height.toFloat() / 2f - mCircleStrokeWidth - mPointerRadius - mPointerHaloBorderWidth * 1.5f
        mCircleWidth = width.toFloat() / 2f - mCircleStrokeWidth - mPointerRadius - mPointerHaloBorderWidth * 1.5f

        // If it is not set to use custom
        if (mCustomRadii) {
            // Check to make sure the custom radii are not out of the view. If they are, just use the view values
            if (mCircleYRadius - mCircleStrokeWidth - mPointerRadius - mPointerHaloBorderWidth < mCircleHeight) {
                mCircleHeight = mCircleYRadius - mCircleStrokeWidth - mPointerRadius - mPointerHaloBorderWidth * 1.5f
            }

            if (mCircleXRadius - mCircleStrokeWidth - mPointerRadius - mPointerHaloBorderWidth < mCircleWidth) {
                mCircleWidth = mCircleXRadius - mCircleStrokeWidth - mPointerRadius - mPointerHaloBorderWidth * 1.5f
            }
        }

        if (mMaintainEqualCircle) { // Applies regardless of how the values were determined
            val min = Math.min(mCircleHeight, mCircleWidth)
            mCircleHeight = min
            mCircleWidth = min
        }

        recalculateAll()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isTouchEnabled) {
            return false
        }

        // Convert coordinates to our internal coordinate system
        val x = event.x - width / 2
        val y = event.y - height / 2

        // Get the distance from the center of the circle in terms of x and y
        val distanceX = mCircleRectF.centerX() - x
        val distanceY = mCircleRectF.centerY() - y

        // Get the distance from the center of the circle in terms of a radius
        val touchEventRadius = Math.sqrt(Math.pow(distanceX.toDouble(), 2.0) + Math.pow(distanceY.toDouble(), 2.0)).toFloat()

        val minimumTouchTarget = MIN_TOUCH_TARGET_DP * DPTOPX_SCALE // Convert minimum touch target into px
        var additionalRadius: Float // Either uses the minimumTouchTarget size or larger if the ring/pointer is larger

        if (mCircleStrokeWidth < minimumTouchTarget) { // If the width is less than the minimumTouchTarget, use the minimumTouchTarget
            additionalRadius = minimumTouchTarget / 2
        } else {
            additionalRadius = mCircleStrokeWidth / 2 // Otherwise use the width
        }
        val outerRadius = Math.max(mCircleHeight, mCircleWidth) + additionalRadius // Max outer radius of the circle, including the minimumTouchTarget or wheel width
        val innerRadius = Math.min(mCircleHeight, mCircleWidth) - additionalRadius // Min inner radius of the circle, including the minimumTouchTarget or wheel width

        if (mPointerRadius < minimumTouchTarget / 2) { // If the pointer radius is less than the minimumTouchTarget, use the minimumTouchTarget
            additionalRadius = minimumTouchTarget / 2
        } else {
            additionalRadius = mPointerRadius // Otherwise use the radius
        }

        var touchAngle: Float
        touchAngle = (Math.atan2(y.toDouble(), x.toDouble()) / Math.PI * 180 % 360).toFloat() // Verified
        touchAngle = if (touchAngle < 0) 360 + touchAngle else touchAngle // Verified

        cwDistanceFromStart = touchAngle - mStartAngle // Verified
        cwDistanceFromStart = if (cwDistanceFromStart < 0) 360f + cwDistanceFromStart else cwDistanceFromStart // Verified
        ccwDistanceFromStart = 360f - cwDistanceFromStart // Verified

        cwDistanceFromEnd = touchAngle - mEndAngle // Verified
        cwDistanceFromEnd = if (cwDistanceFromEnd < 0) 360f + cwDistanceFromEnd else cwDistanceFromEnd // Verified
        ccwDistanceFromEnd = 360f - cwDistanceFromEnd // Verified

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // These are only used for ACTION_DOWN for handling if the pointer was the part that was touched
                val pointerRadiusDegrees = (mPointerRadius * 180 / (Math.PI * Math.max(mCircleHeight, mCircleWidth))).toFloat()
                cwDistanceFromPointer = touchAngle - mPointerPosition
                cwDistanceFromPointer = if (cwDistanceFromPointer < 0) 360f + cwDistanceFromPointer else cwDistanceFromPointer
                ccwDistanceFromPointer = 360f - cwDistanceFromPointer
                // This is for if the first touch is on the actual pointer.
                if (touchEventRadius >= innerRadius && touchEventRadius <= outerRadius && (cwDistanceFromPointer <= pointerRadiusDegrees || ccwDistanceFromPointer <= pointerRadiusDegrees)) {
                    setProgressBasedOnAngle(mPointerPosition)
                    lastCWDistanceFromStart = cwDistanceFromStart
                    mIsMovingCW = true
                    mPointerHaloPaint!!.alpha = mPointerAlphaOnTouch
                    mPointerHaloPaint!!.color = mPointerHaloColorOnTouch
                    recalculateAll()
                    invalidate()
                    if (mOnCircularSeekBarChangeListener != null) {
                        mOnCircularSeekBarChangeListener!!.onStartTrackingTouch(this)
                    }
                    mUserIsMovingPointer = true
                    lockAtEnd = false
                    lockAtStart = false
                } else if (cwDistanceFromStart > mTotalCircleDegrees) { // If the user is touching outside of the start AND end
                    mUserIsMovingPointer = false
                    return false
                } else if (touchEventRadius >= innerRadius && touchEventRadius <= outerRadius) { // If the user is touching near the circle
                    setProgressBasedOnAngle(touchAngle)
                    lastCWDistanceFromStart = cwDistanceFromStart
                    mIsMovingCW = true
                    mPointerHaloPaint!!.alpha = mPointerAlphaOnTouch
                    mPointerHaloPaint!!.color = mPointerHaloColorOnTouch
                    recalculateAll()
                    invalidate()
                    if (mOnCircularSeekBarChangeListener != null) {
                        mOnCircularSeekBarChangeListener!!.onStartTrackingTouch(this)
                        mOnCircularSeekBarChangeListener!!.onProgressChanged(this, mProgress, true)
                    }
                    mUserIsMovingPointer = true
                    lockAtEnd = false
                    lockAtStart = false
                } else { // If the user is not touching near the circle
                    mUserIsMovingPointer = false
                    return false
                }
            }
            MotionEvent.ACTION_MOVE -> if (mUserIsMovingPointer) {
                if (lastCWDistanceFromStart < cwDistanceFromStart) {
                    if (cwDistanceFromStart - lastCWDistanceFromStart > 180f && !mIsMovingCW) {
                        lockAtStart = true
                        lockAtEnd = false
                    } else {
                        mIsMovingCW = true
                    }
                } else {
                    if (lastCWDistanceFromStart - cwDistanceFromStart > 180f && mIsMovingCW) {
                        lockAtEnd = true
                        lockAtStart = false
                    } else {
                        mIsMovingCW = false
                    }
                }

                if (lockAtStart && mIsMovingCW) {
                    lockAtStart = false
                }
                if (lockAtEnd && !mIsMovingCW) {
                    lockAtEnd = false
                }
                if (lockAtStart && !mIsMovingCW && ccwDistanceFromStart > 90) {
                    lockAtStart = false
                }
                if (lockAtEnd && mIsMovingCW && cwDistanceFromEnd > 90) {
                    lockAtEnd = false
                }
                // Fix for passing the end of a semi-circle quickly
                if (!lockAtEnd && cwDistanceFromStart > mTotalCircleDegrees && mIsMovingCW && lastCWDistanceFromStart < mTotalCircleDegrees) {
                    lockAtEnd = true
                }

                if (lockAtStart && isLockEnabled) {
                    // TODO: Add a check if mProgress is already 0, in which case don't call the listener
                    mProgress = 0
                    recalculateAll()
                    invalidate()
                    if (mOnCircularSeekBarChangeListener != null) {
                        mOnCircularSeekBarChangeListener!!.onProgressChanged(this, mProgress, true)
                    }

                } else if (lockAtEnd && isLockEnabled) {
                    mProgress = mMax
                    recalculateAll()
                    invalidate()
                    if (mOnCircularSeekBarChangeListener != null) {
                        mOnCircularSeekBarChangeListener!!.onProgressChanged(this, mProgress, true)
                    }
                } else if (mMoveOutsideCircle || touchEventRadius <= outerRadius) {
                    if (cwDistanceFromStart <= mTotalCircleDegrees) {
                        setProgressBasedOnAngle(touchAngle)
                    }
                    recalculateAll()
                    invalidate()
                    if (mOnCircularSeekBarChangeListener != null) {
                        mOnCircularSeekBarChangeListener!!.onProgressChanged(this, mProgress, true)
                    }
                } else {
                    //break
                }

                lastCWDistanceFromStart = cwDistanceFromStart
            } else {
                return false
            }
            MotionEvent.ACTION_UP -> {
                mPointerHaloPaint!!.alpha = mPointerAlpha
                mPointerHaloPaint!!.color = mPointerHaloColor
                if (mUserIsMovingPointer) {
                    mUserIsMovingPointer = false
                    invalidate()
                    if (mOnCircularSeekBarChangeListener != null) {
                        mOnCircularSeekBarChangeListener!!.onStopTrackingTouch(this)
                    }
                } else {
                    return false
                }
            }
            MotionEvent.ACTION_CANCEL // Used when the parent view intercepts touches for things like scrolling
            -> {
                mPointerHaloPaint!!.alpha = mPointerAlpha
                mPointerHaloPaint!!.color = mPointerHaloColor
                mUserIsMovingPointer = false
                invalidate()
            }
        }

        if (event.action == MotionEvent.ACTION_MOVE && parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }

        return true
    }

    protected fun init(attrs: AttributeSet?, defStyle: Int) {
        val attrArray = context.obtainStyledAttributes(attrs, R.styleable.CircularSeekBar, defStyle, 0)

        initAttributes(attrArray)

        attrArray.recycle()

        initPaints()
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        val state = Bundle()
        state.putParcelable("PARENT", superState)
        state.putInt("MAX", mMax)
        state.putInt("PROGRESS", mProgress)
        state.putInt("mCircleColor", mCircleColor)
        state.putInt("mCircleProgressColor", mCircleProgressColor)
        state.putInt("mPointerColor", mPointerColor)
        state.putInt("mPointerHaloColor", mPointerHaloColor)
        state.putInt("mPointerHaloColorOnTouch", mPointerHaloColorOnTouch)
        state.putInt("mPointerAlpha", mPointerAlpha)
        state.putInt("mPointerAlphaOnTouch", mPointerAlphaOnTouch)
        state.putBoolean("lockEnabled", isLockEnabled)
        state.putBoolean("isTouchEnabled", isTouchEnabled)

        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as Bundle

        val superState = savedState.getParcelable<Parcelable>("PARENT")
        super.onRestoreInstanceState(superState)

        mMax = savedState.getInt("MAX")
        mProgress = savedState.getInt("PROGRESS")
        mCircleColor = savedState.getInt("mCircleColor")
        mCircleProgressColor = savedState.getInt("mCircleProgressColor")
        mPointerColor = savedState.getInt("mPointerColor")
        mPointerHaloColor = savedState.getInt("mPointerHaloColor")
        mPointerHaloColorOnTouch = savedState.getInt("mPointerHaloColorOnTouch")
        mPointerAlpha = savedState.getInt("mPointerAlpha")
        mPointerAlphaOnTouch = savedState.getInt("mPointerAlphaOnTouch")
        isLockEnabled = savedState.getBoolean("lockEnabled")
        isTouchEnabled = savedState.getBoolean("isTouchEnabled")

        initPaints()

        recalculateAll()
    }

    fun setOnSeekBarChangeListener(l: OnCircularSeekBarChangeListener) {
        mOnCircularSeekBarChangeListener = l
    }

    /**
     * Listener for the CircularSeekBar. Implements the same methods as the normal OnSeekBarChangeListener.
     */
    interface OnCircularSeekBarChangeListener {

        fun onProgressChanged(circularSeekBar: CircularSeekBar, progress: Int, fromUser: Boolean)

        fun onStopTrackingTouch(seekBar: CircularSeekBar)

        fun onStartTrackingTouch(seekBar: CircularSeekBar)
    }

    companion object {

        // Default values
        protected val DEFAULT_CIRCLE_X_RADIUS = 30f
        protected val DEFAULT_CIRCLE_Y_RADIUS = 30f
        protected val DEFAULT_POINTER_RADIUS = 4.5f
        protected val DEFAULT_POINTER_HALO_WIDTH = 2.5f
        protected val DEFAULT_POINTER_HALO_BORDER_WIDTH = 2f
        protected val DEFAULT_CIRCLE_STROKE_WIDTH = 1f
        protected val DEFAULT_START_ANGLE = 270f // Geometric (clockwise, relative to 3 o'clock)
        protected val DEFAULT_END_ANGLE = 270f // Geometric (clockwise, relative to 3 o'clock)
        protected val DEFAULT_MAX = 100
        protected val DEFAULT_PROGRESS = 0
        protected val DEFAULT_CIRCLE_COLOR = Color.DKGRAY
        protected val DEFAULT_CIRCLE_PROGRESS_COLOR = Color.argb(235, 74, 138, 255)
        protected val DEFAULT_POINTER_COLOR = Color.argb(235, 74, 138, 255)
        protected val DEFAULT_POINTER_HALO_COLOR = Color.argb(135, 74, 138, 255)
        protected val DEFAULT_POINTER_HALO_COLOR_ONTOUCH = Color.argb(135, 74, 138, 255)
        protected val DEFAULT_CIRCLE_FILL_COLOR = Color.TRANSPARENT
        protected val DEFAULT_POINTER_ALPHA = 135
        protected val DEFAULT_POINTER_ALPHA_ONTOUCH = 100
        protected val DEFAULT_USE_CUSTOM_RADII = false
        protected val DEFAULT_MAINTAIN_EQUAL_CIRCLE = true
        protected val DEFAULT_MOVE_OUTSIDE_CIRCLE = false
        protected val DEFAULT_LOCK_ENABLED = true
    }

}
