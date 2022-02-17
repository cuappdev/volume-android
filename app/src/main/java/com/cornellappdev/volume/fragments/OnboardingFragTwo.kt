package com.cornellappdev.volume.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.NoInternetActivity
import com.cornellappdev.volume.adapters.MorePublicationsAdapter
import com.cornellappdev.volume.databinding.FragmentOnboardingTwoBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.util.ActivityForResultConstants
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
class OnboardingFragTwo : Fragment(), MorePublicationsAdapter.AdapterOnClickHandler, MorePublicationsAdapter.AdapterOnClicker {

    interface DataPassListener {
        fun onPublicationFollowed(numFollowed: Int)
    }

    private lateinit var mCallback: DataPassListener
    private lateinit var publicationRV: RecyclerView
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var prefUtils: PrefUtils
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private var _binding: FragmentOnboardingTwoBinding? = null
    private val binding get() = _binding!!
    private var followCounter = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefUtils = PrefUtils(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        mCallback = activity as DataPassListener
        _binding = FragmentOnboardingTwoBinding.inflate(inflater, container, false)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == ActivityForResultConstants.FROM_NO_INTERNET.code) {
                setupOnboardingFragment()
            }
        }
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnboardingFragment()
    }

    private fun setupOnboardingFragment() {
        disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
            if (!hasInternet) {
                resultLauncher.launch(Intent(context, NoInternetActivity::class.java))
            } else {
                if (!this@OnboardingFragTwo::publicationRV.isInitialized) {
                    setupPublicationRV(binding)
                    // Notifies that no one has been followed.
                    mCallback.onPublicationFollowed(0)
                }
            }
        })
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
                            Publication(
                                id = publication.id,
                                backgroundImageURL = publication.backgroundImageURL,
                                bio = publication.bio,
                                name = publication.name,
                                profileImageURL = publication.profileImageURL,
                                rssName = publication.rssName,
                                rssURL = publication.rssURL,
                                slug = publication.slug,
                                shoutouts = publication.shoutouts,
                                websiteURL = publication.websiteURL,
                                socials = publication.socials.toList()
                                    .map { Social(it.social, it.uRL) }),
                            publication.mostRecentArticle.date.toString(),
                            publication.mostRecentArticle.shoutouts,
                            isNSFW,
                        )
                    },
                    publication.socials.toList()
                        .map { social -> Social(social.social, social.uRL) })
            })

            publicationRV = onboardingBinding.rvPublications
            with(publicationRV) {
                adapter =
                    MorePublicationsAdapter(allPublicationsList, prefUtils, this@OnboardingFragTwo, mAdapterOnClicker = this@OnboardingFragTwo)
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
        })
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

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMorePublicationClicked(publication: Publication, isOnboarding: Boolean) = Unit
}