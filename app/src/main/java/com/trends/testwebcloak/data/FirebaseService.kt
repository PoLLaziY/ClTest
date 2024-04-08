package com.trends.testwebcloak.data

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseService {

    private val config = Firebase.remoteConfig

    suspend fun getRawLink(isOrganic: Boolean): String? {
        if (!resetConfig()) return null
        if (!fetchConfig()) return null
        return getUrl(isOrganic)
    }

    private suspend fun resetConfig(): Boolean {
        return suspendCoroutine { cont ->
            config.reset().addOnCompleteListener {
                cont.resume(it.isSuccessful)
            }
        }
    }

    private suspend fun fetchConfig(): Boolean {
        return suspendCoroutine { cont ->
            config.fetchAndActivate().addOnCompleteListener {
                cont.resume(it.result ?: it.isSuccessful)
            }
        }
    }

    private fun getUrl(isOrganic: Boolean): String? {
        val key = if (isOrganic) ORGANIC_URL_KEY else INORGANIC_URL_KEY
        val result = config.getString(key)
        if (result.isEmpty()) return null
        return result
    }

    private companion object {
        const val ORGANIC_URL_KEY = "first_url"
        const val INORGANIC_URL_KEY = "second_url"
    }
}