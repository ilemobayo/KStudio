package com.musicplayer.aow.delegates.data.model

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataConverter {
    @TypeConverter
    fun fromSongs(value: ArrayList<Song>): String {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Song>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toSongs(value: String): ArrayList<Song> {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Song>>() {}.type
        return gson.fromJson(value, type)
    }


    @TypeConverter
    fun fromCurrentSong(value: Song): String {
        val gson = Gson()
        val type = object : TypeToken<Song>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCurrentSong(value: String): Song {
        val gson = Gson()
        val type = object : TypeToken<Song>() {}.type
        return gson.fromJson(value, type)
    }
}