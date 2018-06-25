package com.musicplayer.aow.delegates.event

import java.io.File
import java.util.*

class AddFolderEvent {

    var folders: MutableList<File> = ArrayList()

    constructor(file: File) {
        folders.add(file)
    }

    constructor(files: MutableList<File>?) {
        if (files != null) {
            folders = files
        }
    }
}
