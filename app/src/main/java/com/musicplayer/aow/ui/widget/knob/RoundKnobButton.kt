package com.musicplayer.aow.ui.widget.knob

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.RelativeLayout

import com.musicplayer.aow.R

class RoundKnobButton// set initial state
// enable gesture detector
/**
 *
 * Ring with selector = back
 *
 */(context: Context) : RelativeLayout(context), OnGestureListener {

    private var m_Context: Context = context
    private lateinit var gestureDetector: GestureDetector
    private var mAngleDown: Float = 0.toFloat()
    private var mAngleUp:Float = 0.toFloat()
    private var ivRotor: ImageView? = null
    private var bmpRotorOn: Bitmap? = null
    private var bmpRotorOff:Bitmap? = null
    private var mState = false
    private var m_nWidth = 100
    private var m_nHeight = 100
    var back = R.drawable.stator
    var rotoron = R.drawable.rotoron
    var rotoroff = R.drawable.rotoroff

    fun getM_nWidth(): Int {
        return m_nWidth
    }

    fun setM_nWidth(m_nWidth: Int) {
        this.m_nWidth = m_nWidth
    }

    fun getM_nHeight(): Int {
        return m_nHeight
    }

    fun setM_nHeight(m_nHeight: Int) {
        this.m_nHeight = m_nHeight
    }

    interface RoundKnobButtonListener {
        fun onStateChange(newstate: Boolean)
        fun onRotate(percentage: Int)
    }

    private var m_listener: RoundKnobButtonListener? = null

    fun SetListener(l: RoundKnobButtonListener) {
        m_listener = l
    }

    fun SetState(state: Boolean) {
        mState = state
        ivRotor!!.setImageBitmap(if (state) bmpRotorOn else bmpRotorOff)
    }

    fun redraw(context: Context) {
        // create stator
        val ivBack = ImageView(context)
        ivBack.setImageResource(back)
        val lp_ivBack = RelativeLayout.LayoutParams(
                m_nWidth, m_nHeight)
        lp_ivBack.addRule(RelativeLayout.CENTER_IN_PARENT)
        addView(ivBack, lp_ivBack)
        // load rotor images
        val srcon = BitmapFactory.decodeResource(context.resources, rotoron)
        val srcoff = BitmapFactory.decodeResource(context.resources, rotoroff)
        val scaleWidth = m_nWidth.toFloat() / srcon.width
        val scaleHeight = m_nHeight.toFloat() / srcon.height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        bmpRotorOn = Bitmap.createBitmap(
                srcon, 0, 0,
                srcon.width, srcon.height, matrix, true)
        bmpRotorOff = Bitmap.createBitmap(
                srcoff, 0, 0,
                srcoff.width, srcoff.height, matrix, true)
        // create rotor
        ivRotor = ImageView(context)
        ivRotor!!.setImageBitmap(bmpRotorOn)
        val lp_ivKnob = RelativeLayout.LayoutParams(m_nWidth, m_nHeight)//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp_ivKnob.addRule(RelativeLayout.CENTER_IN_PARENT)
        addView(ivRotor, lp_ivKnob)
    }

    /**
     * math..
     * @param x
     * @param y
     * @return
     */
    private fun cartesianToPolar(x: Float, y: Float): Float {
        return (-Math.toDegrees(Math.atan2((x - 0.5f).toDouble(), (y - 0.5f).toDouble()))).toFloat()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event))
            true
        else
            super.onTouchEvent(event)
    }

    override fun onDown(event: MotionEvent): Boolean {
        val x = event.x / width.toFloat()
        val y = event.y / height.toFloat()
        mAngleDown = cartesianToPolar(1 - x, 1 - y)// 1- to correct our custom axis direction
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val x = e.x / width.toFloat()
        val y = e.y / height.toFloat()
        mAngleUp = cartesianToPolar(1 - x, 1 - y)// 1- to correct our custom axis direction

        // if we click up the same place where we clicked down, it's just a button press
        if (!java.lang.Float.isNaN(mAngleDown) && !java.lang.Float.isNaN(mAngleUp) && Math.abs(mAngleUp - mAngleDown) < 10) {
            SetState(!mState)
            if (m_listener != null) m_listener!!.onStateChange(mState)
        }
        return true
    }

    fun setRotorPosAngle(deg: Float) {
        var deg = deg

        if (deg >= 210 || deg <= 150) {
            if (deg > 180) deg = deg - 360
            val matrix = Matrix()
            ivRotor!!.scaleType = ScaleType.MATRIX
            matrix.postRotate(deg, (m_nWidth / 2).toFloat(), (m_nHeight / 2).toFloat())//getWidth()/2, getHeight()/2);
            ivRotor!!.imageMatrix = matrix
        }
    }

    fun setRotorPercentage(percentage: Int) {
        var posDegree = percentage * 3 - 150
        if (posDegree < 0) posDegree = 360 + posDegree
        setRotorPosAngle(posDegree.toFloat())
    }


    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        val x = e2.x / width.toFloat()
        val y = e2.y / height.toFloat()
        val rotDegrees = cartesianToPolar(1 - x, 1 - y)// 1- to correct our custom axis direction

        if (!java.lang.Float.isNaN(rotDegrees)) {
            // instead of getting 0-> 180, -180 0 , we go for 0 -> 360
            var posDegrees = rotDegrees
            if (rotDegrees < 0) posDegrees = 360 + rotDegrees

            // deny full rotation, start start and stop point, and get a linear scale
            if (posDegrees > 210 || posDegrees < 150) {
                // rotate our imageview
                setRotorPosAngle(posDegrees)
                // get a linear scale
                val scaleDegrees = rotDegrees + 150 // given the current parameters, we go from 0 to 300
                // get position percent
                val percent = (scaleDegrees / 3).toInt()
                if (m_listener != null) m_listener!!.onRotate(percent)
                return true //consumed
            } else
                return false
        } else
            return false // not consumed
    }

    override fun onShowPress(e: MotionEvent) {
        // TODO Auto-generated method stub

    }

    override fun onFling(arg0: MotionEvent, arg1: MotionEvent, arg2: Float, arg3: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {}

    init {
        redraw(m_Context)
        SetState(mState)
        gestureDetector = GestureDetector(getContext(), this)
    }

}
