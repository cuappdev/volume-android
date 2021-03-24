package com.cornellappdev.volume.fragments

import PrefUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.R
import com.cornellappdev.volume.adapters.FollowingHorizontalAdapter
import com.cornellappdev.volume.adapters.MorePublicationsAdapter
import com.cornellappdev.volume.databinding.FragmentPublicationsBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.util.GraphQlUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class PublicationsFragment : Fragment() {

    private lateinit var followpublicationRV: RecyclerView
    private lateinit var morepublicationRV: RecyclerView
    private val graphQlUtil = GraphQlUtil()
    private val disposables = CompositeDisposable()
    private val prefUtils = PrefUtils()
    private var _binding: FragmentPublicationsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentPublicationsBinding.inflate(inflater, container, false)
        getFollowingPublications(binding, isRefreshing = false)
        getMorePublications(binding)
        val volumeOrange: Int? = context?.let { ContextCompat.getColor(it, R.color.volumeOrange) }
        if (volumeOrange != null) {
            binding.srlQuery.setColorSchemeColors(volumeOrange, volumeOrange, volumeOrange)
        }
        binding.srlQuery.setOnRefreshListener {
            getFollowingPublications(binding,
                    isRefreshing = this::followpublicationRV.isInitialized)
            binding.srlQuery.isRefreshing = false
        }
        return binding.root
    }

    private fun getMorePublications(binding: FragmentPublicationsBinding) {
        val moreObs = graphQlUtil
                .getAllPublications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        disposables.add(moreObs.subscribe { response ->
            val morePublications = mutableListOf<Publication>()
            val publications = response.data?.getAllPublications
            if (publications != null) {
                publications.mapTo(morePublications, { publication ->
                    Publication(
                            publication.id,
                            publication.backgroundImageURL,
                            publication.bio,
                            publication.name,
                            publication.profileImageURL,
                            publication.rssName,
                            publication.rssURL,
                            publication.slug,
                            publication.shoutouts,
                            publication.websiteURL,
                            Article(
                                    publication.mostRecentArticle?.id,
                                    publication.mostRecentArticle?.title,
                                    publication.mostRecentArticle?.articleURL,
                                    publication.mostRecentArticle?.imageURL,
                                    nsfw = publication.mostRecentArticle?.nsfw))
                })
                morepublicationRV = binding.rvMorePublications
                morepublicationRV.adapter =
                        MorePublicationsAdapter(morePublications, prefUtils)
                morepublicationRV.layoutManager = LinearLayoutManager(context)
                morepublicationRV.setHasFixedSize(true)
            }
        })
    }

    private fun getFollowingPublications(binding: FragmentPublicationsBinding, isRefreshing: Boolean) {
        val followingPublicationsIDs =
                prefUtils.getStringSet("following", mutableSetOf())?.toMutableList()
        val followingObs = followingPublicationsIDs?.let {
            graphQlUtil
                    .getPublicationsByIDs(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
        if (followingObs != null) {
            disposables.add(followingObs.subscribe { response ->
                val followingPublications = mutableListOf<Publication>()
                val publications = response.data?.getPublicationsByIDs
                if (publications != null) {
                    publications.mapTo(followingPublications, { publication ->
                        Publication(
                                publication.id,
                                publication.backgroundImageURL,
                                publication.bio,
                                publication.name,
                                publication.profileImageURL,
                                publication.rssName,
                                publication.rssURL,
                                publication.slug,
                                publication.shoutouts,
                                publication.websiteURL,
                                Article(
                                        publication.mostRecentArticle?.id,
                                        publication.mostRecentArticle?.title,
                                        publication.mostRecentArticle?.articleURL,
                                        publication.mostRecentArticle?.imageURL))
                    })
                    if (!isRefreshing) {
                        followpublicationRV = binding.rvFollowing
                        followpublicationRV.adapter = FollowingHorizontalAdapter(followingPublications)
                        val linearLayoutManager = LinearLayoutManager(context)
                        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                        followpublicationRV.layoutManager = linearLayoutManager
                        followpublicationRV.setHasFixedSize(true)
                    } else {
                        val adapter = followpublicationRV.adapter as FollowingHorizontalAdapter
                        adapter.clear()
                        adapter.addAll(followingPublications)
                    }
                }
            })
        }
        if (followingPublicationsIDs?.isEmpty() == true) {
            binding.groupNotFollowing.visibility = View.VISIBLE
        } else {
            binding.groupNotFollowing.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.let {
            getMorePublications(it)
            getFollowingPublications(it, isRefreshing = this::followpublicationRV.isInitialized)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}