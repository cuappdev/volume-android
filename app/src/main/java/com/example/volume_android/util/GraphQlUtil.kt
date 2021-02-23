package com.example.volume_android.util

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.rx2.rxMutate
import com.apollographql.apollo.rx2.rxQuery
import com.kotlin.graphql.*
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class GraphQlUtil {

    private val BASE_URL = "http://volume-dev.cornellappdev.com/graphql"
    private var client : ApolloClient

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
                .serverUrl(BASE_URL)
                .okHttpClient(okHttp.build())
                .build()
    }

    fun getAllArticles(): Observable<Response<AllArticlesQuery.Data>> {
        val query = AllArticlesQuery()
        return client.rxQuery(query)
    }

    fun getTrendingArticles(limit: Double): Observable<Response<TrendingArticlesQuery.Data>> {
        val query = (TrendingArticlesQuery(limit.toInput()))
        return client.rxQuery(query)
    }

    fun getAllPublications(): Observable<Response<AllPublicationsQuery.Data>> {
        val query = AllPublicationsQuery()
        return client.rxQuery(query)
    }

    fun getArticleByPublication(pubID: String): Observable<Response<ArticlesByPublicationQuery.Data>> {
        val query = (ArticlesByPublicationQuery(pubID))
        return client.rxQuery(query)
    }

    fun getPublicationById(pubID: String): Observable<Response<PublicationByIdQuery.Data>> {
        val query = (PublicationByIdQuery(pubID))
        return client.rxQuery(query)
    }

    fun getArticlesByIds(ids:MutableSet<String>):Observable<Response<ArticlesByIDsQuery.Data>>{
        val query = (ArticlesByIDsQuery(ids.toList()))
        return client.rxQuery(query)
    }

    fun likeArticle(id: String): Single<Response<IncrementShoutoutMutation.Data>> {
        val mutation = (IncrementShoutoutMutation(id))
        return client.rxMutate(mutation)
    }

    fun getArticlesAfterDate(since: String): Observable<Response<ArticlesAfterDateQuery.Data>>{
        val query = ArticlesAfterDateQuery(since)
        return client.rxQuery(query)
    }


}


