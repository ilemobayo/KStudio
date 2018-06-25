package com.musicplayer.aow.delegates.scheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context

/**
 * Created by Arca on 12/4/2017.
 */
class UtilServes {

    // schedule the start of the service every 10 - 30 seconds
    fun scheduleJob(context: Context) {
        var serviceComponent = ComponentName(context, JobServices::class.java);
        var builder: JobInfo.Builder = JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(1 * 1000); // wait at least
        builder.setOverrideDeadline(3 * 1000); // maximum delay
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        var jobScheduler: JobScheduler = context.getSystemService(JobScheduler::class.java);
        jobScheduler.schedule(builder.build());
    }

}