package com.trends.testwebcloak.view

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoadState {
    data object NotStarted : LoadState()
    class Success(val url: String) : LoadState()
    data object Error : LoadState()
}

const val EMPTY_BODY = "\"\""
const val JS_BODY_REQUEST = "window.variable = document.body.innerText;"

class WebViewClient(checkRedirect: Boolean = false) : WebViewClient() {

    private val store = UrlStore(checkRedirect)
    private val scope = CoroutineScope(Dispatchers.Main)

    val loadState = store.loadState

    private val mIntentFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val intentFlow = mIntentFlow.asSharedFlow()

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        store.addUrl(url)
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        view?.evaluateJavascript(JS_BODY_REQUEST) {
            val success = it != EMPTY_BODY
            setupToStoreDelayed(view, url, success, 4000)
        }
        super.onPageFinished(view, url)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        val url = request?.url.toString()
        setupToStoreDelayed(view, url, false, 2000)
        super.onReceivedError(view, request, error)
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString()
        store.addUrl(url)
        if (url?.startsWith("http") == false) {
            mIntentFlow.tryEmit(url)
            return true
        }
        return false
    }

    private fun setupToStoreDelayed(view: WebView?, url: String?, success: Boolean, delay: Long) {
        if (!store.actual()) return
        scope.launch {
            delay(delay)
            if (view?.url == url) {
                store.addResult(url, success)
            } else {
                store.addUrl(view?.url)
                view?.evaluateJavascript(JS_BODY_REQUEST) {
                    store.addResult(view.url, it != EMPTY_BODY)
                }
            }
        }
    }
}

class UrlStore(private val checkRedirect: Boolean) {
    private var previous: String? = null
    private var actual: String? = null
    private val mState = MutableStateFlow<LoadState>(LoadState.NotStarted)

    val loadState = mState.asStateFlow()
    fun addUrl(url: String?) {
        if (!actual()) return
        if (url == null) return
        if (url == actual) return
        previous = actual
        actual = url
    }

    fun addResult(url: String?, success: Boolean) {
        if (!actual()) return
        if (url == null) return
        if (url != actual) return
        val result = if (!checkRedirect) success else success && previous != null
        mState.value = getLoadState(result, url)
    }

    fun actual(): Boolean =
        mState.value !is LoadState.Success && mState.value != LoadState.Error

    private fun getLoadState(success: Boolean, url: String): LoadState =
        if (success) LoadState.Success(url) else LoadState.Error
}
