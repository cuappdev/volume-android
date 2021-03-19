package com.cornellappdev.volume.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.cornellappdev.volume.R
import com.cornellappdev.volume.databinding.LayoutWebviewTopBinding
import com.cornellappdev.volume.models.Article


class WebviewTop @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutWebviewTopBinding =
            LayoutWebviewTopBinding.bind(
                    LayoutInflater
                            .from(context)
                            .inflate(
                                    R.layout.layout_webview_top,
                                    this,
                                    true))


//    private val binding: LayoutWebviewTopBinding =
//            LayoutWebviewTopBinding.inflate(
//                    LayoutInflater.from(context),
//                    this,
//                    true)

    fun setName(article: Article) {
        binding.tvArticleTitle.text = article.title
    }

    fun minimize(isMinimized: Boolean) {
        if (isMinimized) {
            val param = binding.tvArticleTitle.layoutParams as MarginLayoutParams
            param.setMargins(0, 20, 0, 10)
            binding.tvArticleTitle.layoutParams = param
            binding.tvReading.visibility = View.GONE
            binding.ivCompass.visibility = View.INVISIBLE
        } else {
            val param = binding.tvArticleTitle.layoutParams as MarginLayoutParams
            param.setMargins(0, 20, 0, 0)
            binding.tvArticleTitle.layoutParams = param
            binding.tvReading.visibility = View.VISIBLE
            binding.ivCompass.visibility = View.VISIBLE
        }
    }
}