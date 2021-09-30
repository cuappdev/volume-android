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

/**
 * This activity is primarily used for viewing article pages.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        disposables = CompositeDisposable()

        // Grabs Article from the intent passed in.
        val article = intent.getParcelableExtra<Article>("article")

        setUpWebView(article)

        // Code to prevent from launching in external browser, but instead within the Volume app.
        binding.wvArticle.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
    }

    /**
     * Sets up the webview using the article passed in.
     */
    private fun setUpWebView(article: Article?) {
        // Makes sure the user has internet before attempting to load the url or else the app crashes.
        disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
            if (article?.articleURL != null) {
                binding.wvTop.setName(article)
                binding.wvBottom.setArticle(article)
                binding.wvBottom.setUpView()
                binding.wvArticle.setTopBot(binding.wvTop, binding.wvBottom)
                if (hasInternet) binding.wvArticle.loadUrl(article.articleURL)
            }
        })
    }
}
