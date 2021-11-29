package com.cornellappdev.volume.util

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication

/**
 * DiffUtil makes the replacement of articles in RecyclerViews faster
 * by comparing the strict difference between oldArticles and newArticles
 * and calling notifyDataSetChanged for the items accordingly.
 */
class DiffUtilCallbackArticle(private val oldArticles: List<Article>, private val newArticles: List<Article>) :
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

/**
 * DiffUtil makes the replacement of publications in RecyclerViews faster
 * by comparing the strict difference between oldPublications and newPublications
 * and calling notifyDataSetChanged for the items accordingly.
 */
class DiffUtilCallbackPublication(
    private val oldPublications: List<Publication>,
    private val newPublications: List<Publication>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldPublications.size
    }

    override fun getNewListSize(): Int {
        return newPublications.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldPublications[oldItemPosition].id === newPublications[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldPublications[oldItemPosition] == newPublications[newItemPosition]
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}