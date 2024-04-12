package com.trends.testwebcloak.view

import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.FrameLayout.LayoutParams
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import com.trends.testwebcloak.databinding.FragmentSplashBinding
import com.trends.testwebcloak.models.SplashViewModel
import com.trends.testwebcloak.view.utils.BindingFragment
import com.trends.testwebcloak.view.utils.ChromeClient
import com.trends.testwebcloak.view.utils.ChromeClientFilesSelector
import com.trends.testwebcloak.view.utils.LoadState
import com.trends.testwebcloak.view.utils.WebViewClient
import org.koin.android.ext.android.inject


class SplashFragment : BindingFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    private val viewModel: SplashViewModel by inject()

    private val chromeClient by lazy { ChromeClient() }
    private val webClient by lazy { WebViewClient() }

    private var webView: WebView? = null

    private val fileSelectorContract = ChromeClientFilesSelector()

    private val fileSelector = registerForActivityResult(fileSelectorContract) {
        if (it == null) showError() else chromeClient.returnFiles(it)
    }

    private var lastBackPress = 0L

    override fun afterInflated() {

        //For Tests
        //startWeb("http://fposttestb.xyz/testing.html")

        viewModel.startLoad(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback {
            onBackClick()
        }

        collectOnCreated(chromeClient.filesRequest) {
            if (it != null) fileSelector.launch(it)
        }

        collectOnStarted(webClient.loadState) {
            onLoadState(it)
        }

        collectOnStarted(chromeClient.progress) {
            binding.progress.isVisible = it == 100 || it == 0
            binding.progress.progress = it
        }

        collectOnStarted(viewModel.progress) {
            binding.progress.progress = it
        }

        collectOnStarted(viewModel.navigateAllowed) {
            if (it) {
                webView?.isVisible = false
                binding.progress.isVisible = false
                binding.text.isVisible = true
            }
        }

        collectOnStarted(webClient.intentFlow) {
            actionView(it)
        }


        collectOnStarted(viewModel.url) {
            if (it == null) {
                viewModel.allowNavigateToNext()
            } else {
                startWeb(it)
            }
        }
    }

    override fun beforeDestroy() {
        webView = null
    }

    private fun onLoadState(state: LoadState) {
        when (state) {
            is LoadState.Success -> viewModel.saveUrl(state.url)
            is LoadState.Error -> {
                binding.root.setOnClickListener { webView?.reload() }
            }

            else -> {
                binding.root.setOnClickListener { }
            }
        }
    }

    private fun startWeb(url: String) {
        if (webView != null) return
        val params = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        binding.progress.isVisible = false

        webView = WebView(requireContext())
        binding.container.removeAllViews()
        binding.container.addView(webView)

        webView?.apply {
            layoutParams = params
            loadUrl(url)

            webViewClient = webClient
            webChromeClient = chromeClient
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowContentAccess = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.allowFileAccess = true
            settings.javaScriptCanOpenWindowsAutomatically = true

            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(webView, true)
            }

            setDownloadListener { url, _, _, _, _ ->
                downloadFile(url)
            }
        }

        binding.container.isVisible = true
    }

    private fun downloadFile(url: String?) {
        if (url == null) return
        val i = Intent(Intent.ACTION_VIEW)
        i.setData(Uri.parse(url))
        startActivity(i)
    }

    private fun showError() {
        Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
    }

    private fun onBackClick() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            val time = System.currentTimeMillis()
            if (time - lastBackPress > 2300) {
                Toast.makeText(requireContext(), "Press back again to finish", Toast.LENGTH_SHORT)
                    .show()
                lastBackPress = System.currentTimeMillis()
            } else {
                requireActivity().finish()
            }
        }
    }
}