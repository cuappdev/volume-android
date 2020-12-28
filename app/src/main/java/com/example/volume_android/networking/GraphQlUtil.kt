package com.example.volume_android.networking

import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.rx2.Rx2Apollo
import com.apollographql.apollo.rx2.rxQuery
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication
import com.kotlin.graphql.AllArticlesQuery
import com.kotlin.graphql.AllPublicationsQuery
import com.kotlin.graphql.TrendingArticlesQuery
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class GraphQlUtil {

    private val BASE_URL = "http://volume-backend.cornellappdev.com/graphql"
    private lateinit var  client : ApolloClient

    init {
        client = setUpApolloCllient()
    }

    private fun setUpApolloCllient(): ApolloClient {

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val okHttp = OkHttpClient
                .Builder()
                .addInterceptor(logging)
        return ApolloClient.builder()
                .serverUrl("http://volume-backend.cornellappdev.com/graphql")
                .okHttpClient(okHttp.build())
                .build()

    }

    fun getAllArticles(): Observable<Response<AllArticlesQuery.Data>> {
        val query = AllArticlesQuery()
        return client.rxQuery(query)
    }

    fun getTrendingArticles(limit: Double, date: String): Observable<Response<TrendingArticlesQuery.Data>> {
        val query = (TrendingArticlesQuery(limit.toInput(), date))
        return client.rxQuery(query)
    }

//    fun getAllPublications() : List<Publication>{
//
//        var newPubList = mutableListOf<Publication>()
//
//        client.query(AllPublicationsQuery()).enqueue(object: ApolloCall.Callback<AllPublicationsQuery.Data?>() {
//            override fun onFailure(e: ApolloException) {
//                Log.e("Error", e.toString())
//            }
//
//            override fun onResponse(response: Response<AllPublicationsQuery.Data?>) {
//
//                //Convert GraphQL articles to FrontEnd Model
//                response.data?.getAllPublications?.mapTo(newPubList, {
//                    it -> Publication(it.bio, it.id, it.imageURL, it.name, it.rssName, it.rssURL, it.shoutouts, it.websiteURL)
//                })
//            }
//
//        })
//
//        return newPubList
//    }

}


