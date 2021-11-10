package com.cornellappdev.volume.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.PublicationProfileActivity
import com.cornellappdev.volume.R
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
    private val publicationList: MutableList<Publication>,
    private val prefUtils: PrefUtils,
    private val mAdapterOnClickHandler: AdapterOnClickHandler?
) :
    RecyclerView.Adapter<MorePublicationsAdapter.MorePublicationVH>() {

    private val UUID = prefUtils.getString(PrefUtils.UUID, null)
    private val graphQlUtil = GraphQlUtil()
    private val disposables = CompositeDisposable()

    interface AdapterOnClickHandler {
        fun onFollowClick(wasFollowed: Boolean)
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

        holder.binding.btnFollow.setOnClickListener {
            if (holder.binding.btnFollow.drawable.constantState == ContextCompat.getDrawable(
                    it.context,
                    R.drawable.ic_followplussvg
                )!!.constantState
            ) {
                holder.binding.btnFollow.setImageResource(R.drawable.ic_followchecksvg)
                val tempSet =
                    prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf()).toMutableSet()
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
                    // This call is a mutation, doesn't need to retrieve anything from response.
                    disposables.add(followObservable.subscribe { _ ->
                    })
                }
            } else {
                holder.binding.btnFollow.setImageResource(R.drawable.ic_followplussvg)
                val tempSet =
                    prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf()).toMutableSet()
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
                    // This call is a mutation, doesn't need to retrieve anything from response.
                    disposables.add(unfollowObservable.subscribe { _ ->
                    })
                }
            }
        }

        holder.binding.clPublicationLayout.setOnClickListener { view ->
            val intent = Intent(view.context, PublicationProfileActivity::class.java)
            intent.putExtra(Publication.INTENT_KEY, currentItem)
            view.context.startActivity(intent)
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