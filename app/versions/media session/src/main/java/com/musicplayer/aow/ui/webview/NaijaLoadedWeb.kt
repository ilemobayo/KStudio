package com.musicplayer.aow.ui.webview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.Window
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.base.BaseActivity


/**
 * Created by Arca on 1/23/2018.
 */
class NaijaLoadedWeb: BaseActivity() {

    var webview: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Let's display the progress in the activity title bar, like the
        // browser app does.
        window.requestFeature(Window.FEATURE_PROGRESS)
        setContentView(R.layout.activity_web)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Naijaloaded"
        toolbar.subtitle = "web browser"
        toolbar.setTitleTextColor(resources.getColor(R.color.red_dim))
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_black)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        webview = findViewById<WebView>(R.id.webview)

        val intent = intent
        if (intent != null) {
            // To get the data use
            val data = intent.getStringExtra("address")
            if (data != null) {

                webview?.settings?.javaScriptEnabled = true
                webview?.settings!!.javaScriptCanOpenWindowsAutomatically = true
                webview?.settings!!.domStorageEnabled = true
                webview?.settings!!.pluginState = WebSettings.PluginState.ON
                webview?.settings!!.allowFileAccess = true

                webview?.setDownloadListener({ url, userAgent, contentDisposition, mimetype, contentLength ->
                    val uri = Uri.parse(url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                })


                //DCB test
                var builder : Uri.Builder = Uri.Builder()

                builder.scheme("https")
                        .authority("www.monapay.com")
                        .appendPath("v1")
                        .appendPath("merchant")
                        .appendPath("pay")
                        .appendQueryParameter("reference_id","MG1489414003ZUEZHOME")
                        .appendQueryParameter("merchant_id","1097")
                        .appendQueryParameter("product_key","9c326246ffdc612abba2778166b3ffc29e89e574")
                        .appendQueryParameter("uuid","456789876543456796gvbndcvbnmnbv")
                        .appendQueryParameter("amount","1000")
                        .appendQueryParameter("description","Buy MaliyoToken 5000 for 10 NGN")
                val paymentUrl = builder.build().toString()
                //webview?.loadUrl(paymentUrl)
                webview?.loadUrl(data)
                webview?.webViewClient = MyWebViewClient()
            }
        }
    }

    //webview go back to previous page
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action === KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (webview!!.canGoBack()) {
                        webview!!.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

//    override fun onBackPressed() {
//        if (webview!!.canGoBack()) {
//            webview!!.goBack()
//        } else {
//            super.onBackPressed()
//        }
//    }

    private inner class MyWebViewClient : WebViewClient() {
        //show the web page in webview but not in web browser
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, NaijaLoadedWeb::class.java)
            return intent
        }
    }
}