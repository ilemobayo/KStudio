package com.musicplayer.aow.ui.base

import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.Loader
import android.view.View
import rx.Subscription
import rx.subscriptions.CompositeSubscription

abstract class BaseFragment : Fragment() {

    private var mSubscriptions: CompositeSubscription? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addSubscription(subscribeEvents())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mSubscriptions != null) {
            mSubscriptions!!.clear()
        }
    }

    protected open fun subscribeEvents(): Subscription? {
        return null
    }

    protected fun addSubscription(subscription: Subscription?) {
        if (subscription == null) return
        if (mSubscriptions == null) {
            mSubscriptions = CompositeSubscription()
        }
        mSubscriptions!!.add(subscription)
    }

    abstract fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?)
    abstract fun onLoaderReset(loader: Loader<Cursor>?)
}
