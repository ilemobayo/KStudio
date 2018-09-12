package com.musicplayer.aow.delegates.player

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.FileObserver
import android.os.Handler
import android.util.Log
import com.musicplayer.aow.application.InitDatabase
import com.musicplayer.aow.delegates.data.db.model.DiscoveryDBModel


class MyContentObserver(handler: Handler, var context: Context) : ContentObserver(handler) {

    private var refreshDatabase = InitDatabase(context)

    override fun onChange(selfChange: Boolean) {
        this.onChange(selfChange, null)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        //update all database table
        refreshDatabase.loadAllDatabase()
    }

}


class MyFileObserver(context: Context,path: String) : FileObserver(path, FileObserver.ALL_EVENTS) {
    var discoveryDBModel = DiscoveryDBModel(context)
    override fun onEvent(event: Int, path: String?) {
        if (path == null) {
            return
        }

        //a new file or subdirectory was created under the monitored directory
        if (FileObserver.CREATE and event != 0) {
            Log.e(this.javaClass.name, "file created")
        }

        //a file or directory was opened
        if (FileObserver.OPEN and event != 0) {
            Log.e(this.javaClass.name, "file opened")
        }

        //data was read from a file
        if (FileObserver.ACCESS and event != 0) {
            Log.e(this.javaClass.name, "file access")
        }

        //data was written to a file
        if (FileObserver.MODIFY and event != 0) {
            Log.e(this.javaClass.name, "file modified")
            discoveryDBModel.initPlaylist()
        }

        //someone has a file or directory open read-only, and closed it
        if (FileObserver.CLOSE_NOWRITE and event != 0) {
            Log.e(this.javaClass.name, "file closed")
        }

        //someone has a file or directory open for writing, and closed it
        if (FileObserver.CLOSE_WRITE and event != 0) {
            Log.e(this.javaClass.name, "file closed")
            discoveryDBModel.initPlaylist()
        }

        //[todo: consider combine this one with one below]
        //a file was deleted from the monitored directory
        if (FileObserver.DELETE and event != 0) {

            //for testing copy file

            // FileUtils.copyFile(absolutePath + "/" + path);

        }

        //the monitored file or directory was deleted, monitoring effectively stops
        if (FileObserver.DELETE_SELF and event != 0) {

        }

        //a file or subdirectory was moved from the monitored directory
        if (FileObserver.MOVED_FROM and event != 0) {

        }

        //a file or subdirectory was moved to the monitored directory
        if (FileObserver.MOVED_TO and event != 0) {

        }

        //the monitored file or directory was moved; monitoring continues
        if (FileObserver.MOVE_SELF and event != 0) {

        }

        //Metadata (permissions, owner, timestamp) was changed explicitly
        if (FileObserver.ATTRIB and event != 0) {

        }
    }
}
