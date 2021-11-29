package com.cornellappdev.volume.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.api.Response
import com.cornellappdev.volume.NoInternetActivity
import com.cornellappdev.volume.PublicationProfileActivity
import com.cornellappdev.volume.adapters.FollowingHorizontalAdapter
import com.cornellappdev.volume.adapters.MorePublicationsAdapter
import com.cornellappdev.volume.analytics.NavigationSource
import com.cornellappdev.volume.analytics.NavigationSource.Companion.putParcelableExtra
import com.cornellappdev.volume.databinding.FragmentPublicationsBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.util.*
import com.cornellappdev.volume.util.GraphQlUtil.Companion.hasInternetConnection
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
class PublicationsFragment : Fragment(), FollowingHorizontalAdapter.AdapterOnClickHandler,
    MorePublicationsAdapter.AdapterOnClickHandler {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var prefUtils: PrefUtils
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private var _binding: FragmentPublicationsBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentPublicationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        graphQlUtil = GraphQlUtil()
        disposables = CompositeDisposable()

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ActivityForResultConstants.FROM_PUBLICATION_PROFILE_ACTIVITY.code) {
                    setupPublicationsView(binding, isRefreshing = true)
                }
                if (result.resultCode == ActivityForResultConstants.FROM_NO_INTERNET.code) {
                    setupPublicationFragment()
                }
            }

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
                    isRefreshing = false
                )
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

        binding.clPublicationPage.visibility = View.VISIBLE
        handleFollowingObservable(
            followingObs,
            isRefreshing
        )

        // It's important that handleMorePublicationObservable comes after handleFollowingObservable
        // since we filter what other publications to display based on what publications the user follows.
        handleMorePublicationObservable(
            allPublicationsObs,
            isRefreshing,
            followingPublicationsIDs as HashSet<String>?
        )
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
                    with(binding.rvFollowing) {
                        adapter = FollowingHorizontalAdapter(
                            followingPublications,
                            this@PublicationsFragment
                        )
                        layoutManager = LinearLayoutManager(context)
                        (layoutManager as LinearLayoutManager).orientation =
                            LinearLayoutManager.HORIZONTAL
                        setHasFixedSize(true)
                    }
                } else {
                    // followingPublicationsRV is already created if initialized, only need to repopulate adapter data.
                    val adapter =
                        binding.rvFollowing.adapter as FollowingHorizontalAdapter
                    val result = DiffUtil.calculateDiff(
                        DiffUtilCallbackPublication(
                            adapter.followedPublications,
                            followingPublications
                        )
                    )
                    adapter.followedPublications = followingPublications
                    result.dispatchUpdatesTo(adapter)
                }

                // Updates the UI if the user doesn't follow any publications.
                if (followingPublications.isEmpty()) {
                    binding.groupFollowing.visibility = View.GONE
                    binding.groupNotFollowing.visibility = View.VISIBLE
                } else {
                    binding.groupNotFollowing.visibility = View.GONE
                    binding.groupFollowing.visibility = View.VISIBLE
                }

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
                    with(binding.rvMorePublications) {
                        adapter =
                            MorePublicationsAdapter(
                                morePublications,
                                prefUtils,
                                this@PublicationsFragment
                            )
                        layoutManager = LinearLayoutManager(context)
                        setHasFixedSize(true)
                    }
                } else {
                    // morePublicationsRV is already created if initialized, only need to repopulate adapter data.
                    val adapter =
                        binding.rvMorePublications.adapter as MorePublicationsAdapter
                    val result = DiffUtil.calculateDiff(
                        DiffUtilCallbackPublication(
                            adapter.publicationList,
                            morePublications
                        )
                    )
                    adapter.publicationList = morePublications
                    result.dispatchUpdatesTo(adapter)
                }
            }
            binding.shimmerMorePublication.visibility = View.INVISIBLE
        })
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Handles the callback from the Following publications RecyclerView; onClick of a view
     * opens the publication profile for the specific publication.
     */
    override fun onPublicationClick(publication: Publication) {
        val intent = Intent(context, PublicationProfileActivity::class.java)
        intent.putExtra(Publication.INTENT_KEY, publication)
        intent.putParcelableExtra(
            NavigationSource.INTENT_KEY,
            NavigationSource.FOLLOWING_PUBLICATIONS
        )
        resultLauncher.launch(intent)
    }

    /**
     * Handles the callback from the follow button being pressed on the More publications RecyclerView;
     * refreshes both RecyclerViews to display the correct following list.
     */
    override fun onFollowClick(wasFollowed: Boolean) {
        setupPublicationsView(binding, isRefreshing = true)
    }
}