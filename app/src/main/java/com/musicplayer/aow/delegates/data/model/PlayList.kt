package com.musicplayer.aow.delegates.data.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.TypeConverters
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.NonNull
import com.litesuits.orm.db.annotation.Unique
import com.musicplayer.aow.delegates.player.PlayMode
import com.musicplayer.aow.delegates.player.SongResolver
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "playlist")
@TypeConverters(DataConverter::class)
class PlayList : Parcelable {
    var id: Int = 0

    @Ignore
    var _id: Long = 0

    @Unique
    @NonNull
    @android.arch.persistence.room.PrimaryKey(autoGenerate = false)
    var mxp_id: String? = "0951"

    var name: String? = null

    var picture: String? = null

    var owner: String? = null

    var numberOfStreams = 0

    var numberOfLikes = 0

    var public: Boolean = false

    var dowloadable: Boolean = false

    var price: Int = 0

    var description: String? = ""

    var date_created: String? = ""

    var numOfSongs: Int = 0

    var isFavorite: Boolean = false

    var createdAt: String? = null

    var updatedAt: String? = null

    var songs: ArrayList<Song>? = ArrayList()

    var playingIndex: Int = -1

    /**
     * Use a singleton play mode
     */
    @Ignore
    var playMode: PlayMode? = PlayMode.LIST

    // Utils

    var itemCount: Int = 0
        get() = if (songs == null) 0 else songs!!.size

    /**
     * The current song being played or is playing based on the [.playingIndex]
     */
    @Ignore
    var currentSong: Song? = null
        get() = if (playingIndex != NO_POSITION) {
                songs!![playingIndex]
        } else null

    constructor() {
        // EMPTY
    }

    @Ignore
    constructor(mxp_id: String){
        this.mxp_id = mxp_id
    }

    @Ignore
    constructor(song: Song) {
        songs!!.add(song)
        numOfSongs = 1
    }

    @Ignore
    constructor(songList: ArrayList<Song>?) {
        songs!!.addAll(songList!!)
        numOfSongs = songs!!.size
    }

    @Ignore
    constructor(songList: List<Song>, name: String) {
        this.name = name
        songs!!.addAll(songList)
        numOfSongs = songs!!.size
    }

