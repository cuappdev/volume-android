package com.example.volume_android.util

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.rx2.rxQuery
import com.kotlin.graphql.*
import io.reactivex.Observable
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

    fun getAllPublications(): Observable<Response<AllPublicationsQuery.Data>> {
        val query = AllPublicationsQuery()
        return client.rxQuery(query)
    }

    fun getArticleByPublication(pubID: String): Observable<Response<FollowingArticlesQuery.Data>> {
        val query = (FollowingArticlesQuery(pubID))
        return client.rxQuery(query)
    }

    fun getPublicationById(pubID: String): Observable<Response<PublicationByIdQuery.Data>> {
        val query = (PublicationByIdQuery(pubID))
        return client.rxQuery(query)
    }




}

