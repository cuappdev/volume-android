package com.cornellappdev.volume

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.cornellappdev.volume.models.Publication

/**
 * DiffUtil makes the replacement of publications in RecyclerViews faster
 * by comparing the strict difference between oldPublications and newPublications
 * and calling notifyDataSetChanged for the items accordingly.
 */
class DiffUtilCallback(
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