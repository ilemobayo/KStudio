package com.musicplayer.aow.delegates.data.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.musicplayer.aow.delegates.data.db.model.DiscoveryModel

@Dao
interface DiscoveryDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (discovery: DiscoveryModel)

    @Query("SELECT * FROM foryou WHERE mxp_id = :id")
    fun fetchOneId (id: Int): LiveData<DiscoveryModel>

    @Query("SELECT * FROM foryou WHERE name = :name")
    fun fetchDiscoveryLiveData (name: String = "discovery"): LiveData<DiscoveryModel>

    @Query("SELECT * FROM foryou WHERE name = :name")
    fun fetchDiscovery (name: String = "discovery"): DiscoveryModel

    @Query("SELECT * FROM foryou WHERE name != :name")
    fun fetchAll (name: String = "discovery"): List<DiscoveryModel>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update (discovery: DiscoveryModel)

    @Delete
    fun delete (discovery: DiscoveryModel)
}