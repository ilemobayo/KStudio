package com.musicplayer.aow.delegates.softcode.adapters.placeholder

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PlaceholderSearch(
        @Expose @SerializedName("id") var _id: String = "",
        @Expose @SerializedName("owner") var owner: String = "",
        @Expose @SerializedName("downloadable") var downloadable: Boolean = false,
        @Expose @SerializedName("date_pdated") var dateUpdated: String = "",
        @Expose @SerializedName("date_created") var dateCreated: String = "",
        @Expose @SerializedName("description") var description: String = "",
        @Expose @SerializedName("no_streams") var noStreams: Int = 0,
        @Expose @SerializedName("type") var type: String? = "",
        @Expose @SerializedName("picture") var picture: String = "",
        @Expose @SerializedName("no_downloads") var noDownloads: Int = 0,
        @Expose @SerializedName("public") var public: Boolean = false,
        @Expose @SerializedName("no_purchase") var noPurchase: Int = 0,
        @Expose @SerializedName("price") var price: Int = 0,
        @Expose @SerializedName("name") var name: String = "",
        @Expose @SerializedName("paid") var paid: Boolean = false,
        @Expose @SerializedName("add_ons") var addOns: AddOns? = null,
        @Expose @SerializedName("location") var location: String = "",
        @Expose @SerializedName("free") var free: Boolean = false,
        @Expose(serialize = false, deserialize = false)var member: String = "")