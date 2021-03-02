package com.cornellappdev.volume

import android.os.Bundle
import android.os.PersistableBundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.views.ObservableWebView
import com.cornellappdev.volume.views.WebviewBottom
import com.cornellappdev.volume.views.WebviewTop
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var webView: ObservableWebView
    private lateinit var topWebView: WebviewTop
    private lateinit var bottomWebView: WebviewBottom

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        topWebView = findViewById(R.id.webview_top)
        bottomWebView = findViewById(R.id.webview_bot)

        //Grab Article
        val article = intent.getParcelableExtra<Article>("article")

        if(article?.articleURL != null) {
            webView = findViewById(R.id.webview)
            webView.loadUrl(article.articleURL)
            topWebView.setName(article)
            bottomWebView.setArticle(article)
            bottomWebView.setUpView()
            webview.setTopBot(topWebView, bottomWebView)
        }

        //Code to prevent from launching in external browser
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }

    }
}