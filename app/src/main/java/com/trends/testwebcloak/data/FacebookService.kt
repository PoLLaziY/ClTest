package com.trends.testwebcloak.data

import android.content.Context
import com.facebook.FacebookSdk
import com.facebook.applinks.AppLinkData
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FacebookService {

    suspend fun getDeepLink(context: Context): String? {
        return suspendCoroutine { cont ->
            try {
                FacebookSdk.sdkInitialize(context)
                AppLinkData.fetchDeferredAppLinkData(context) {
                    onGetLink(cont, it)
                }
            } catch (e: Exception) {
                cont.resume(null)
            }
        }
    }

    private fun onGetLink(continuation: Continuation<String?>, link: AppLinkData?) {
        val result = link?.ref

        if (result.isNullOrEmpty()) {
            continuation.resume(null)
            return
        }

        continuation.resume(result)
    }
}