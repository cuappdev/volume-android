package com.cornellappdev.volume

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cornellappdev.volume.analytics.EventType
import com.cornellappdev.volume.analytics.NavigationSource
import com.cornellappdev.volume.analytics.VolumeEvent
import com.cornellappdev.volume.databinding.ActivityMainBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.util.GraphQlUtil
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import io.reactivex.disposables.CompositeDisposable

/**
 * This activity is primarily used for viewing article pages.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var disposables: CompositeDisposable
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var article: Article

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        disposables = CompositeDisposable()
        firebaseAnalytics = Firebase.analytics

        // Grabs Article from the intent passed in.
        article = intent.getParcelableExtra(Article.INTENT_KEY)!!
        val navigationSource = intent.getParcelableExtra<NavigationSource>(NavigationSource.INTENT_KEY)!!

        setupWebView(article, navigationSource)

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
    private fun setupWebView(article: Article?, navigationSource: NavigationSource) {
        // Makes sure the user has internet before attempting to load the url or else the app crashes.
        disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
            if (article?.articleURL != null) {
                binding.wvTop.setName(article)
                binding.wvBottom.setArticle(article)
                binding.wvBottom.setUpView()
                binding.wvArticle.setWebViews(binding.wvTop, binding.wvBottom)
                if (hasInternet) {
                    binding.wvArticle.loadUrl(article.articleURL)
                    VolumeEvent.logEvent(
                        EventType.ARTICLE,
                        VolumeEvent.OPEN_ARTICLE,
                        navigationSource,
                        article.id
                    )
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        VolumeEvent.logEvent(EventType.ARTICLE, VolumeEvent.CLOSE_ARTICLE, id = article.id)
    }
}
