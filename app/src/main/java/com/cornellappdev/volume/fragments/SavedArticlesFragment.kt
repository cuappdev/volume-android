package com.cornellappdev.volume.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.api.Response
import com.cornellappdev.volume.NoInternetActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.SettingsActivity
import com.cornellappdev.volume.adapters.SavedArticlesAdapter
import com.cornellappdev.volume.databinding.FragmentSavedArticlesBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import com.kotlin.graphql.ArticlesByIDsQuery
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Fragment for the saved articles page, displays articles that the user saved to read later.
 *
 *  @see {@link com.cornellappdev.volume.R.layout#fragment_saved_articles}
 */
class SavedArticlesFragment : Fragment() {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val prefUtils = PrefUtils()
    private var disposables = CompositeDisposable()
    private val graphQlUtil = GraphQlUtil()
    private var _binding: FragmentSavedArticlesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedArticlesBinding.inflate(inflater, container, false)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                setupSavedArticlesFragment()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSavedArticlesFragment()
    }

    /**
     * Sets up the entirety of the saved articles fragment, adds onClicks, checks for internet, and sets up
     * the saved articles.
     */
    private fun setupSavedArticlesFragment() {
        disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
            if (!hasInternet) {
                resultLauncher.launch(Intent(context, NoInternetActivity::class.java))
            } else {
                loadSavedArticles()

                val volumeOrange: Int? =
                    context?.let { ContextCompat.getColor(it, R.color.volume_orange) }
                with(binding.srlQuery) {
                    if (volumeOrange != null) {
                        setColorSchemeColors(volumeOrange)
                    }

                    // Re-populates the RecyclerViews on refresh, is dependent on whether or not they are
                    // initialized.
                    setOnRefreshListener {
                        loadSavedArticles()
                        // After repopulating, can stop signifying the refresh animation.
                        binding.srlQuery.isRefreshing = false
                    }
                }

                binding.ivSettings.setOnClickListener {
                    val intent = Intent(requireActivity(), SettingsActivity::class.java)
                    requireActivity().startActivity(intent)
                }
            }
        })
    }

    /**
     * Loads all of the saved articles.
     */
    private fun loadSavedArticles() {
        val savedArticleIds = prefUtils.getStringSet(PrefUtils.SAVED_ARTICLES_KEY, mutableSetOf())

        // Creates API call observation for retrieving all articles the user saved.
        val savedArticlesObs =
            graphQlUtil.getArticlesByIDs(savedArticleIds as MutableSet<String>).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        if (savedArticleIds.isNotEmpty()) {
            handleSavedArticleObservable(savedArticlesObs)
        } else {
            binding.noSavedArticlesGroup.visibility = View.VISIBLE
            binding.clBookmarkPage.visibility = View.GONE
            binding.fragmentContainer.visibility = View.GONE
        }
    }

    /**
     * Parses the raw publications from our ArticlesByID query, turning them into our Article
     * model, adding said Articles to the list passed in.
     */
    private fun retrieveArticlesByIDFromResponse(
        response: Response<ArticlesByIDsQuery.Data>?,
        savedArticles: MutableList<Article>
    ) {
        response?.data?.getArticlesByIDs?.mapTo(savedArticles, { article ->
            val publication = article.publication
            Article(
                title = article.title,
                articleURL = article.articleURL,
                date = article.date.toString(),
                id = article.id,
                imageURL = article.imageURL,
                publication = Publication(
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
                shoutouts = article.shoutouts,
                nsfw = article.nsfw
            )
        })
    }

    /**
     * Retrieves the articles the user saved and initializes the following publications
     * RecyclerView.
     */
    private fun handleSavedArticleObservable(savedArticlesObs: Observable<Response<ArticlesByIDsQuery.Data>>?) {
        if (savedArticlesObs != null) {
            val savedArticles = mutableListOf<Article>()

            disposables.add(savedArticlesObs.subscribe { response ->
                retrieveArticlesByIDFromResponse(response, savedArticles)
                binding.noSavedArticlesGroup.visibility = View.GONE
                binding.clBookmarkPage.visibility = View.VISIBLE
                binding.fragmentContainer.visibility = View.VISIBLE
                with(binding.rvSavedArticles) {
                    adapter = SavedArticlesAdapter(savedArticles)
                    layoutManager = LinearLayoutManager(context)
                }
            })
        }
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