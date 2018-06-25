package com.musicplayer.aow.delegates.data.model

import android.os.Parcel
import android.os.Parcelable
import com.litesuits.orm.db.annotation.*
import com.litesuits.orm.db.enums.AssignType
import com.litesuits.orm.db.enums.Relation
import com.musicplayer.aow.delegates.player.PlayMode
import java.util.*

@Table("songs")
class Songs : Parcelable {

    @PrimaryKey(AssignType.AUTO_INCREMENT)
    var id: Int = 0

    var name: String? = null

    var numOfSongs: Int = 0

    @Column(COLUMN_FAVORITE)
    var isFavorite: Boolean = false

    var createdAt: Date? = null

    var updatedAt: Date? = null

//    @MapCollection(ArrayList<*>::class)
    @MapCollection(Collection::class)
    @Mapping(Relation.OneToMany)
    private var songs: MutableList<Song>? = ArrayList()

    @Ignore
    var playingIndex = -1

    /**
     * Use a singleton play mode
     */
    var playMode: PlayMode? = PlayMode.LOOP

    // Utils

    val itemCount: Int
        get() = if (songs == null) 0 else songs!!.size

    /**
     * The current song being played or is playing based on the [.playingIndex]
     */
    val currentSong: Song?
        get() = if (playingIndex != NO_POSITION) {
            songs!![playingIndex]
        } else null

    constructor() {
        // EMPTY
    }

    constructor(song: Song) {
        songs!!.add(song)
        numOfSongs = 1
    }

    constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    fun getSongs(): MutableList<Song>? {
        if (songs == null) {
            songs = ArrayList()
        }
        return songs
    }

    fun setSongs(songs: MutableList<Song>?) {
        var nSongs = songs
        if (nSongs == null) {
            nSongs = ArrayList()
        }
        this.songs = nSongs
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
        dest.writeLong(if (this.createdAt != null) this.createdAt!!.time else -1)
        dest.writeLong(if (this.updatedAt != null) this.updatedAt!!.time else -1)
        dest.writeTypedList(this.songs)
        dest.writeInt(this.playingIndex)
        dest.writeInt(if (this.playMode == null) -1 else this.playMode!!.ordinal)
    }

    fun readFromParcel(`in`: Parcel) {
        this.id = `in`.readInt()
        this.name = `in`.readString()
        this.numOfSongs = `in`.readInt()
        this.isFavorite = `in`.readByte().toInt() != 0
        val tmpCreatedAt = `in`.readLong()
        this.createdAt = if (tmpCreatedAt == (-1).toLong()) null else Date(tmpCreatedAt)
        val tmpUpdatedAt = `in`.readLong()
        this.updatedAt = if (tmpUpdatedAt == (-1).toLong()) null else Date(tmpUpdatedAt)
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
            if (true) {
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
            PlayMode.LOOP, PlayMode.LIST, PlayMode.SINGLE, PlayMode.default -> {
                var newIndex = playingIndex - 1
                if (newIndex < 0) {
                    newIndex = songs!!.size - 1
                }
                playingIndex = newIndex
            }
            PlayMode.SHUFFLE -> playingIndex = randomPlayIndex()
            null -> TODO()
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
            if (playMode == PlayMode.LIST && playingIndex + 1 >= songs!!.size) return false
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
            PlayMode.LOOP, PlayMode.LIST, PlayMode.SINGLE, PlayMode.default -> {
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
        const val COLUMN_FAVORITE = "songs"

        val CREATOR: Parcelable.Creator<PlayList> = object : Parcelable.Creator<PlayList> {
            override fun createFromParcel(source: Parcel): PlayList {
                return PlayList(source)
            }

            override fun newArray(size: Int): Array<PlayList?> {
                return arrayOfNulls(size)
            }
        }

        fun fromFolder(folder: Folder): PlayList {
            val playList = PlayList()
            playList.name = folder.name
            playList.setSongs(folder.songs)
            playList.numOfSongs = folder.numOfSongs
            return playList
        }
    }
}
