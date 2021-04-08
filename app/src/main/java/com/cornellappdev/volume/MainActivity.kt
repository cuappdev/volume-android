package com.cornellappdev.volume

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cornellappdev.volume.databinding.ActivityMainBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.util.GraphQlUtil
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        disposables = CompositeDisposable()

        // Grab Article
        val article = intent.getParcelableExtra<Article>("article")

        disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
            if (article?.articleURL != null) {
                binding.wvTop.setName(article)
                binding.wvBottom.setArticle(article)
                binding.wvBottom.setUpView()
                binding.wvArticle.setTopBot(binding.wvTop, binding.wvBottom)
                if (hasInternet) binding.wvArticle.loadUrl(article.articleURL)
            }
        })

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