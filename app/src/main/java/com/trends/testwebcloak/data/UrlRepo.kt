package com.trends.testwebcloak.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UrlRepo(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    private val mLastUrl: MutableStateFlow<String?>
    val lastUrl: StateFlow<String?>

    init {
        val lastSavedUrl = prefs.getString(LAST_URL, "")
        mLastUrl = MutableStateFlow(if (lastSavedUrl.isNullOrEmpty()) null else lastSavedUrl)
        lastUrl = mLastUrl.asStateFlow()
    }

    fun save(url: String) {
        mLastUrl.value = url
        prefs.edit { putString(LAST_URL, url) }
    }

    private companion object {
        const val PREFS_KEY = "URL_REPO"
        const val LAST_URL = "LAST_URL"
    }
}