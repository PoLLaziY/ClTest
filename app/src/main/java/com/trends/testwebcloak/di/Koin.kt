package com.trends.testwebcloak.di

import com.trends.testwebcloak.data.AppsFlyerService
import com.trends.testwebcloak.data.FacebookService
import com.trends.testwebcloak.data.FirebaseService
import com.trends.testwebcloak.data.GetUrlUseCase
import com.trends.testwebcloak.data.UrlRepo
import com.trends.testwebcloak.models.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object Koin {
    val appModule = module {
        factory {
            AppsFlyerService()
        }

        factory {
            GetUrlUseCase(get(), get(), get(), get())
        }

        single {
            UrlRepo(get())
        }

        factory { FacebookService() }

        factory { FirebaseService() }

        viewModel { SplashViewModel(get(), get()) }
    }
}