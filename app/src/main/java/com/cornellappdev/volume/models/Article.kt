package com.cornellappdev.volume.models

import android.content.Context
import android.graphics.BlurMaskFilter
import android.os.Build
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.cornellappdev.volume.R
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Parcelize
class Article(
    val id: String,
    val title: String,
    val articleURL: String,
    val imageURL: String,
    val publication: Publication? = null,
    val date: String,
    val shoutouts: Double,
    val nsfw: Boolean = false
) : Parcelable {

    companion object {
        const val INTENT_KEY = "article"

        fun applyNSFWFilter(article: Article, target: TextView) {
            if (article.nsfw) {
                target.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                val radius: Float = target.textSize / 3
                val filter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
                target.paint.maskFilter = filter
            }
        }

        fun sortByDate(articles: MutableList<Article>) {
            articles.sortWith(compareByDescending { article ->
                article.date
            })
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setCorrectDateText(article: Article, target: TextView, context: Context) {
            val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val datePublished = LocalDateTime.parse(article.date, format)
            val dur = Duration.between(datePublished, LocalDateTime.now())
            val hours = abs(dur.toHours()).toInt()
            val days = abs(dur.toDays()).toInt()

            val weeks = days / 7
            val months = days / 30
            val years = days / 365

            target.text = when {
                days < 1 -> {
                    context.resources.getQuantityString(R.plurals.x_h_ago, hours, hours)
                }
                days in 1..6 -> {
                    context.resources.getQuantityString(R.plurals.x_days_ago, days, days)
                }
                days in 7..29 -> {
                    context.resources.getQuantityString(R.plurals.x_weeks_ago, weeks, weeks)
                }
                days in 30..364 -> {
                    context.resources.getQuantityString(R.plurals.x_months_ago, months, months)
                }
                else -> {
                    context.resources.getQuantityString(R.plurals.x_years_ago, years, years)
                }
            }
        }
    }
}