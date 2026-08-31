package com.sifu.mysub.di

import android.content.Context
import com.google.gson.Gson
import com.sifu.mysub.R
import com.sifu.mysub.core.util.AppError
import com.sifu.mysub.core.util.DefaultDispatcherProvider
import com.sifu.mysub.core.util.DispatcherProvider
import com.sifu.mysub.data.repository.SubscribeMenuRepositoryImpl
import com.sifu.mysub.data.repository.SubscriptionAuthRepositoryImpl
import com.sifu.mysub.data.repository.SubscriptionRepositoryImpl
import com.sifu.mysub.data.source.RawResSubscribeMenuDataSource
import com.sifu.mysub.data.source.DataAuthLocalDataSource
import com.sifu.mysub.data.source.RawResDataAuthDataSource
import com.sifu.mysub.data.source.RawResSubscriptionDataSource
import com.sifu.mysub.data.source.SubscribeMenuLocalDataSource
import com.sifu.mysub.data.source.SubscriptionLocalDataSource
import com.sifu.mysub.domain.repository.SubscribeMenuRepository
import com.sifu.mysub.domain.repository.SubscriptionAuthRepository
import com.sifu.mysub.domain.repository.SubscriptionRepository
import com.sifu.mysub.domain.usecase.GetSubscribeMenuUseCase
import com.sifu.mysub.domain.usecase.GetSubscriptionAuthUseCase
import com.sifu.mysub.domain.usecase.GetSubscriptionUseCase
import com.sifu.mysub.presentation.detail.SubscriptionDetailViewModel
import com.sifu.mysub.presentation.main.ErrorMessageMapper
import com.sifu.mysub.presentation.main.MainViewModel
import com.sifu.mysub.presentation.main.TitleProvider
import com.sifu.mysub.presentation.service.ServiceViewModel

/**
 * Manual dependency container — the composition root. Every construction happens
 * here, which is why no other class needs to know a concrete type.
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    private val gson: Gson by lazy { Gson() }

    private val dispatchers: DispatcherProvider by lazy { DefaultDispatcherProvider() }

    // ---------------------------------------------------------------- menu

    private val menuDataSource: SubscribeMenuLocalDataSource by lazy {
        RawResSubscribeMenuDataSource(appContext, gson)
    }

    private val menuRepository: SubscribeMenuRepository by lazy {
        SubscribeMenuRepositoryImpl(menuDataSource, dispatchers)
    }

    private val getSubscribeMenuUseCase by lazy {
        GetSubscribeMenuUseCase(menuRepository)
    }

    // -------------------------------------------------------- subscription

    private val subscriptionDataSource: SubscriptionLocalDataSource by lazy {
        RawResSubscriptionDataSource(appContext, gson)
    }

    private val subscriptionRepository: SubscriptionRepository by lazy {
        SubscriptionRepositoryImpl(subscriptionDataSource, dispatchers)
    }

    private val getSubscriptionUseCase by lazy {
        GetSubscriptionUseCase(subscriptionRepository)
    }

    private val dataAuthDataSource: DataAuthLocalDataSource by lazy {
        RawResDataAuthDataSource(appContext, gson)
    }

    private val subscriptionAuthRepository: SubscriptionAuthRepository by lazy {
        SubscriptionAuthRepositoryImpl(dataAuthDataSource, dispatchers)
    }

    private val getSubscriptionAuthUseCase by lazy {
        GetSubscriptionAuthUseCase(subscriptionAuthRepository)
    }

    // --------------------------------------------------------- presentation

    private val errorMessageMapper = ErrorMessageMapper { error ->
        when (error) {
            is AppError.Business ->
                error.message ?: appContext.getString(R.string.error_generic)

            is AppError.Parsing -> appContext.getString(R.string.error_parsing)
            is AppError.NotFound -> appContext.getString(R.string.error_not_found)
            is AppError.Unknown -> appContext.getString(R.string.error_generic)
        }
    }

    /** Keeps `R` out of the ViewModel, which stays a plain JVM class. */
    private val titleProvider = object : TitleProvider {
        override fun category(): String = appContext.getString(R.string.title_category)
    }

    fun serviceViewModelFactory(serviceCode: String) = ServiceViewModel.Factory(
        serviceCode = serviceCode,
        getSubscribeMenu = getSubscribeMenuUseCase,
        getSubscription = getSubscriptionUseCase,
        errorMessages = errorMessageMapper,
        titles = titleProvider
    )

    fun subscriptionDetailViewModelFactory() = SubscriptionDetailViewModel.Factory(
        getSubscription = getSubscriptionUseCase,
        getSubscriptionAuth = getSubscriptionAuthUseCase,
        errorMessages = errorMessageMapper
    )

    fun mainViewModelFactory() = MainViewModel.Factory(
        getSubscribeMenu = getSubscribeMenuUseCase,
        errorMessages = errorMessageMapper,
        titles = titleProvider
    )
}
