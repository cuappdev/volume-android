package com.cornellappdev.volume.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class WeeklyDebrief(
    val createdAt: Long,
    val expiration: Long,
    val numShoutouts: Double,
    val numBookmarkedArticles: Double,
    val numReadArticles: Double,
    val readArticles: List<Article>,
    val randomArticles: List<Article>
) : Parcelable {
    companion object {
        const val INTENT_KEY = "weekly_debrief"
        const val TAG = "weekly_debrief"
    }
}