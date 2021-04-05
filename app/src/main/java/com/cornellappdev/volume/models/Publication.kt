package com.cornellappdev.volume.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Publication(
        val id: String,
        val backgroundImageURL: String,
        val bio: String,
        val name: String,
        val profileImageURL: String,
        val rssName: String,
        val rssURL: String,
        val slug: String,
        val shoutouts: Double,
        val websiteURL: String,
        val mostRecentArticle: Article? = null,
        val socials: List<Social>?
) : Parcelable {
    companion object {
        const val INTENT_KEY = "publication"
    }
}