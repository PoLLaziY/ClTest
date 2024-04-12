package com.trends.testwebcloak.data

import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GetUrlUseCase(
    private val urlRepo: UrlRepo,
    private val appsFlyers: AppsFlyerService,
    private val facebook: FacebookService,
    private val remoteConfig: FirebaseService
) {
    private val appsFlyersCheckerDelay = 10_000L
    private val placeHolder = "wp"

    private var delayJob: Job = Job()
    private var appsFlyersLoad: Job = Job()

    private var attributes: String? = null

    private var isOrganic = false

    private var rawUrl: String? = null


    suspend fun get(context: Context): String? {
        val lastUrl = urlRepo.lastUrl.value
        if (lastUrl != null) return lastUrl

        loadAttrs(context)
        loadRawUrl()

        return getUrl()
    }

    private suspend fun loadRawUrl() {
        rawUrl = remoteConfig.getRawLink(isOrganic)
    }

    private fun getUrl(): String? {
        return if (!isOrganic) rawUrl
        else createUrl(rawUrl, attributes)
    }

    private fun createUrl(rawUrl: String?, attrs: String?): String? {
        if (rawUrl == null || attrs == null) return null

        val attrsList = attrs.replace("app://", "").split("_")

        var index = 1
        var url: String = rawUrl

        while (url.contains("$placeHolder$index") && attrsList.lastIndex > index - 1) {
            url = url.replace("$placeHolder$index", attrsList[index - 1])
            index++
        }

        return url
    }

    private suspend fun loadAttrs(context: Context) {
        loadFromAppsFlyers(context)
        appsFlyersLoad.join()
        if (attributes == null) loadFacebookAttrs(context)
        isOrganic = attributes != null
    }

    private suspend fun loadFromAppsFlyers(context: Context) {
        coroutineScope {
            appsFlyersLoad = launch {
                attributes = appsFlyers.getInstallAttrs(context)
            }
            delayJob = launch { startChecker() }
        }
    }

    private suspend fun startChecker() {
        delay(appsFlyersCheckerDelay)
        if (!appsFlyersLoad.isCompleted) appsFlyersLoad.cancel()
    }

    private suspend fun loadFacebookAttrs(context: Context) {
        attributes = facebook.getDeepLink(context)
    }
}