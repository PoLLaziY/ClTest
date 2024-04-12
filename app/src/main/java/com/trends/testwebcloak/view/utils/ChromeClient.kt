package com.trends.testwebcloak.view.utils

import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ChromeClient : WebChromeClient() {

    private var callback: ValueCallback<Array<Uri>>? = null
    private val mProgress = MutableStateFlow(0)
    private val mFilesRequest = MutableSharedFlow<Intent?>(0, 1)
    val filesRequest = mFilesRequest.asSharedFlow()
    val progress = mProgress.asStateFlow()

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        mFilesRequest.tryEmit(fileChooserParams?.createIntent())
        callback = filePathCallback
        return true
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        mProgress.value = newProgress
        super.onProgressChanged(view, newProgress)
    }

    fun returnFiles(list: List<Uri>) {
        callback?.onReceiveValue(list.toTypedArray())
    }
}