package com.musicplayer.aow.utils

import android.media.MediaMetadataRetriever
import android.text.TextUtils
import com.musicplayer.aow.delegates.data.model.Folder
import com.musicplayer.aow.delegates.data.model.Track
import java.io.File
import java.text.DecimalFormat
import java.util.*

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com.musicpalyer.com.musicplayer.aow
 * Date: 9/3/16
 * Time: 11:11 PM
 * Desc: FileUtils
 */
object FileUtils {

    private val UNKNOWN = "unknown"

    /**
     * http://stackoverflow.com/a/5599842/2290191
     *
     * @param size Original file size in byte
     * @return Readable file size in formats
     */
    fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("b", "kb", "M", "G", "T")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.##").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun isMusic(file: File): Boolean {
        val REGEX = "(.*/)*.+\\.(mp3|m4a|ogg|wav|aac)$"
        return file.name.matches(REGEX.toRegex())
    }

    fun isLyric(file: File): Boolean {
        return file.name.toLowerCase().endsWith(".lrc")
    }

    fun scanDir(dir: File?): List<Track> {
        val songs = ArrayList<Track>()
        if (dir != null && dir.isDirectory) {
            val files = dir.listFiles { item ->
                item.isFile && isMusic(item)
                //                    return true;
            }
            for (file in files) {
                if (file.isDirectory) {
                    //                    tracks.addAll(scanDir(file));
                } else if (file.isFile && isMusic(file)) {
                    val song = fileToMusic(file)
                    if (song != null) {
                        songs.add(song)
                    }
                }
            }
        }
        return songs
    }

    fun musicFiles(dir: File): List<Track> {
        val songs = scanDir(dir)
        if (songs.size > 1) {
            Collections.sort(songs) { left, right -> left.title!!.compareTo(right.title!!) }
        }
        return songs
    }

    fun fileToMusic(file: File): Track? {
        if (file.length() == 0L) return null

        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(file.absolutePath)

        val duration: Int

        val keyDuration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        // ensure the duration is a digit, otherwise return null track
        if (keyDuration == null || !keyDuration.matches("\\d+".toRegex())) return null
        duration = Integer.parseInt(keyDuration)

        val title = extractMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_TITLE, file.name)
        val displayName = extractMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_TITLE, file.name)
        val artist = extractMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_ARTIST, UNKNOWN)
        val album = extractMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_ALBUM, UNKNOWN)

        val song = Track()
        song.title = title
        song.displayName = displayName
        song.artist = artist
        song.path = file.absolutePath
        song.album = album
        song.duration = duration
        song.size = file.length().toInt()
        return song
    }

    fun folderFromDir(dir: File): Folder {
        val folder = Folder(dir.name, dir.absolutePath)
//        val tracks = musicFiles(dir)
//        folder.tracks = tracks
//        folder.numOfSongs = tracks.size
        return folder
    }

    private fun extractMetadata(retriever: MediaMetadataRetriever, key: Int, defaultValue: String): String {
        var value = retriever.extractMetadata(key)
        if (TextUtils.isEmpty(value)) {
            value = defaultValue
        }
        return value
    }
}
