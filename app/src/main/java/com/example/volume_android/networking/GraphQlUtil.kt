package com.example.volume_android.networking

import com.apollographql.apollo.ApolloClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class GraphQlUtil {

    private val BASE_URL = "http://volume-backend.cornellappdev.com/graphql"
    private lateinit var  client : ApolloClient

    init {
        client = setUpApolloCllient()

    }

    
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