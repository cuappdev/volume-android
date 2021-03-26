package com.cornellappdev.volume.fragments

import PrefUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cornellappdev.volume.adapters.MorePublicationsAdapter
import com.cornellappdev.volume.databinding.FragmentOnboardingTwoBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.util.GraphQlUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class OnboardingFragTwo : Fragment(), MorePublicationsAdapter.AdapterOnClickHandler {

    interface DataPassListener {
        fun onPublicationFollowed(numFollowed: Int)
    }

    private lateinit var disposables: CompositeDisposable
    private val graphQlUtil = GraphQlUtil()
    private val prefUtils = PrefUtils()
    private lateinit var mCallback: DataPassListener
    private var _binding: FragmentOnboardingTwoBinding? = null
    private val binding get() = _binding!!
    private var followCounter = 0

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        disposables = CompositeDisposable()
        mCallback = activity as DataPassListener
        _binding = FragmentOnboardingTwoBinding.inflate(inflater, container, false)
        setupArticlesRV(binding)
        mCallback.onPublicationFollowed(0)
        return binding.root
    }

    private fun setupArticlesRV(onboardingBinding: FragmentOnboardingTwoBinding) {
        val pubsObs = graphQlUtil.getAllPublications().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(pubsObs.subscribe {
            val allPubs = mutableListOf<Publication>()
            it.data?.getAllPublications?.mapTo(allPubs, { publication ->
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
                                nsfw = publication.mostRecentArticle?.nsfw)
                )
            })
            if (this.context != null) {
                onboardingBinding.rvPublications.adapter =
                        MorePublicationsAdapter(allPubs, prefUtils, this)
                onboardingBinding.rvPublications.layoutManager = LinearLayoutManager(context)
                onboardingBinding.rvPublications.setHasFixedSize(true)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onFollowClick(wasFollowed: Boolean) {
        if (wasFollowed) {
            followCounter++
        } else {
            followCounter = (followCounter - 1).coerceAtLeast(0)
        }
        mCallback.onPublicationFollowed(followCounter)
    }
}