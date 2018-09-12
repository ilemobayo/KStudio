package com.musicplayer.aow.bus

import android.arch.lifecycle.MutableLiveData

/**
 * Created with Android Studio.
 * Desc: An EventBus powered by RxJava.
 * But before you use this RxBus, bear in mind this very IMPORTANT note:
 * - Be very careful when error occurred here, this can terminate the whole
 * event observer pattern. If one error ever happened, new events won't be
 * received because this subscription has be terminated after onError(Throwable).
 */

class RxBus {

    var playEvent: MutableLiveData<Any> = MutableLiveData()

    companion object {

        private val TAG = "LiveDataBus"

        @Volatile private var sInstance: RxBus? = RxBus()

        val instance: RxBus?
            get() {
                if (sInstance == null) {
                    synchronized(RxBus::class.java) {
                        if (sInstance == null) {
                            sInstance = RxBus()
                        }
                    }
                }
                return sInstance
            }

        /**
         * A simple logger for RxBus which can also prevent
         * potential crash(OnErrorNotImplementedException) caused by error in the workflow.
         */
//        fun defaultSubscriber(): Subscriber<Any> {
//            return object : Subscriber<Any>() {
//                override fun onCompleted() {
//                    Log.d(TAG, "Duty off!!!")
//                }
//
//                override fun onError(e: Throwable) {
//                    Log.e(TAG, "Error? Please solve this as soon as possible!", e)
//                }
//
//                override fun onNext(o: Any) {
//                    Log.d(TAG, "New event received: " + o)
//                }
//            }
//        }
    }
}
