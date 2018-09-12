package com.musicplayer.aow.delegates.scheduler.jobs

import android.util.Log
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import com.musicplayer.aow.delegates.softcode.adapters.workers.GetAlbumArts

// A job to send a tweet
class AlbumArtJob(private val text: String)// This job requires network connectivity,
// and should be persisted in case the application exits before job is completed.
    //: Job(Params(PRIORITY).requireNetwork().persist()) {
    : Job(Params(PRIORITY).persist()) {

    override fun onAdded() {
        // Job has been saved to disk.
        // This is a good place to dispatch a UI event to indicate the job will eventually run.
        // In this example, it would be good to update the UI with the newly posted tweet.
    }

    @Throws(Throwable::class)
    override fun onRun() {
        // Job logic goes here. In this example, the network call to post to Twitter is done here.
        // All work done here should be synchronous, a job is removed from the queue once
        // onRun() finishes.
        //webservice.postTweet(text);
        GetAlbumArts(applicationContext).runIn()
        Log.e(this.javaClass.name, "i am running with $text")
    }

    override fun shouldReRunOnThrowable(throwable:Throwable, runCount:Int,
                                                  maxRunCount:Int): RetryConstraint {
        // An error occurred in onRun.
        // Return value determines whether this job should retry or cancel. You can further
        // specify a backoff strategy or change the job's priority. You can also apply the
        // delay to the whole group to preserve jobs' running order.
        return RetryConstraint.createExponentialBackoff(runCount, 1000)
    }

    override fun onCancel(cancelReason:Int, throwable:Throwable?) {
        // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
    }

    companion object {
        val PRIORITY = 1
    }
}