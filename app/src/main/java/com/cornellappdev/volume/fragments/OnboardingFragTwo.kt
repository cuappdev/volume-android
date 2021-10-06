package com.cornellappdev.volume.fragments

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
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Page two of Onboarding.
 *
 * @see {@link com.cornellappdev.volume.R.layout#fragment_onboarding_two}
 */
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        disposables = CompositeDisposable()
        mCallback = activity as DataPassListener
        _binding = FragmentOnboardingTwoBinding.inflate(inflater, container, false)
        setupPublicationRV(binding)
        // Notifies that no one has been followed.
        mCallback.onPublicationFollowed(0)
        return binding.root
    }

    /**
     * Sets up the RecyclerView showcasing all the publications on Volume.
     */
    private fun setupPublicationRV(onboardingBinding: FragmentOnboardingTwoBinding) {
        // Creates API call observation for retrieving all publications in the Volume database.
        val allPublicationsObs =
            graphQlUtil.getAllPublications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        val allPublicationsList = mutableListOf<Publication>()


        disposables.add(allPublicationsObs.subscribe { response ->
            response.data?.getAllPublications?.mapTo(allPublicationsList, { publication ->
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
                    publication.mostRecentArticle?.nsfw?.let { isNSFW ->
                        Article(
                            publication.mostRecentArticle.id,
                            publication.mostRecentArticle.title,
                            publication.mostRecentArticle.articleURL,
                            publication.mostRecentArticle.imageURL,
                            nsfw = isNSFW
                        )
                    },
                    publication.socials.toList()
                        .map { social -> Social(social.social, social.uRL) })
            })

            if (this.context != null) {
                onboardingBinding.rvPublications.adapter =
                    MorePublicationsAdapter(allPublicationsList, prefUtils, this)
                onboardingBinding.rvPublications.layoutManager = LinearLayoutManager(context)
                onboardingBinding.rvPublications.setHasFixedSize(true)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Detects and notifies when a user follows/unfollows through a callback with {@link MorePublicationsAdapter}.
     */
    override fun onFollowClick(wasFollowed: Boolean) {
        if (wasFollowed) {
            followCounter++
        } else {
            followCounter = (followCounter - 1).coerceAtLeast(0)
        }
        mCallback.onPublicationFollowed(followCounter)
    }
}