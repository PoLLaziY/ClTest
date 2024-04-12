package com.trends.testwebcloak.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trends.testwebcloak.data.GetUrlUseCase
import com.trends.testwebcloak.data.UrlRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val getUrlCase: GetUrlUseCase,
    private val urlRepo: UrlRepo
): ViewModel() {

    private var started = false

    private val mUrl = MutableSharedFlow<String?>(replay = 1, extraBufferCapacity = 1)
    val url = mUrl.asSharedFlow()

    private val mNavigateAllowed = MutableStateFlow(false)
    val navigateAllowed = mNavigateAllowed.asStateFlow()

    private val mProgress = MutableStateFlow(0)
    val progress = mProgress.asStateFlow()

    fun startLoad(context: Context) {
        if (started) return
        started = true

        startProgress()
        startUrlLoad(context)
    }

    private fun startProgress() {
        val interval = 10000L/100
        viewModelScope.launch {
            var progress = 0
            while (progress++ < 100) {
                mProgress.value = progress
                delay(interval)
            }
        }
    }

    private fun startUrlLoad(context: Context) {
        viewModelScope.launch {
            //val url = getUrlCase.get(context)
            val url = "http://fposttestb.xyz/testing.html"
            mUrl.emit(url)
        }
    }

    fun saveUrl(url: String?) {
        if (url == null) return
        urlRepo.save(url)
    }

    fun allowNavigateToNext() {
        mNavigateAllowed.value = true
    }
}