package com.musicplayer.aow.delegates.data.model

import android.os.Parcel
import android.os.Parcelable
import com.litesuits.orm.db.annotation.*
import com.litesuits.orm.db.enums.AssignType
import com.litesuits.orm.db.enums.Relation
import java.util.*

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com.musicpalyer.com.musicplayer.aow
 * Date: 9/3/16
 * Time: 7:19 PM
 * Desc: Folder
 */
@Table("folder")
class Folder : Parcelable {

    @PrimaryKey(AssignType.AUTO_INCREMENT)
    var id: Int = 0

    @Column(COLUMN_NAME)
    var name: String? = null

    @Unique
    var path: String? = null

    var numOfSongs: Int = 0

//    @MapCollection(ArrayList<*>::class)
    @MapCollection(Collection::class)
    @Mapping(Relation.OneToMany)
    var tracks: MutableList<Track>? = ArrayList()

    var createdAt: Date? = null

    constructor() {
        // Empty
    }

    constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    constructor(name: String, path: String) {
        this.name = name
        this.path = path
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.id)
        dest.writeString(this.name)
        dest.writeString(this.path)
        dest.writeInt(this.numOfSongs)
        dest.writeTypedList(this.tracks)
        dest.writeLong(if (this.createdAt != null) this.createdAt!!.time else -1)
    }

    private fun readFromParcel(`in`: Parcel) {
        this.id = `in`.readInt()
        this.name = `in`.readString()
        this.path = `in`.readString()
        this.numOfSongs = `in`.readInt()
        this.tracks = `in`.createTypedArrayList(Track.CREATOR)
        val tmpCreatedAt = `in`.readLong()
        this.createdAt = if (tmpCreatedAt == (-1).toLong()) null else Date(tmpCreatedAt)
    }

    companion object {

        const val COLUMN_NAME = "name"

        val CREATOR: Parcelable.Creator<Folder> = object : Parcelable.Creator<Folder> {
            override fun createFromParcel(source: Parcel): Folder {
                return Folder(source)
            }

            override fun newArray(size: Int): Array<Folder?> {
                return arrayOfNulls(size)
            }
        }
    }
}
