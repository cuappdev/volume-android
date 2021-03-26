package com.cornellappdev.volume.fragments

import PrefUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cornellappdev.volume.adapters.SavedArticlesAdapter
import com.cornellappdev.volume.databinding.FragmentSavedArticlesBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.util.GraphQlUtil
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
        loadArticles(binding)
        return binding.root
    }

    private fun loadArticles(savedArticlesBinding: FragmentSavedArticlesBinding) {
        val articleIds = prefUtils.getStringSet(PrefUtils.SAVED_ARTICLES_KEY, mutableSetOf())?.toMutableSet()
        val obs = articleIds?.let { graphQlUtil.getArticlesByIDs(it).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }
        val savedArticles = mutableListOf<Article>()
        if (obs != null) {
            disposables.add(obs.subscribe {
                it.data?.getArticlesByIDs?.mapTo(savedArticles, { article ->
                    Article(
                            article.id,
                            article.title,
                            article.articleURL,
                            article.imageURL,
                            Publication(
                                    id = article.publication.id,
                                    name = article.publication.name,
                                    profileImageURL = article.publication.profileImageURL),
                            article.date.toString(),
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
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.let { loadArticles(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}