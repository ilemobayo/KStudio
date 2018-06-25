package com.musicplayer.aow.delegates.scheduler

import android.app.job.JobParameters
import android.app.job.JobService


/**
 * Created by Arca on 12/4/2017.
 */
class JobServices : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }

    companion object {
        private val TAG = "SyncService"
    }

}