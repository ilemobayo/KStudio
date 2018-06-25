package com.musicplayer.aow.delegates.softcode.adapters.placeholder

import com.google.gson.annotations.SerializedName

data class PlaceholderDataNoMember(
        @SerializedName("id") var _id: String = "",
        @SerializedName("owner") var owner: String = "",
        @SerializedName("downloadable") var downloadable: Boolean = false,
        @SerializedName("date_pdated") var dateUpdated: String = "",
        @SerializedName("date_created") var dateCreated: String = "",
        @SerializedName("description") var description: String = "",
        @SerializedName("no_streams") var noStreams: Int = 0,
        @SerializedName("type") var type: String = "",
        @SerializedName("picture") var picture: String = "",
        @SerializedName("no_downloads") var noDownloads: Int = 0,
        @SerializedName("public") var public: Boolean = false,
        @SerializedName("no_urchase") var noPurchase: Int = 0,
        @SerializedName("price") var price: Int = 0,
        @SerializedName("name") var name: String = "",
        @SerializedName("paid") var paid: Boolean = false,
        @SerializedName("add_ons") var addOns: AddOns? = null,
        @SerializedName("location") var location: String = "",
        @SerializedName("free") var free: Boolean = false,
        @SerializedName("member") var member: ArrayList<String> = ArrayList())