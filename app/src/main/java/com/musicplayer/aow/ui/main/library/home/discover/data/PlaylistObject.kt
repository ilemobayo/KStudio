package com.musicplayer.aow.ui.main.library.home.discover.data

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity(tableName = "discovery")
class PlaylistObject() {
    @NonNull
    @PrimaryKey(autoGenerate = false)
    var id: String? = null
    var name: String? = null
    var type: String? = null
    var public: String? = null
    var owner: String? = null
    var picture: String? = null
    var location: String? = null
    var dateCreated: String? = null
    @Embedded
    var memberItem: ArrayList<PlaylistObject>? = ArrayList()
    var des: String? = null

    
    override fun toString(): String {
        return "PlaylistObject(id=$id, name=$name, type=$type, public=$public, owner=$owner, picture=$picture, location=$location, dateCreated=$dateCreated, memberItem=$memberItem, des=$des)"
    }

}