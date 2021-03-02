package com.cornellappdev.volume.models

import android.graphics.BlurMaskFilter
import android.os.Build
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import kotlinx.android.parcel.Parcelize
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Parcelize
class Article(
    val id: String? = "",
    val title: String? = "",
    val articleURL: String? = "",
    val imageURL: String? = "",
    val publication: Publication? = null,
    val date: String? = "",
    val shoutouts: Double? = 0.0,
    val nsfw: Boolean? = false
): Parcelable {

    companion object {
        fun applyNSFWFilter(article: Article, target: TextView) {
            if (article.nsfw == true) {
                target.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                val radius: Float = target.textSize / 3
                val filter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
                target.paint.maskFilter = filter
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setCorrectDateText(article: Article, target: TextView) {
            val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val datePublished = LocalDateTime.parse(article.date, format)
            val dur = Duration.between(datePublished, LocalDateTime.now())
            if(dur.toDays() < 1) {
                val hours = dur.toHours()
                target.text = "${abs(hours)} h ago"
            }
            if(dur.toDays() in 1..6) {
                target.text = if (dur.toDays() <= 1) {
                    "${dur.toDays()} day ago"
                } else {
                    "${dur.toDays()} days ago"
                }
            }
            if(dur.toDays() in 7..29) {
                val weeks = dur.toDays().toInt()/7
                target.text = if(weeks <= 1) {
                    "$weeks week ago"
                } else {
                    "$weeks weeks ago"
                }
            }
            if(dur.toDays() in 30..364) {
                val months = dur.toDays()/30
                target.text = if (months <= 1) {
                    "$months month ago"
                } else {
                    "$months months ago"
                }
            }
            if (dur.toDays() >= 365) {
                val years = dur.toDays()/365
                target.text = if(years <= 1) {
                    "$years year ago"
                } else {
                    "$years years ago"
                }
            }
        }
    }
}