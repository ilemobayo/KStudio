package com.musicplayer.aow.delegates.scheduler.jobs

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService
import com.musicplayer.aow.application.MusicPlayerApplication

/**
 * Created by yboyar on 3/20/16.
 */
class MyGcmJobService : GcmJobSchedulerService() {
    override fun getJobManager(): JobManager {
        return MusicPlayerApplication.instance!!.getJobManager()
    }
}