package com.cornellappdev.volume.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.PublicationProfileActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.analytics.EventType
import com.cornellappdev.volume.analytics.NavigationSource
import com.cornellappdev.volume.analytics.NavigationSource.Companion.putParcelableExtra
import com.cornellappdev.volume.analytics.VolumeEvent
import com.cornellappdev.volume.databinding.ItemMorePublicationBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MorePublicationsAdapter(
    var publicationList: MutableList<Publication>,
    private val prefUtils: PrefUtils,
    private val mAdapterOnClickHandler: AdapterOnClickHandler?,
    private val isOnboarding: Boolean = false,
    private val mAdapterOnClicker:AdapterOnClicker?) :
        RecyclerView.Adapter<MorePublicationsAdapter.MorePublicationVH>() {

    interface AdapterOnClickHandler {
        fun onFollowClick(wasFollowed: Boolean)
    }

    interface AdapterOnClicker{
        fun onMorePublicationClicked(publication: Publication, isOnboarding: Boolean)
    }

    class MorePublicationVH(
        val binding: ItemMorePublicationBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MorePublicationVH {
        val binding = ItemMorePublicationBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MorePublicationVH(binding)
    }

    override fun getItemCount(): Int {
        return publicationList.size
    }

    override fun onBindViewHolder(holder: MorePublicationVH, position: Int) {
        val currentItem = publicationList[position]
        if (currentItem.profileImageURL.isNotBlank()) {
            Picasso.get().load(currentItem.profileImageURL).into(holder.binding.ivLogo)
        }
        currentItem.mostRecentArticle?.let {
            Article.applyNSFWFilter(it, holder.binding.tvRecentArticleTitle)
        }
        holder.binding.tvName.text = currentItem.name
        holder.binding.tvDescription.text = currentItem.bio
        holder.binding.tvRecentArticleTitle.text = currentItem.mostRecentArticle?.title

        val currentFollowingSet =
            prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf())

        if (currentFollowingSet.contains(currentItem.id)) {
            holder.binding.btnFollow.setImageResource(R.drawable.ic_followchecksvg)
        } else {
            holder.binding.btnFollow.setImageResource(R.drawable.ic_followplussvg)
        }

        val UUID = prefUtils.getString(PrefUtils.UUID, null)
        val disposables = CompositeDisposable()
        val graphQlUtil = GraphQlUtil()

        holder.binding.btnFollow.setOnClickListener {
            if (holder.binding.btnFollow.drawable.constantState == ContextCompat.getDrawable(
                    it.context,
                    R.drawable.ic_followplussvg
                )!!.constantState
            ) {
                holder.binding.btnFollow.setImageResource(R.drawable.ic_followchecksvg)
                val tempSet =
                    prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf()).toMutableSet()
                VolumeEvent.logEvent(
                    EventType.PUBLICATION, VolumeEvent.FOLLOW_PUBLICATION, if (isOnboarding) {
                        NavigationSource.ONBOARDING
                    } else {
                        NavigationSource.MORE_PUBLICATIONS
                    },
                    currentItem.id
                )
                tempSet.add(currentItem.id)
                prefUtils.save(PrefUtils.FOLLOWING_KEY, tempSet)
                mAdapterOnClickHandler?.onFollowClick(wasFollowed = true)

                // Follow by user.
                val followObservable = UUID?.let { uuid ->
                    graphQlUtil
                        .followPublication(currentItem.id, uuid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                }

                if (followObservable != null) {
                    disposables.add(followObservable.subscribe { _ ->
                    })
                }
            } else {
                holder.binding.btnFollow.setImageResource(R.drawable.ic_followplussvg)
                val tempSet =
                    prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf()).toMutableSet()
                VolumeEvent.logEvent(
                    EventType.PUBLICATION,
                    VolumeEvent.UNFOLLOW_PUBLICATION,
                    id = currentItem.id
                )
                tempSet.remove(currentItem.id)
                prefUtils.save(PrefUtils.FOLLOWING_KEY, tempSet)
                mAdapterOnClickHandler?.onFollowClick(wasFollowed = false)

                // Unfollow by user.
                val unfollowObservable = UUID?.let { uuid ->
                    graphQlUtil
                        .unfollowPublication(currentItem.id, uuid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                }

                if (unfollowObservable != null) {
                    disposables.add(unfollowObservable.subscribe { _ ->
                    })
                }
            }
        }

        holder.binding.clPublicationLayout.setOnClickListener { view ->
            mAdapterOnClicker?.onMorePublicationClicked(currentItem, isOnboarding)
        }
    }

    fun clear() {
        publicationList.clear()
        notifyDataSetChanged()
    }

    fun addAll(list: List<Publication>) {
        publicationList.addAll(list)
        notifyDataSetChanged()
    }
}