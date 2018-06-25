package com.musicplayer.aow.delegates.data.model

import android.os.Parcel
import android.os.Parcelable
import com.litesuits.orm.db.annotation.PrimaryKey
import com.litesuits.orm.db.annotation.Table
import com.litesuits.orm.db.annotation.Unique
import com.litesuits.orm.db.enums.AssignType

@Table("song")
class Song : Parcelable {

    @PrimaryKey(AssignType.AUTO_INCREMENT)
    var id: Int = 0

    var title: String? = null

    var displayName: String? = null

    var artist: String? = null

    var album: String? = null

    @Unique
    var path: String? = null

    var duration: Int = 0

    var size: Int = 0

    var isFavorite: Boolean = false

    var numberOfPlay: Int = 0

    var dateAdded: String = ""

    var albumArt: String? = null

    constructor() {
        // Empty
    }

    constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    constructor(title: String?, displayName: String?, artist: String?, album: String?, path: String?, duration: Int, size: Int, isFavorite: Boolean, numberOfPlay: Int, dateAdded: String, albumArt: String?) {
        this.title = title
        this.displayName = displayName
        this.artist = artist
        this.album = album
        this.path = path
        this.duration = duration
        this.size = size
        this.isFavorite = isFavorite
        this.numberOfPlay = numberOfPlay
        this.dateAdded = dateAdded
        this.albumArt = albumArt
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.id)
        dest.writeString(this.title)
        dest.writeString(this.displayName)
        dest.writeString(this.artist)
        dest.writeString(this.album)
        dest.writeString(this.path)
        dest.writeInt(this.duration)
        dest.writeInt(this.size)
        dest.writeInt(if (this.isFavorite) 1 else 0)
    }

    fun readFromParcel(`in`: Parcel) {
        this.id = `in`.readInt()
        this.title = `in`.readString()
        this.displayName = `in`.readString()
        this.artist = `in`.readString()
        this.album = `in`.readString()
        this.path = `in`.readString()
        this.duration = `in`.readInt()
        this.size = `in`.readInt()
        this.isFavorite = `in`.readInt() == 1
    }

    override fun toString(): String {
        return "Song(id=$id, title=$title, displayName=$displayName, artist=$artist, album=$album, path=$path," +
                " duration=$duration, size=$size, isFavorite=$isFavorite, numberOfPlay=$numberOfPlay," +
                " dateAdded=$dateAdded, albumArt=$albumArt)"
    }


    companion object {

        val CREATOR: Parcelable.Creator<Song> = object : Parcelable.Creator<Song> {
            override fun createFromParcel(source: Parcel): Song {
                return Song(source)
            }

            override fun newArray(size: Int): Array<Song?> {
                return arrayOfNulls(size)
            }
        }
    }
}
