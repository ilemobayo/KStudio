package com.musicplayer.aow.delegates.data.model

import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.TypeConverters
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.NonNull
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.webkit.URLUtil
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.litesuits.orm.db.annotation.Unique
import com.musicplayer.aow.R
import com.musicplayer.aow.delegates.exo.DownloadUtil
import com.musicplayer.aow.delegates.player.PlayMode
import com.musicplayer.aow.delegates.player.SongResolver
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import java.util.*

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

    var tracks: ArrayList<Track>? = ArrayList()

    var playingIndex: Int = -1

    /**
     * Use a singleton play mode
     */
    @Ignore
    var playMode: PlayMode? = PlayMode.LIST

    // Utils

    var itemCount: Int = 0
        get() = if (tracks == null) 0 else tracks!!.size

    /**
     * The current track being played or is playing based on the [.playingIndex]
     */
    @Ignore
    var currentTrack: MutableLiveData<Track>? = null
        get() = if (playingIndex != NO_POSITION) {
           val data: MutableLiveData<Track> = MutableLiveData()
           data.value = tracks!![playingIndex]
           data
        } else null

    constructor() {
        // EMPTY
    }

    @Ignore
    constructor(mxp_id: String){
        this.mxp_id = mxp_id
    }

    @Ignore
    constructor(track: Track) {
        tracks!!.add(track)
        numOfSongs = 1
    }

    @Ignore
    constructor(trackList: ArrayList<Track>?) {
        tracks!!.addAll(trackList!!)
        numOfSongs = tracks!!.size
    }

    @Ignore
    constructor(trackList: List<Track>, name: String) {
        this.name = name
        tracks!!.addAll(trackList)
        numOfSongs = tracks!!.size
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
        dest.writeTypedList(this.tracks)
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
        this.tracks = `in`.createTypedArrayList(Track.CREATOR)
        this.playingIndex = `in`.readInt()
        val tmpPlayMode = `in`.readInt()
        this.playMode = if (tmpPlayMode == -1) null else PlayMode.values()[tmpPlayMode]
    }

    fun addSong(track: Track?) {
        if (track == null) return

        tracks!!.add(track)
        numOfSongs = tracks!!.size
    }

    fun addSong(track: Track?, index: Int) {
        if (track == null) return

        tracks!!.add(index, track)
        numOfSongs = tracks!!.size
    }

    fun addSong(tracks: List<Track>?, index: Int) {
        if (tracks == null || tracks.isEmpty()) return

        this.tracks!!.addAll(index, tracks)
        this.numOfSongs = this.tracks!!.size
    }

    fun removeTrack(track: Track?): Boolean {
        if (track == null) return false

        val index = tracks!!.indexOf(track)
        if (index != -1) {
            tracks!!.removeAt(index).apply {
                numOfSongs = size
            }
            return true
        } else {
            val iterator = tracks!!.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (track.path == item.path) {
                    iterator.remove()
                    numOfSongs = tracks!!.size
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
        if (tracks!!.isEmpty()) return false
        if (playingIndex == NO_POSITION) {
            playingIndex = 0
        }
        return true
    }

    fun hasLast(): Boolean {
        return tracks != null && tracks!!.size != 0
    }

    fun last(previousIndex: Int): MutableLiveData<Track> {
        playingIndex = previousIndex
        currentTrack?.value = tracks!![playingIndex]
        return currentTrack!!
    }

    /**
     * @return Whether has next track to play.
     *
     *
     * If this query satisfies these conditions
     * - comes from media player's complete listener
     * - current play mode is PlayMode.LIST (the only limited play mode)
     * - current track is already in the end of the list
     * then there shouldn't be a next track to play, for this condition, it returns false.
     *
     *
     * If this query is from user's action, such as from play controls, there should always
     * has a next track to play, for this condition, it returns true.
     */
    fun hasNext(): Boolean {
        if (tracks!!.isEmpty()) return false

        return true
    }

    /**
     * Move the playingIndex forward depends on the play mode
     *
     * @return The next track to play
     */
    fun next(nextIndex: Int): MutableLiveData<Track> {
        playingIndex = nextIndex
        currentTrack?.value = tracks!![playingIndex]
        return currentTrack!!
    }

    fun hasThisTrack(tPath: String? = ""): Boolean {
        var found = false
        tracks?.forEach {
            if (it.path == tPath){
                found = true
                return true
            }
        }
        return found
    }

    fun getConcatenatedSongLocal(context: Context, track: Track?): ExtractorMediaSource {
        val dataSpec = DataSpec(Uri.parse(track?.path))
        val fileDataSource = FileDataSource()
        try
        {
            fileDataSource.open(dataSpec)
        }
        catch (e:FileDataSource.FileDataSourceException) {
            e.printStackTrace()
        }
        val factory = DataSource.Factory { fileDataSource }
        return ExtractorMediaSource(fileDataSource.uri,
                factory, DefaultExtractorsFactory(), null, null)
    }

    fun getConcatenatedSong(context: Context, track: Track?): MediaSource{
        val dataSourceFactory = DefaultDataSourceFactory(
                context, Util.getUserAgent(context, context.getString(R.string.application_name)))
        val cacheDataSourceFactory = CacheDataSourceFactory(
                DownloadUtil.getCache(context),
                dataSourceFactory,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        return ExtractorMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(Uri.parse(track?.path!!))
    }

    fun getConcatenatedPlaylist(context: Context): ConcatenatingMediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(
                context, Util.getUserAgent(context, context.getString(R.string.application_name)))
        val cacheDataSourceFactory = CacheDataSourceFactory(
                DownloadUtil.getCache(context),
                dataSourceFactory,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val concatenatingMediaSource = ConcatenatingMediaSource()
        tracks?.forEach {
            val mediaSource = ExtractorMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(Uri.parse(it.path))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun getConcatenatedPlaylistLocal(): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        tracks?.forEach {
            val dataSpec = DataSpec(Uri.parse(it.path))
            val fileDataSource = FileDataSource()
            try
            {
                fileDataSource.open(dataSpec)
            }
            catch (e:FileDataSource.FileDataSourceException) {
                e.printStackTrace()
            }
            val factory = DataSource.Factory { fileDataSource }
            val mediaSource = ExtractorMediaSource(fileDataSource.uri,
                    factory, DefaultExtractorsFactory(), null, null)
            concatenatingMediaSource.addMediaSource(mediaSource)
        }

        return concatenatingMediaSource
    }

    fun getBitmap(context: Context, track: Track?, completion: (Bitmap?, Error?) -> Unit){
        if (track?.albumArt != null && track.albumArt != "null") {
            if (URLUtil.isHttpUrl(track.albumArt) || URLUtil.isHttpsUrl(track.albumArt)) {
                doAsync {
                    val bitmap = Glide.with(context)
                            .asBitmap()
                            .load(track.albumArt)
                            .submit(250, 250)
                            .get()
                    onComplete {
                        completion(bitmap, null)
                    }
                }
            } else {
                try{
                    val metadataRetriever = MediaMetadataRetriever()
                    metadataRetriever.setDataSource(track.path)
                    val data = metadataRetriever.embeddedPicture
                    // convert the byte array to a bitmap
                    if(data != null)
                    {
                        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                        if (bitmap != null){
                            completion(bitmap, null)
                        }else{
                            completion(bitmap, null)
                        }
                    }else{
                        completion(null, null)
                    }
                    metadataRetriever.release()
                }catch (e:Exception){
                    completion(null, null)
                }
            }
        } else {
            completion(null, null)
        }
    }

    fun getMediaDescription(index: Int? = 0, track: Track): MediaDescriptionCompat {
        val extras = Bundle()
        return MediaDescriptionCompat.Builder()
                .setMediaId(index.toString())
                .setTitle(track.title)
                .setDescription(track.artist)
                //.setIconUri(Uri.parse(track.albumArt))
                .setExtras(extras)
                .build()
    }

    companion object {

        // Play List: Favorite
        val NO_POSITION = -1

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
            playList.tracks = folder.tracks as ArrayList<Track>?
            playList.numOfSongs = folder.numOfSongs
            return playList
        }
    }
}
