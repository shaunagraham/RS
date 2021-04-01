package com.rap.sheet.view.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.rap.sheet.R
import com.rap.sheet.extenstion.beGone
import com.rap.sheet.extenstion.beInVisible
import com.rap.sheet.extenstion.beVisible
import com.rap.sheet.extenstion.click
import com.rap.sheet.view.common.BaseActivity
import kotlinx.android.synthetic.main.activity_help_privacy.*
import kotlinx.android.synthetic.main.progress_dialog_view.view.*
import kotlinx.android.synthetic.main.top_view_layout.view.*

class HelpPrivacyTermsActivity : BaseActivity() {

    private var title: String? = null
    private var url: String? = null
//    private lateinit var binding: ActivityHelpPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_help_privacy);
        setContentView(R.layout.activity_help_privacy)
        getIntentData()
        setUpToolBar()
        loadWebView()
    }


    private fun getIntentData() {
        intent.extras?.apply {
            title = this.getString("title")
            url = this.getString("url")
        }
    }


    private fun setUpToolBar() {
        toolbar2.imgClose.click {
            onBackPressed()
        }
        toolbar2.tvTitle.text = title
    }

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    private fun loadWebView() {
        val webSettings: WebSettings = webViewHelp.settings
        webSettings.javaScriptEnabled = true
        webViewHelp.webViewClient = MyWebViewClient()
        webViewHelp.loadUrl(url.toString())
    }

    private fun proceedUrl(view: WebView, uri: Uri) {
        when {
            uri.toString().startsWith("mailto:") -> {
                startActivity(Intent(Intent.ACTION_SENDTO, uri))
            }
            uri.toString().startsWith("tel:") -> {
                startActivity(Intent(Intent.ACTION_DIAL, uri))
            }
            else -> {
                view.loadUrl(uri.toString())
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progress.linearLayoutProgressBar.beVisible()
            webViewHelp.beInVisible()
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            progress.linearLayoutProgressBar.beGone()
            webViewHelp.beVisible()
            super.onPageCommitVisible(view, url)
        }
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request?.url?.let { proceedUrl(view, it) }
                }
            }
            return true
        }

    }
}


