package com.cornellappdev.volume

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.cornellappdev.volume.models.Article

/**
 * DiffUtil makes the replacement of articles in RecyclerViews faster
 * by comparing the strict difference between oldArticles and newArticles
 * and calling notifyDataSetChanged for the items accordingly.
 */
class MyDiffCallback(private val oldArticles: List<Article>, private val newArticles: List<Article>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldArticles.size
    }

    override fun getNewListSize(): Int {
        return newArticles.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldArticles[oldItemPosition].id === newArticles[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldArticles[oldItemPosition] == newArticles[newItemPosition]
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}