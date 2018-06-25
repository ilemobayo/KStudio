package com.musicplayer.aow.ui.widget

import android.content.Context
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout


/**
 * Created by Arca on 1/9/2018.
 */
class BottomNavigationBehavior : CoordinatorLayout.Behavior<BottomNavigationView> {

    var state: Boolean = false
    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun layoutDependsOn(parent: CoordinatorLayout, child: BottomNavigationView, dependency: View): Boolean {
        return dependency is FrameLayout
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: BottomNavigationView, directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: BottomNavigationView, target: View, dx: Int, dy: Int, consumed: IntArray) {
        if (dy < 0) {
            state = true
            showBottomNavigationView(child)
        } else if (dy > 0) {
            state = false
            hideBottomNavigationView(child)
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: BottomNavigationView, target: View ) {
        // Do nothing
        if (!state) {
//            Handler().postDelayed(
//            {
                showBottomNavigationView(child)
//            }, 2000)
        }
    }

    private fun hideBottomNavigationView(view: BottomNavigationView) {
        view.animate().translationY(view.height.toFloat())
    }

    private fun showBottomNavigationView(view: BottomNavigationView) {
        view.animate().translationY(0f)
    }
}