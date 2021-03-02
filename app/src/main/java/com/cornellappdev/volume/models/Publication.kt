package com.cornellappdev.volume.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Publication (
//        val bio: String,
//                    val id: String,
//                    val imageURL: String,
//                    val name: String,
//                    val rssName: String,
//                    val rssURL: String,
//                    val shoutouts: Double,
//                    val websiteURL: String
    val id: String,
    val backgroundImageURL: String? = "",
    val bio: String? = "",
    val name: String,
    val profileImageURL: String? = "",
    val rssName: String? = "",
    val rssURL: String? = "",
    val slug: String? = "",
    val shoutouts: Double? = 0.0,
    val websiteURL: String? = "",
    val mostRecentArticle: Article? = null
) : Parcelable {


    fun Publication(id: String, name: String){


    }
}