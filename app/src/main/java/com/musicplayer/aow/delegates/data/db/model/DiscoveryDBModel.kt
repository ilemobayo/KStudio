package com.musicplayer.aow.delegates.data.db.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.TypeConverters
import android.content.Context
import android.support.annotation.NonNull
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.litesuits.orm.db.annotation.Unique
import com.musicplayer.aow.delegates.data.db.AppExecutors
import com.musicplayer.aow.delegates.data.db.database.DiscoveryDatabase
import com.musicplayer.aow.delegates.data.model.DataConverter
import com.musicplayer.aow.delegates.softcode.adapters.placeholder.AddOns
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException


class DiscoveryDBModel(var context: Context) {
    private var discoveryDatabase: DiscoveryDatabase? = DiscoveryDatabase.getsInstance(context)

    fun initPlaylist(){
        AppExecutors.instance?.diskIO()?.execute {
            val discovery = discoveryDatabase?.discoveryDAO()?.fetchDiscovery()
            if (discovery == null){
                try {
                    val file = File(context.externalCacheDir?.absolutePath + "/onlinedata/discovery.json")
                            .inputStream()
                            .readBytes()
                            .toString(Charsets.UTF_8)
                    val gson = Gson()
                    val json = gson.fromJson(file, DiscoveryModel::class.java)
                    if (json != null) {
                        discoveryDatabase?.discoveryDAO()?.insert(json)
                    }else{
                        Log.e(this.javaClass.name, "empty json")
                    }
                } catch (e: FileNotFoundException) {

                } catch (ioe: IOException) {

                }
            } else {
                try {
                    val file = File(context.externalCacheDir?.absolutePath + "/onlinedata/discovery.json")
                            .inputStream()
                            .readBytes()
                            .toString(Charsets.UTF_8)
                    Log.e(this.javaClass.name, "json: ${file}")
                    val gson = Gson()
                    val json = gson.fromJson(file, DiscoveryModel::class.java)
                    if (json != null) {
                        discoveryDatabase?.discoveryDAO()?.update(json)
                    }else{
                        Log.e(this.javaClass.name, "empty json")
                    }
                } catch (e: FileNotFoundException) {

                } catch (ioe: IOException) {

                }
            }
        }
    }
}

@Entity(tableName = "foryou")
@TypeConverters(DataConverter::class)
class DiscoveryModel {

    @Unique
    @NonNull
    @android.arch.persistence.room.PrimaryKey(autoGenerate = false)
    var mxp_id: String? = "discovery"
    var name: String = "discovery"
    var result: List<Result>? = null
}

class Result {
    var id: String? = ""
    var addOns: AddOns? = null
    var date: String? = ""
    var dateCreated: String? = ""
    var dateUpdated: String? = ""
    var description: String? = ""
    var downloadable: Boolean? = false
    var free: Boolean? = false
    var location: String? = ""
    var member: List<Member>? = null
    var name: String? = ""
    var noDownloads: Int? = 0
    var noPurchase: Int? = 0
    var noStreams: Int? = 0
    var owner: String? = ""
    var paid: Boolean? = false
    var picture: String? = ""
    var price: Int? = 0
    var public: Boolean? = true
    var type: String? = ""
}

class Member {
    var id: String? = ""
    var addOns: AddOns_? = null
    var dateCreated: String? = ""
    var dateUpdated: String? = ""
    var description: String? = ""
    var downloadable: Boolean? = false
    var free: Boolean? = false
    var location: String? = ""
    var member: List<String>? = null
    var name: String? = ""
    var noDownloads: Int? = 0
    var noPurchase: Int? = 0
    var noStreams: Int? = 0
    var owner: String? = ""
    var paid: Boolean? = false
    var picture: String? = ""
    var price: Int? = 0
    var public: Boolean? = true
    var type: String? = ""
    var date: String? = ""
}

class AddOns {

}

class AddOns_ {

}