package com.musicplayer.aow.delegates.scheduler.jobs

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService
import com.musicplayer.aow.application.MusicPlayerApplication

import java.util.Objects

class MyJobService : FrameworkJobSchedulerService() {
    override fun getJobManager(): JobManager {
        return MusicPlayerApplication.instance!!.getJobManager()
    }
}
