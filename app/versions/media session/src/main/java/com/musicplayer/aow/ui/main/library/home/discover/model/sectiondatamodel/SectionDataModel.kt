package com.musicplayer.aow.ui.browse.model.sectiondatamodel

import com.musicplayer.aow.ui.browse.model.singleitemmodel.SingleItemModel
import java.util.*


/**
 * Created by Arca on 11/28/2017.
 */
class SectionDataModel {

    var headerTitle: String? = null
    var allItemsInSection: ArrayList<SingleItemModel>? = null

    constructor()

    constructor(headerTitle: String, allItemsInSection: ArrayList<SingleItemModel>) {
        this.headerTitle = headerTitle
        this.allItemsInSection = allItemsInSection
    }
}