package com.musicplayer.aow.delegates.data.model

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.musicplayer.aow.delegates.data.db.model.Member
import com.musicplayer.aow.delegates.data.db.model.Result

class DataConverter {
    @TypeConverter
    fun fromSongs(value: ArrayList<Track>): String {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Track>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toSongs(value: String): ArrayList<Track> {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Track>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromResult(value: List<Result>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Result>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toResult(value: String): List<Result> {
        val gson = Gson()
        val type = object : TypeToken<List<Result>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromMember(value: List<Member>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Member>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toMember(value: String): List<Member> {
        val gson = Gson()
        val type = object : TypeToken<List<Member>>() {}.type
        return gson.fromJson(value, type)
    }


    @TypeConverter
    fun fromCurrentSong(value: Track): String {
        val gson = Gson()
        val type = object : TypeToken<Track>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCurrentSong(value: String): Track {
        val gson = Gson()
        val type = object : TypeToken<Track>() {}.type
        return gson.fromJson(value, type)
    }
}