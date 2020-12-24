package com.example.volume_android.networking

import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.example.volume_android.models.Article
import com.kotlin.graphql.QueryAllArticlesQuery
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class GraphQlUtil {

    private val BASE_URL = "http://volume-backend.cornellappdev.com/graphql"
    private lateinit var  client : ApolloClient
    private lateinit var  allArticles: List<QueryAllArticlesQuery.GetAllArticle>

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

    fun getAllArticles(){

        client.query(QueryAllArticlesQuery()).enqueue(object: ApolloCall.Callback<QueryAllArticlesQuery.Data?>() {
            override fun onFailure(e: ApolloException) {
                Log.e("Error", e.toString())
            }

            override fun onResponse(response: Response<QueryAllArticlesQuery.Data?>) {

                var newArticleList = mutableListOf<Article>()

                //Convert GraphQL articles to FrontEnd Model
                response.data?.getAllArticles?.mapTo(newArticleList, {
                    it -> Article(it.articleURL, it.date.toString(), it.id, it.imageURL, it.publicationID, it.shoutouts, it.title)
                })

                Log.d("RETURN", newArticleList.toString())

            }


        })



    }

    fun parseArticleList(apolloArticles: List<QueryAllArticlesQuery.GetAllArticle>){


    }
}


