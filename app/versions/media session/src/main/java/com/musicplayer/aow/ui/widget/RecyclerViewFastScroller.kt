package com.musicplayer.aow.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.musicplayer.aow.R

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com.musicpalyer.com.musicplayer.aow
 * Date: 9/2/16
 * Time: 9:17 PM
 * Desc: FastScroller
 */
class RecyclerViewFastScroller @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var bubbleView: TextView? = null
    private var fastScroll: View? = null
    private var recyclerView: RecyclerView? = null
    private var currentAnimator: ObjectAnimator? = null

    private var isDragging = false

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            if (!isDragging) {
                updateBubbleAndHandlePosition()
            }
        }
    }

    interface BubbleTextGetter {
        fun getTextToShowInBubble(position: Int): String
    }

    init {
        init()
    }

    protected fun init() {
        orientation = LinearLayout.HORIZONTAL
        clipChildren = false
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        bubbleView = findViewById<TextView>(R.id.bubble)
        bubbleView!!.visibility = View.GONE
        fastScroll = findViewById(R.id.handle)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBubbleAndHandlePosition()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x < fastScroll!!.x - ViewCompat.getPaddingStart(fastScroll))
                    return false
                if (currentAnimator != null)
                    currentAnimator!!.cancel()
                if (bubbleView != null && bubbleView!!.visibility == View.GONE)
                    showBubble()
                fastScroll!!.isSelected = true
                isDragging = true
                setBubbleAndHandlePosition(event.y)
                setRecyclerViewPosition(event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                isDragging = true
                setBubbleAndHandlePosition(event.y)
                setRecyclerViewPosition(event.y)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                fastScroll!!.isSelected = false
                hideBubble()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        if (this.recyclerView !== recyclerView) {
            if (this.recyclerView != null)
                this.recyclerView!!.removeOnScrollListener(onScrollListener)
            this.recyclerView = recyclerView
            recyclerView.addOnScrollListener(onScrollListener)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (recyclerView != null) {
            recyclerView!!.removeOnScrollListener(onScrollListener)
            recyclerView = null
        }
    }

    private fun setRecyclerViewPosition(y: Float) {
        if (recyclerView != null) {
            val itemCount = recyclerView!!.adapter.itemCount
            val proportion: Float
            proportion = y / (height - paddingTop).toFloat()

            val verticalScrollOffset = recyclerView!!.computeVerticalScrollOffset()
            val verticalScrollRange = recyclerView!!.computeVerticalScrollRange() - recyclerView!!.height + recyclerView!!.paddingTop + recyclerView!!.paddingBottom

            val offset = verticalScrollRange * proportion - verticalScrollOffset + 0.5f
            recyclerView!!.scrollBy(0, offset.toInt())

            /*
            Log.d(TAG, String.format("recyclerView[scrollOffset: %d, scrollRange: %d] " +
                            "fastScroll[y: %.2f] proportion: %.2f, height: %d",
                    verticalScrollOffset, verticalScrollRange, fastScroll.getY(), proportion, getHeight()));
            */
            val targetPos = getValueInRange(0, itemCount - 1, (proportion * itemCount.toFloat()).toInt())
            val bubbleText = (recyclerView!!.adapter as BubbleTextGetter).getTextToShowInBubble(targetPos)
            if (bubbleView != null)
                bubbleView!!.text = bubbleText
        }
    }

    private fun getValueInRange(min: Int, max: Int, value: Int): Int {
        val minimum = Math.max(min, value)
        return Math.min(minimum, max)
    }

    private fun updateBubbleAndHandlePosition() {
        if (recyclerView == null || bubbleView == null || fastScroll!!.isSelected)
            return

        val verticalScrollOffset = recyclerView!!.computeVerticalScrollOffset()
        val verticalScrollRange = recyclerView!!.computeVerticalScrollRange()
        val proportion = verticalScrollOffset.toFloat() / (verticalScrollRange.toFloat() - height)
        setBubbleAndHandlePosition(height * proportion)
    }

    private fun setBubbleAndHandlePosition(y: Float) {
        val handleHeight = fastScroll!!.height
        fastScroll!!.y = getValueInRange(
                paddingTop,
                height - handleHeight - paddingBottom,
                (y - handleHeight / 2).toInt()
        ).toFloat()
        if (bubbleView != null) {
            val bubbleHeight = bubbleView!!.height
            bubbleView!!.y = getValueInRange(
                    paddingTop,
                    height - bubbleHeight - handleHeight / 2 - paddingBottom,
                    (y - bubbleHeight).toInt()
            ).toFloat()
        }
    }

    private fun showBubble() {
        if (bubbleView == null)
            return
        bubbleView!!.visibility = View.VISIBLE
        if (currentAnimator != null)
            currentAnimator!!.cancel()
        currentAnimator = ObjectAnimator.ofFloat(bubbleView, onSetAlpha(2).toString(), 0f, 1f).setDuration(BUBBLE_ANIMATION_DURATION.toLong())
        currentAnimator!!.start()
    }

    private fun hideBubble() {
        if (bubbleView == null)
            return
        if (currentAnimator != null)
            currentAnimator!!.cancel()
        currentAnimator = ObjectAnimator.ofFloat(bubbleView, onSetAlpha(2).toString(), 1f, 0f).setDuration(BUBBLE_ANIMATION_DURATION.toLong())
        currentAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                bubbleView!!.visibility = View.GONE
                currentAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                bubbleView!!.visibility = View.GONE
                currentAnimator = null
            }
        })
        currentAnimator!!.start()
    }

    companion object {

        // private static final String TAG = "FastScroller";

        private val BUBBLE_ANIMATION_DURATION = 100
    }
}
