package com.trends.testwebcloak.data

import android.content.Context
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.trends.testwebcloak.BuildConfig
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AppsFlyerService {

    suspend fun getInstallAttrs(context: Context): String? {
        return suspendCoroutine { cont ->
            val errorListener = ErrorListener { cont.resume(null) }
            val attrsRequestListener = AttrsRequestListener { map ->
                cont.resume(getCompanyName(map))
            }
            AppsFlyerLib.getInstance().init(BuildConfig.AF_DEV_KEY, attrsRequestListener, context)
            AppsFlyerLib.getInstance().start(context, BuildConfig.AF_DEV_KEY, errorListener)
        }
    }

    private fun getCompanyName(from: Map<String, String?>?): String? {
        if (from == null) return null
        val result = from[COMPANY_KEY] ?: return null
        if (result.isEmpty()) return null
        return result
    }

    private class ErrorListener(private val onError: (String) -> Unit) : AppsFlyerRequestListener {
        override fun onSuccess() {}

        override fun onError(p0: Int, p1: String) {
            onError(p1)
        }
    }

    private class AttrsRequestListener(
        private val onAppOpenAttrs: (Map<String, String?>?) -> Unit
    ) : AppsFlyerConversionListener {
        override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {}

        override fun onConversionDataFail(p0: String?) {}

        override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
            onAppOpenAttrs(p0)
        }

        override fun onAttributionFailure(p0: String?) {
            onAppOpenAttrs(null)
        }
    }

    private companion object {
        const val COMPANY_KEY = "company_name"
    }
}
