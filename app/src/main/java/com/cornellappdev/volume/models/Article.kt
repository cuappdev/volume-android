package com.cornellappdev.volume.models

import android.graphics.BlurMaskFilter
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import kotlinx.android.parcel.Parcelize

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
    }
}