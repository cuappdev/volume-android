package com.cornellappdev.volume.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.api.Response
import com.cornellappdev.volume.NoInternetActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.adapters.FollowingHorizontalAdapter
import com.cornellappdev.volume.adapters.MorePublicationsAdapter
import com.cornellappdev.volume.databinding.FragmentPublicationsBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.GraphQlUtil.Companion.hasInternetConnection
import com.cornellappdev.volume.util.PrefUtils
import com.kotlin.graphql.AllPublicationsQuery
import com.kotlin.graphql.PublicationsByIDsQuery
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Fragment for the publications page, displays publications that the user is currently following
 * and all other publications the user isn't.
 *
 *  @see {@link com.cornellappdev.volume.R.layout#fragment_publications}
 */
class PublicationsFragment : Fragment() {

    private lateinit var followingPublicationsRV: RecyclerView
    private lateinit var morePublicationsRV: RecyclerView
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val graphQlUtil = GraphQlUtil()
    private val disposables = CompositeDisposable()
    private val prefUtils = PrefUtils()
    private var _binding: FragmentPublicationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentPublicationsBinding.inflate(inflater, container, false)
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    setupPublicationFragment()
                }
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPublicationFragment()
    }

    /**
     * Sets up the entirety of the publication fragment, adds onClicks, checks for internet, and sets up
     * the articles.
     */
    private fun setupPublicationFragment() {
        disposables.add(hasInternetConnection().subscribe { hasInternet ->
            if (!hasInternet) {
                resultLauncher.launch(Intent(context, NoInternetActivity::class.java))
            } else {
                setupPublicationsView(
                    binding,
                    isRefreshing = this@PublicationsFragment::followingPublicationsRV.isInitialized &&
                            this@PublicationsFragment::morePublicationsRV.isInitialized
                )

                val volumeOrange: Int? =
                    context?.let { ContextCompat.getColor(it, R.color.volume_orange) }
                with(binding.srlQuery) {
                    if (volumeOrange != null) {
                        setColorSchemeColors(volumeOrange)
                    }

                    // Re-populates the RecyclerViews on refresh, is dependent on whether or not they are
                    // initialized.
                    setOnRefreshListener {
                        disposables.add(hasInternetConnection().subscribe { hasInternet ->
                            if (!hasInternet) {
                                startActivity(Intent(context, NoInternetActivity::class.java))
                            } else {
                                setupPublicationsView(
                                    binding,
                                    isRefreshing = (this@PublicationsFragment::followingPublicationsRV.isInitialized &&
                                            this@PublicationsFragment::morePublicationsRV.isInitialized)

                                )
                            }
                            // After repopulating, can stop signifying the refresh animation.
                            binding.srlQuery.isRefreshing = false
                        })
                    }
                }
            }
        })
    }

    /**
     * Sets up the publication view.
     */
    private fun setupPublicationsView(binding: FragmentPublicationsBinding, isRefreshing: Boolean) {
        val followingPublicationsIDs =
            prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf())

        // Creates API call observation for retrieving all publications the user follows.
        val followingObs =
            followingPublicationsIDs.let {
                graphQlUtil
                    .getPublicationsByIDs(it.toMutableList())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }

        // Creates API call observation for retrieving all publications in the Volume database.
        val allPublicationsObs =
            graphQlUtil.getAllPublications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())


        childFragmentManager.findFragmentByTag(NoInternetDialog.TAG).let { dialogFrag ->
            (dialogFrag as? DialogFragment)?.dismiss()
        }

        binding.clPublicationPage.visibility = View.VISIBLE

        if (!followingPublicationsIDs.isNullOrEmpty()) {
            handleFollowingObservable(
                followingObs,
                isRefreshing
            )
        } else if (isRefreshing) {
            // Can simply just clear adapter, since there's no following article data
            // to populate from (the user doesn't follow any publications).
            val adapter = followingPublicationsRV.adapter as FollowingHorizontalAdapter
            adapter.clear()
        }

        // It's important that handleMorePublicationObservable comes after handleFollowingObservable
        // since we filter what other publications to display based on what publications the user follows.
        handleMorePublicationObservable(
            allPublicationsObs,
            isRefreshing,
            followingPublicationsIDs as HashSet<String>?
        )

        // Updates the UI if the user doesn't follow any publications.
        if (followingPublicationsIDs.isEmpty()) {
            binding.groupFollowing.visibility = View.GONE
            binding.groupNotFollowing.visibility = View.VISIBLE
        }
    }

    /**
     * Parses the raw publications from our PublicationsByIDs query, turning them into our Publication
     * model, adding said publications to the list passed in.
     */
    private fun retrievePublicationsByIDFromResponse(
        response: Response<PublicationsByIDsQuery.Data>,
        publicationsList: MutableList<Publication>
    ) {
        response.data?.getPublicationsByIDs?.mapTo(publicationsList, { publication ->
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
    }

    /**
     * Parses the raw publications from our AllPublications query, turning them into our Publication
     * model, adding said publications to the list passed in.
     */
    private fun retrieveAllPublicationsFromResponse(
        response: Response<AllPublicationsQuery.Data>,
        publicationsList: MutableList<Publication>
    ) {
        response.data?.getAllPublications?.mapTo(publicationsList, { publication ->
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
    }

    /**
     * Retrieves the publications the user follows and initializes/refreshes the following publications
     * RecyclerView.
     */
    private fun handleFollowingObservable(
        followingObs: Observable<Response<PublicationsByIDsQuery.Data>>?,
        isRefreshing: Boolean
    ) {
        val followingPublications = mutableListOf<Publication>()
        if (followingObs != null) {
            disposables.add(followingObs.subscribe { response ->
                retrievePublicationsByIDFromResponse(response, followingPublications)

                // If not refreshing, must initialize the followingPublicationsRV
                if (!isRefreshing) {
                    followingPublicationsRV = binding.rvFollowing
                    with(followingPublicationsRV) {
                        adapter = FollowingHorizontalAdapter(followingPublications)
                        layoutManager = LinearLayoutManager(context)
                        (layoutManager as LinearLayoutManager).orientation =
                            LinearLayoutManager.HORIZONTAL
                        setHasFixedSize(true)
                    }
                } else {
                    // followingPublicationsRV is already created if initialized, only need to repopulate adapter data.
                    val adapter =
                        followingPublicationsRV.adapter as FollowingHorizontalAdapter
                    adapter.clear()
                    adapter.addAll(followingPublications)
                }

                binding.groupNotFollowing.visibility = View.GONE
                binding.groupFollowing.visibility = View.VISIBLE
                binding.shimmerFollowingPublication.visibility = View.INVISIBLE
            })
        }
    }

    /**
     * Retrieves the other publications that the user does not follow
     * and initializes/refreshes the more publications RecyclerView.
     */
    private fun handleMorePublicationObservable(
        allPublicationsObs: Observable<Response<AllPublicationsQuery.Data>>,
        isRefreshing: Boolean,
        followingPublicationsIDs: HashSet<String>?
    ) {
        val morePublications = mutableListOf<Publication>()
        disposables.add(allPublicationsObs.subscribe { response ->
            retrieveAllPublicationsFromResponse(response, morePublications)

            // Removes from morePublications all the publications that the user follows.
            if (followingPublicationsIDs != null) {
                morePublications.removeAll { publication ->
                    followingPublicationsIDs.contains(publication.id)
                }
            }

            if (morePublications.isEmpty()) {
                // Hides the more publication section if the user follows all publications.
                binding.groupMorePublications.visibility = View.GONE
            } else {
                binding.groupMorePublications.visibility = View.VISIBLE

                // If not refreshing, must initialize the morePublicationsRV.
                if (!isRefreshing) {
                    morePublicationsRV = binding.rvMorePublications
                    with(morePublicationsRV) {
                        adapter =
                            MorePublicationsAdapter(morePublications, prefUtils, null)
                        layoutManager = LinearLayoutManager(context)
                        setHasFixedSize(true)
                    }
                } else {
                    // morePublicationsRV is already created if initialized, only need to repopulate adapter data.
                    val adapter =
                        morePublicationsRV.adapter as MorePublicationsAdapter
                    adapter.clear()
                    adapter.addAll(morePublications)
                }
            }
            binding.shimmerMorePublication.visibility = View.INVISIBLE
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}