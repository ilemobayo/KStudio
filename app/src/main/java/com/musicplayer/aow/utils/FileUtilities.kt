package com.musicplayer.aow.utils

import android.media.MediaMetadataRetriever
import android.text.TextUtils
import com.musicplayer.aow.delegates.data.model.Folder
import com.musicplayer.aow.delegates.data.model.Song
import java.io.File
import java.text.DecimalFormat
import java.util.*

object FileUtilities {

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

    fun scanDir(dir: File?): List<Song> {
        val songs = ArrayList<Song>()
        if (dir != null && dir.isDirectory) {
            val files = dir.listFiles { item ->
                item.isFile && isMusic(item)
                //                    return true;
            }
            for (file in files) {
                if (file.isDirectory) {
                    //                    songs.addAll(scanDir(file));
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

    fun musicFiles(dir: File): List<Song> {
        val songs = scanDir(dir)
        if (songs.size > 1) {
            Collections.sort(songs) { left, right -> left.title!!.compareTo(right.title!!) }
        }
        return songs
    }

    fun fileToMusic(file: File): Song? {
        if (file.length() == 0L) return null

        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(file.absolutePath)

        val duration: Int

        val keyDuration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        // ensure the duration is a digit, otherwise return null song
        if (keyDuration == null || !keyDuration.matches("\\d+".toRegex())) return null
        duration = Integer.parseInt(keyDuration)

        val title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val displayName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)

        var song = Song()
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
//        val songs = musicFiles(dir)
//        folder.songs = songs
//        folder.numOfSongs = songs.size
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
