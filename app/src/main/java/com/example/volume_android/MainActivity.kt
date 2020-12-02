package com.example.volume_android

import android.os.Bundle
import android.os.PersistableBundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.volume_android.views.ObservableWebView
import com.example.volume_android.views.WebviewBottom
import com.example.volume_android.views.WebviewTop
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

        webView = findViewById(R.id.webview)
        webView.loadUrl("https://www.nyt.com/")
        webview.setTopBot(topWebView, bottomWebView)

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