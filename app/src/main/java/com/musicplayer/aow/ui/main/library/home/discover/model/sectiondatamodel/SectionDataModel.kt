package com.musicplayer.aow.ui.browse.model.sectiondatamodel

import com.musicplayer.aow.ui.browse.model.singleitemmodel.SingleItemModel


/**
 * Created by Arca on 11/28/2017.
 */
class SectionDataModel {

    var headerTitle: String? = null
    var link: String? = null
    var picture: String? = null
    var allItemsInSection: ArrayList<SingleItemModel>? = ArrayList()

    constructor()

    constructor(headerTitle: String, allItemsInSection: ArrayList<SingleItemModel>) {
        this.headerTitle = headerTitle
        this.allItemsInSection = allItemsInSection
    }

    override fun toString(): String {
        return "{\"name\":\"$headerTitle\", \"url\":\"$link\", \"image\":\"$picture\", \"member\":$allItemsInSection}"
    }
}