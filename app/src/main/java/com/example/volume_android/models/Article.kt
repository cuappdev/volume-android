package com.example.volume_android.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Article(
//        val articleURL: String,
//        val date: String,
//        val id: String,
//        val imageURL: String,
//        val publicationID: String,
//        val shoutouts: Double,
//        val title: String,
    val id: String? = "",
    val title: String? = "",
    val articleURL: String? = "",
    val imageURL: String? = "",
    val publication: Publication? = null,
    val date: String? = "",
    val shoutouts: Double? = 0.0,
): Parcelable {
}