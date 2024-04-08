package com.trends.testwebcloak.view

import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.FrameLayout.LayoutParams
import android.widget.FrameLayout.LayoutParams.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import com.trends.testwebcloak.BindingFragment
import com.trends.testwebcloak.databinding.FragmentSplashBinding
import com.trends.testwebcloak.models.SplashViewModel
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

        collectOnStarted(chromeClient.filesRequest) {
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
            Log.e("VVV", "URL == $it")
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
            is LoadState.Error -> { binding.root.setOnClickListener { webView?.reload() } }
            else -> { binding.root.setOnClickListener {  } }
        }
    }

    private fun startWeb(url: String) {
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
        }

        binding.container.isVisible = true
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
                Toast.makeText(requireContext(), "Press back again to finish", Toast.LENGTH_SHORT).show()
                lastBackPress = System.currentTimeMillis()
            } else {
                requireActivity().finish()
            }
        }
    }
}