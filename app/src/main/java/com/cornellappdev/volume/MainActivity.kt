package com.cornellappdev.volume

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cornellappdev.volume.databinding.ActivityMainBinding
import com.cornellappdev.volume.models.Article

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Grab Article
        val article = intent.getParcelableExtra<Article>("article")

        //experiment w/ behaviors with no internet, specfically loadUrl
        if (article?.articleURL != null) {
            binding.wvArticle.loadUrl(article.articleURL)
            binding.wvTop.setName(article)
            binding.wvBottom.setArticle(article)
            binding.wvBottom.setUpView()
            binding.wvArticle.setTopBot(binding.wvTop, binding.wvBottom)
        }

        // Code to prevent from launching in external browser
        binding.wvArticle.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
    }
}