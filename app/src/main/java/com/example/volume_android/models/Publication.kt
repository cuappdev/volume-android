package com.example.volume_android.models

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
    val backgroundImageURL: String?,
    val bio: String?,
    val name: String?,
    val profileImageURL: String? = null,
    val rssName: String?,
    val rssURL: String?,
    val slug: String?,
    val shoutouts: Float? = null,
    val websiteURL: String? = null,
    val mostRecentArticle: Article? = null
) {}