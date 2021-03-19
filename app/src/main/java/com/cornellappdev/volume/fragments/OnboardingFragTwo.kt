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

class OnboardingFragTwo : Fragment() {

    private lateinit var disposables: CompositeDisposable
    private val graphQlUtil = GraphQlUtil()
    private val prefUtils = PrefUtils()
    private var binding: FragmentOnboardingTwoBinding? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        disposables = CompositeDisposable()
        binding = FragmentOnboardingTwoBinding.inflate(inflater, container, false)
        setupArticlesRV(binding!!)
        return binding!!.root
    }

    private fun setupArticlesRV(onboardingBinding: FragmentOnboardingTwoBinding) {
        val pubsObs = graphQlUtil.getAllPublications().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(pubsObs.subscribe{
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
                                nsfw = publication.mostRecentArticle?.nsfw))
            })
            if (this.context != null) {
                onboardingBinding.rvPublications.adapter = MorePublicationsAdapter(allPubs, prefUtils)
                onboardingBinding.rvPublications.layoutManager = LinearLayoutManager(context)
                onboardingBinding.rvPublications.setHasFixedSize(true)
            }
        })
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}