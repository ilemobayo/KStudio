package com.musicplayer.aow.delegates.data.source.db

import com.litesuits.orm.LiteOrm
import com.musicplayer.aow.BuildConfig
import com.musicplayer.aow.application.Injection

/**
 * Created with Android Studio.
 * User:
 * Date:
 * Time:
 * Desc: LiteOrmHelper
 */
object LiteOrmHelper {

    private val DB_NAME = "db.db"

    @Volatile private var sInstance: LiteOrm? = null

    val instance: LiteOrm?
        get() {
            if (sInstance == null) {
                synchronized(LiteOrmHelper::class.java) {
                    if (sInstance == null) {
                        sInstance = LiteOrm.newCascadeInstance(Injection.provideContext(), DB_NAME)
                        sInstance!!.setDebugged(BuildConfig.DEBUG)
                    }
                }
            }
            return sInstance
        }
}// Avoid direct instantiate
