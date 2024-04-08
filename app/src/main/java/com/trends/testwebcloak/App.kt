package com.trends.testwebcloak

import android.app.Application
import com.trends.testwebcloak.di.Koin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(listOf(Koin.appModule))
            androidContext(this@App)
            androidLogger(Level.INFO)
        }
    }
}