    @Ignore
    constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    // Parcelable

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.id)
        dest.writeString(this.name)
        dest.writeInt(this.numOfSongs)
        dest.writeByte(if (this.isFavorite) 1.toByte() else 0.toByte())
        dest.writeString(this.createdAt!!)
        dest.writeString(this.updatedAt!!)
        dest.writeTypedList(this.songs)
        dest.writeInt(this.playingIndex)
        dest.writeInt(if (this.playMode == null) -1 else this.playMode!!.ordinal)
    }

    fun readFromParcel(`in`: Parcel) {
        this.id = `in`.readInt()
        this.name = `in`.readString()
        this.numOfSongs = `in`.readInt()
        this.isFavorite = `in`.readByte().toInt() != 0
        this.createdAt = `in`.readString()
        this.updatedAt = `in`.readString()
        this.songs = `in`.createTypedArrayList(Song.CREATOR)
        this.playingIndex = `in`.readInt()
        val tmpPlayMode = `in`.readInt()
        this.playMode = if (tmpPlayMode == -1) null else PlayMode.values()[tmpPlayMode]
    }

    fun addSong(song: Song?) {
        if (song == null) return

        songs!!.add(song)
        numOfSongs = songs!!.size
    }

    fun addSong(song: Song?, index: Int) {
        if (song == null) return

        songs!!.add(index, song)
        numOfSongs = songs!!.size
    }

    fun addSong(songs: List<Song>?, index: Int) {
        if (songs == null || songs.isEmpty()) return

        this.songs!!.addAll(index, songs)
        this.numOfSongs = this.songs!!.size
    }

    fun removeSong(song: Song?): Boolean {
        if (song == null) return false

        val index = songs!!.indexOf(song)
        if (index != -1) {
            if (songs!!.removeAt(index) != null) {
                numOfSongs = songs!!.size
                return true
            }
        } else {
            val iterator = songs!!.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (song.path == item.path) {
                    iterator.remove()
                    numOfSongs = songs!!.size
                    return true
                }
            }
        }
        return false
    }

    /**
     * Prepare to play
     */
    fun prepare(): Boolean {
        if (songs!!.isEmpty()) return false
        if (playingIndex == NO_POSITION) {
            playingIndex = 0
        }
        return true
    }

    fun hasLast(): Boolean {
        return songs != null && songs!!.size != 0
    }

    fun last(): Song {
        when (playMode) {
            PlayMode.LOOP, PlayMode.LIST, PlayMode.SINGLE -> {
                var newIndex = playingIndex - 1
                if (newIndex < 0) {
                    newIndex = songs!!.size - 1
                }
                playingIndex = newIndex
            }
            PlayMode.SHUFFLE -> playingIndex = randomPlayIndex()
        }
        return songs!![playingIndex]
    }

    /**
     * @return Whether has next song to play.
     *
     *
     * If this query satisfies these conditions
     * - comes from media player's complete listener
     * - current play mode is PlayMode.LIST (the only limited play mode)
     * - current song is already in the end of the list
     * then there shouldn't be a next song to play, for this condition, it returns false.
     *
     *
     * If this query is from user's action, such as from play controls, there should always
     * has a next song to play, for this condition, it returns true.
     */
    fun hasNext(fromComplete: Boolean): Boolean {
        if (songs!!.isEmpty()) return false
        if (fromComplete) {
            if (playMode === PlayMode.LIST && playingIndex + 1 >= songs!!.size) return false
        }
        return true
    }

    /**
     * Move the playingIndex forward depends on the play mode
     *
     * @return The next song to play
     */
    operator fun next(): Song {
        when (playMode) {
            PlayMode.LOOP, PlayMode.LIST, PlayMode.SINGLE -> {
                var newIndex = playingIndex + 1
                if (newIndex >= songs!!.size) {
                    newIndex = 0
                }
                playingIndex = newIndex
            }
            PlayMode.SHUFFLE -> playingIndex = randomPlayIndex()
        }
        return songs!![playingIndex]
    }

    fun nextWithIndex(): SongResolver {
        when (playMode) {
            PlayMode.LOOP, PlayMode.LIST, PlayMode.SINGLE -> {
                var newIndex = playingIndex + 1
                if (newIndex >= songs!!.size) {
                    newIndex = 0
                }
                playingIndex = newIndex
            }
            PlayMode.SHUFFLE -> playingIndex = randomPlayIndex()
        }
        return SongResolver(songs!![playingIndex], playingIndex)
    }

    private fun randomPlayIndex(): Int {
        val randomIndex = Random().nextInt(songs!!.size)
        // Make sure not play the same song twice if there are at least 2 songs
        if (songs!!.size > 1 && randomIndex == playingIndex) {
            randomPlayIndex()
        }
        return randomIndex
    }

    companion object {

        // Play List: Favorite
        val NO_POSITION = -1
        val COLUMN_FAVORITE = "favorite"
        val COLUMN_NAME = "name"
        val ID = "id"
        val PLAYING_INDEX = "playing"
        val NUMBER_OF_SONGS = "sizeofsongs"

        val CREATOR: Parcelable.Creator<PlayList?> = object : Parcelable.Creator<PlayList?> {
            override fun createFromParcel(source: Parcel): PlayList? {
                return PlayList(source)
            }

            override fun newArray(size: Int): Array<PlayList?> {
                return arrayOfNulls(size)
            }
        }

        fun fromFolder(folder: Folder): PlayList {
            val playList = PlayList()
            playList.name = folder.name
            playList.songs = folder.songs as ArrayList<Song>?
            playList.numOfSongs = folder.numOfSongs
            return playList
        }
    }
}
