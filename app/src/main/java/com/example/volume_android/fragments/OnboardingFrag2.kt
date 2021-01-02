package com.example.volume_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.R
import com.example.volume_android.adapters.BigReadHomeAdapter
import com.example.volume_android.adapters.FollowPublicationsAdapter
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication
import com.example.volume_android.util.GraphQlUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class OnboardingFrag2 (val publications: List<Publication>) : Fragment() {

    private lateinit var publicationRV : RecyclerView
    private lateinit var disposables: CompositeDisposable
    private val graphQlUtil = GraphQlUtil()


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.onboardingfrag2, container, false)

        disposables = CompositeDisposable()

        loadArticlesLoadRV(view)

        return view
    }

    private fun loadArticlesLoadRV(view: View) {
        val pubsObs = graphQlUtil.getAllPublications().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(pubsObs.subscribe{
            var allPubs = mutableListOf<Publication>()
            it.data?.getAllPublications?.mapTo(allPubs, { it -> Publication(it.id, it.backgroundImageURL,
                    it.bio, it.name, it.profileImageURL, it.rssName, it.rssURL, it.slug, it.shoutouts, it.websiteURL, Article())
            })
            publicationRV = view.findViewById(R.id.onboarding2_rv)
            publicationRV.adapter = FollowPublicationsAdapter(allPubs, view.context)
            publicationRV.layoutManager = LinearLayoutManager(view.context)
            publicationRV.setHasFixedSize(true)
        })
    }
}