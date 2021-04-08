package com.cornellappdev.volume.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cornellappdev.volume.R
import com.cornellappdev.volume.SettingsActivity
import com.cornellappdev.volume.adapters.SavedArticlesAdapter
import com.cornellappdev.volume.databinding.FragmentSavedArticlesBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SavedArticlesFragment : Fragment() {

    private val prefUtils = PrefUtils()
    private var disposables = CompositeDisposable()
    private val graphQlUtil = GraphQlUtil()
    private var _binding: FragmentSavedArticlesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentSavedArticlesBinding.inflate(inflater, container, false)
        if (prefUtils.getStringSet(PrefUtils.SAVED_ARTICLES_KEY, mutableSetOf())?.isEmpty() == true) {
            binding.noSavedArticlesGroup.visibility = View.VISIBLE
        } else {
            disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
                if (hasInternet) {
                    binding.noSavedArticlesGroup.visibility = View.GONE
                    loadArticles(binding)
                } else {
                    binding.clBookmarkPage.visibility = View.GONE
                    binding.ivSettings.visibility = View.GONE
                    binding.fragmentContainer.visibility = View.VISIBLE
                    val ft = childFragmentManager.beginTransaction()
                    val dialog = NoInternetDialog()
                    ft.replace(binding.fragmentContainer.id, dialog, NoInternetDialog.TAG).commit()
                }
            })
        }
        val volumeOrange: Int? = context?.let { ContextCompat.getColor(it, R.color.volumeOrange) }
        if (volumeOrange != null) {
            binding.srlQuery.setColorSchemeColors(volumeOrange, volumeOrange, volumeOrange)
        }
        binding.srlQuery.setOnRefreshListener {
            loadArticles(binding)
            binding.srlQuery.isRefreshing = false
        }

        binding.ivSettings.setOnClickListener {
            val intent = Intent(requireActivity(), SettingsActivity::class.java)
            requireActivity().startActivity(intent)
        }

        return binding.root
    }

    private fun loadArticles(savedArticlesBinding: FragmentSavedArticlesBinding) {
        val articleIds = prefUtils.getStringSet(PrefUtils.SAVED_ARTICLES_KEY, mutableSetOf())?.toMutableSet()
        val obs = articleIds?.let { graphQlUtil.getArticlesByIDs(it).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }
        val savedArticles = mutableListOf<Article>()
        if (obs != null) {
            disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
                if (hasInternet) {
                    childFragmentManager.findFragmentByTag(NoInternetDialog.TAG).let { dialogFrag ->
                        (dialogFrag as? DialogFragment)?.dismiss()
                    }
                    binding.clBookmarkPage.visibility = View.VISIBLE
                    binding.ivSettings.visibility = View.VISIBLE
                    disposables.add(obs.subscribe { response ->
                        response.data?.getArticlesByIDs?.mapTo(savedArticles, { article ->
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
                                            socials = publication.socials.toList().map { Social(it.social, it.uRL) }),
                                    shoutouts = article.shoutouts,
                                    nsfw = article.nsfw)
                        })
                        if (context != null) {
                            savedArticlesBinding.rvSavedArticles.adapter = SavedArticlesAdapter(savedArticles)
                            val layoutManager = LinearLayoutManager(context)
                            savedArticlesBinding.rvSavedArticles.layoutManager = layoutManager
                            savedArticlesBinding.noSavedArticlesGroup.visibility = if (savedArticles.isEmpty()) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                        }
                    })
                } else {
                    binding.clBookmarkPage.visibility = View.GONE
                    val ft = childFragmentManager.beginTransaction()
                    val dialog = NoInternetDialog()
                    ft.replace(binding.fragmentContainer.id, dialog, NoInternetDialog.TAG).commit()
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        loadArticles(binding)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}