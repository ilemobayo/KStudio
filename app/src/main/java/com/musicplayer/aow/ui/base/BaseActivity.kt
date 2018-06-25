package com.musicplayer.aow.ui.base

import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.musicplayer.aow.R
import com.musicplayer.aow.utils.GradientUtils
import com.trello.rxlifecycle.ActivityEvent
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.util.concurrent.TimeUnit


abstract class BaseActivity : RxAppCompatActivity() {

    private var mSubscriptions: CompositeSubscription? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // https://crazygui.wordpress.com/2010/09/05/high-quality-radial-gradient-in-android/
        val displayMetrics = resources.displayMetrics
        // int screenWidth = displayMetrics.widthPixels;
        val screenHeight = displayMetrics.heightPixels

        val window = window
        val gradientBackgroundDrawable = GradientUtils.create(
                ContextCompat.getColor(this, R.color.mp_theme_dark_blue_gradientColor),
                ContextCompat.getColor(this, R.color.mp_theme_dark_blue_background),
                screenHeight / 2, // (int) Math.hypot(screenWidth / 2, screenHeight / 2),
                0.5f,
                0.5f
        )
        window.setBackgroundDrawable(gradientBackgroundDrawable)
        window.setFormat(PixelFormat.RGBA_8888)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSubscription(subscribeEvents())

        Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe()
        Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mSubscriptions != null) {
            mSubscriptions!!.clear()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * An easy way to set up non-home(no back button on the toolbar) activity to enable
     * go back action.
     *
     * @param toolbar The toolbar with go back button
     * @return ActionBar
     */
    protected fun supportActionBar(toolbar: Toolbar?): ActionBar? {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }
        return actionBar
    }

    protected fun addSubscription(subscription: Subscription?) {
        if (subscription == null) return
        if (mSubscriptions == null) {
            mSubscriptions = CompositeSubscription()
        }
        mSubscriptions!!.add(subscription)
    }

    protected open fun subscribeEvents(): Subscription? {
        return null
    }
}
