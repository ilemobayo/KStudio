package com.musicplayer.aow.bus

import android.util.Log
import rx.Observable
import rx.Subscriber
import rx.subjects.PublishSubject

/**
 * Created with Android Studio.
 * Desc: An EventBus powered by RxJava.
 * But before you use this RxBus, bear in mind this very IMPORTANT note:
 * - Be very careful when error occurred here, this can terminate the whole
 * event observer pattern. If one error ever happened, new events won't be
 * received because this subscription has be terminated after onError(Throwable).
 */

class RxBus {

    /**
     * PublishSubject<Object> subject = PublishSubject.create();
     * // observer1 will receive all onNext and onCompleted events
     * subject.subscribe(observer1);
     * subject.onNext("one");
     * subject.onNext("two");
     * // observer2 will only receive "three" and onCompleted
     * subject.subscribe(observer2);
     * subject.onNext("three");
     * subject.onCompleted();
    </Object> */
    private val mEventBus = PublishSubject.create<Any>()

    fun post(event: Any) {
        mEventBus.onNext(event)
    }

    fun toObservable(): Observable<Any> {
        return mEventBus
    }

    companion object {

        private val TAG = "RxBus"

        @Volatile private var sInstance: RxBus? = null

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
        fun defaultSubscriber(): Subscriber<Any> {
            return object : Subscriber<Any>() {
                override fun onCompleted() {
                    Log.d(TAG, "Duty off!!!")
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "Error? Please solve this as soon as possible!", e)
                }

                override fun onNext(o: Any) {
                    Log.d(TAG, "New event received: " + o)
                }
            }
        }
    }
}
