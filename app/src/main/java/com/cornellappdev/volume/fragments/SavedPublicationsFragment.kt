package com.cornellappdev.volume.fragments

import PrefUtils
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.R
import com.cornellappdev.volume.adapters.SavedArticlesAdapter
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.util.GraphQlUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SavedPublicationsFragment : Fragment() {

    private lateinit var savedArticlesRV: RecyclerView
    private val prefUtils = PrefUtils()
    private var disposables = CompositeDisposable()
    private val graphQlUtil = GraphQlUtil()
    private lateinit var savedArticleView: View

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        savedArticleView = inflater.inflate(R.layout.bookmarks_page, container, false)
        loadArticles(savedArticleView)
        return savedArticleView
    }

    @SuppressLint("LongLogTag")
    private fun loadArticles(view: View) {
        val articleIds = prefUtils.getStringSet("savedArticles", mutableSetOf())?.toMutableSet()
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
                savedArticlesRV = view.findViewById(R.id.saved_articles_rv)
                savedArticlesRV.adapter = SavedArticlesAdapter(savedArticles)
                val layoutManager = LinearLayoutManager(view.context)
                savedArticlesRV.layoutManager = layoutManager
                view.findViewById<Group>(R.id.no_saved_articles_group).visibility = if (savedArticles.isEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        loadArticles(savedArticleView)
    }
}