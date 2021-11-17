package com.cornellappdev.volume.models

import android.os.Parcelable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.parcelize.Parcelize

@Parcelize
class WeeklyDebrief(
    val createdAt: Long,
    val expiration: Long,
    val shoutouts: Int,
    val numArticles: Int,
    val readArticles: List<Article>,
    val randomArticles: List<Article>
) : Parcelable {
    companion object {
        const val INTENT_KEY = "weekly_debrief"
    }
}