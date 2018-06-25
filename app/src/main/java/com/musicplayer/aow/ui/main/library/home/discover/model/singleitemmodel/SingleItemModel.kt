package com.musicplayer.aow.ui.browse.model.singleitemmodel

/**
 * Created by Arca on 11/28/2017.
 */
class SingleItemModel {

    var name: String? = null
    var artist: String? = null
    var url: String? = null
    var description: String? = null
    var link: String? = null
    var listType: String? = null
    var parent_playlist: String? = null

    constructor()

    override fun toString(): String {
        return "{\"name\":\"$name\", \"artist\":\"$artist\"," +
                " \"url\":\"$url\", \"description\":\"$description\", \"link\":\"$link\", \"listType\":\"$listType\"}"
    }

}