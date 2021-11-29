package com.cornellappdev.volume

import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cornellappdev.volume.analytics.EventType
import com.cornellappdev.volume.analytics.NavigationSource
import com.cornellappdev.volume.analytics.VolumeEvent
import com.cornellappdev.volume.databinding.ActivityMainBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.util.ActivityForResultConstants
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import io.reactivex.disposables.CompositeDisposable

/**
 * This activity is primarily used for viewing article pages.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var article: Article
    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ActivityForResultConstants.FROM_NO_INTERNET.code) {
                    initializeMainActivity()
                }
            }

        disposables = CompositeDisposable()
        firebaseAnalytics = Firebase.analytics
        initializeMainActivity()
    }

    private fun initializeMainActivity() {
        // Makes sure the user has internet before attempting to load the url or else the app crashes.
        disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
            if (!hasInternet) {
                resultLauncher.launch(Intent(this, NoInternetActivity::class.java))
            } else {
                // Grabs Article from the intent passed in.
                article = intent.getParcelableExtra(Article.INTENT_KEY)!!
                val navigationSource =
                    intent.getParcelableExtra<NavigationSource>(NavigationSource.INTENT_KEY)!!

                // Sets up the webview using the article passed in.
                binding.wvTop.setName(article)
                binding.wvBottom.setArticle(article)
                binding.wvBottom.setUpView(PrefUtils(this), disposables)
                with(binding.wvArticle) {
                    setWebViews(binding.wvTop, binding.wvBottom)
                    // Code to prevent from launching in external browser, but instead within the Volume app.
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            return false
                        }
                    }
                    loadUrl(article.articleURL)
                }
                VolumeEvent.logEvent(
                    EventType.ARTICLE,
                    VolumeEvent.OPEN_ARTICLE,
                    navigationSource,
                    article.id
                )

            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        VolumeEvent.logEvent(EventType.ARTICLE, VolumeEvent.CLOSE_ARTICLE, id = article.id)
        disposables.clear()
    }

    override fun onBackPressed() {
        setResult(ActivityForResultConstants.FROM_MAIN_ACTIVITY.code)
        super.onBackPressed()
    }
}
