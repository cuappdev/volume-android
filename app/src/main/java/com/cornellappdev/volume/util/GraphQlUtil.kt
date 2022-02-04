package com.cornellappdev.volume.util

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.rx2.rxMutate
import com.apollographql.apollo.rx2.rxQuery
import com.cornellappdev.volume.BuildConfig
import com.kotlin.graphql.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Holds the various API calls to our backend.
 *
 * The API specifications for these calls are on Playground, the GraphQL IDE.
 */
class GraphQlUtil {

    companion object {
        // When developing anything, make sure to use the development endpoint. When deploying, make sure
        // the endpoint is set to deployment.
        private const val IS_USING_DEV_ENDPOINT = true
        private val BASE_URL = if (IS_USING_DEV_ENDPOINT) BuildConfig.DEV_ENDPOINT else BuildConfig.PROD_ENDPOINT
        private val PING_URL: String = if (IS_USING_DEV_ENDPOINT) BuildConfig.DEV_PING else BuildConfig.PROD_PING
        private const val DEVICE_TYPE: String = "ANDROID"

        /**
         * Hits the deployment endpoint to make sure that 1.) the user has internet and 2.) that
         * the server is up.
         */
        fun hasInternetConnection(): Single<Boolean> {
            return Single.fromCallable {
                try {
                    val command = "ping -i 5 -c 1 $PING_URL"
                    return@fromCallable Runtime.getRuntime().exec(command).waitFor() == 0
                } catch (e: IOException) {
                    return@fromCallable false
                }
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }
    }

    private var client: ApolloClient = setUpApolloClient()

    private fun setUpApolloClient(): ApolloClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val okHttp = OkHttpClient
            .Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
        return ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttp.build())
            .build()
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

    fun getArticlesByPublicationIDs(pubIDs: MutableList<String>): Observable<Response<ArticlesByPublicationIDsQuery.Data>> {
        val query = (ArticlesByPublicationIDsQuery(pubIDs.toList()))
        return client.rxQuery(query)
    }

    fun getPublicationsByIDs(pubIDs: MutableList<String>): Observable<Response<PublicationsByIDsQuery.Data>> {
        val query = (PublicationsByIDsQuery(pubIDs.toList()))
        return client.rxQuery(query)
    }

    fun getArticlesByIDs(ids: MutableSet<String>): Observable<Response<ArticlesByIDsQuery.Data>> {
        val query = (ArticlesByIDsQuery(ids.toList()))
        return client.rxQuery(query)
    }

    fun getArticleByID(id: String): Observable<Response<ArticleByIDQuery.Data>> {
        val query = (ArticleByIDQuery(id))
        return client.rxQuery(query)
    }

    fun shoutoutArticle(id: String): Single<Response<IncrementShoutoutMutation.Data>> {
        val mutation = (IncrementShoutoutMutation(id))
        return client.rxMutate(mutation)
    }

    fun createUser(followedPublications: List<String>, deviceToken: String): Single<Response<CreateUserMutation.Data>> {
        val mutation = CreateUserMutation(DEVICE_TYPE, followedPublications, deviceToken)
        return client.rxMutate(mutation)
    }

    fun followPublication(pubID: String, uuid: String): Single<Response<FollowPublicationMutation.Data>> {
        val mutation = FollowPublicationMutation(pubID, uuid)
        return client.rxMutate(mutation)
    }

    fun unfollowPublication(pubID: String, uuid: String): Single<Response<UnfollowPublicationMutation.Data>> {
        val mutation = UnfollowPublicationMutation(pubID, uuid)
        return client.rxMutate(mutation)
    }
}
