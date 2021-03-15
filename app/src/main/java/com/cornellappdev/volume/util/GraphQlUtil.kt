package com.cornellappdev.volume.util

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

    private val BASE_URL = "https://volume-backend.cornellappdev.com/graphql"
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

    fun getArticleByPublicationID(pubID: String): Observable<Response<ArticlesByPublicationIDQuery.Data>> {
        val query = (ArticlesByPublicationIDQuery(pubID))
        return client.rxQuery(query)
    }

    fun getArticleByPublicationIDs(pubIDs: MutableList<String>):Observable<Response<ArticlesByPublicationIDsQuery.Data>>{
        val query = (ArticlesByPublicationIDsQuery(pubIDs.toList()))
        return client.rxQuery(query)
    }

    fun getPublicationByID(pubID: String): Observable<Response<PublicationByIDQuery.Data>> {
        val query = (PublicationByIDQuery(pubID))
        return client.rxQuery(query)
    }

    fun getPublicationsByIDs(pubIDs: MutableList<String>): Observable<Response<PublicationsByIDsQuery.Data>> {
        val query = (PublicationsByIDsQuery(pubIDs.toList()))
        return client.rxQuery(query)
    }

    fun getArticlesByIDs(ids:MutableSet<String>):Observable<Response<ArticlesByIDsQuery.Data>>{
        val query = (ArticlesByIDsQuery(ids.toList()))
        return client.rxQuery(query)
    }

    fun getArticleByID(id: String): Observable<Response<ArticleByIDQuery.Data>> {
        val query = (ArticleByIDQuery(id))
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


