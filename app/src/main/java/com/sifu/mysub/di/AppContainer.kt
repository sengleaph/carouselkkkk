package com.sifu.mysub.di

import android.content.Context
import com.google.gson.Gson
import com.sifu.mysub.R
import com.sifu.mysub.core.util.AppError
import com.sifu.mysub.core.util.DefaultDispatcherProvider
import com.sifu.mysub.core.util.DispatcherProvider
import com.sifu.mysub.data.repository.SubscriptionRepositoryImpl
import com.sifu.mysub.data.repository.SuccessAuthRepositoryImpl
import com.sifu.mysub.data.repository.UpgradePlanRepositoryImpl
import com.sifu.mysub.data.source.RawResSubscriptionDataSource
import com.sifu.mysub.data.source.RawResSuccessAuthDataSource
import com.sifu.mysub.data.source.RawResUpgradePlanDataSource
import com.sifu.mysub.data.source.SubscriptionLocalDataSource
import com.sifu.mysub.data.source.SuccessAuthLocalDataSource
import com.sifu.mysub.data.source.UpgradePlanLocalDataSource
import com.sifu.mysub.domain.repository.SubscriptionRepository
import com.sifu.mysub.domain.repository.SuccessAuthRepository
import com.sifu.mysub.domain.repository.UpgradePlanRepository
import com.sifu.mysub.domain.usecase.GetSubscriptionUseCase
import com.sifu.mysub.domain.usecase.GetSuccessAuthUseCase
import com.sifu.mysub.domain.usecase.GetUpgradePlansUseCase
import com.sifu.mysub.domain.usecase.ResolveRowActionUseCase
import com.sifu.mysub.presentation.HomeViewModel
import com.sifu.mysub.presentation.subscription.ErrorMessageMapper
import com.sifu.mysub.presentation.subscription.SubscriptionViewModel
import com.sifu.mysub.presentation.upgrade.UpgradePlanViewModel

/**
 * Manual dependency container — the composition root. Every `new` in the app
 * happens here, which is why no other class needs to know a concrete type.
 *
 * Drop-in replaceable with Hilt later: the constructors are already injection-ready.
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    private val gson: Gson by lazy { Gson() }

    private val dispatchers: DispatcherProvider by lazy { DefaultDispatcherProvider() }

    private val localDataSource: SubscriptionLocalDataSource by lazy {
        RawResSubscriptionDataSource(appContext, gson)
    }

    private val subscriptionRepository: SubscriptionRepository by lazy {
        SubscriptionRepositoryImpl(localDataSource, dispatchers)
    }

    private val getSubscriptionUseCase by lazy {
        GetSubscriptionUseCase(subscriptionRepository)
    }

    private val resolveRowActionUseCase by lazy { ResolveRowActionUseCase() }

    private val upgradePlanDataSource: UpgradePlanLocalDataSource by lazy {
        RawResUpgradePlanDataSource(appContext, gson)
    }

    private val upgradePlanRepository: UpgradePlanRepository by lazy {
        UpgradePlanRepositoryImpl(upgradePlanDataSource, dispatchers)
    }

    private val getUpgradePlansUseCase by lazy {
        GetUpgradePlansUseCase(upgradePlanRepository)
    }

    private val errorMessageMapper = ErrorMessageMapper { error ->
        when (error) {
            is AppError.Business ->
                error.message ?: appContext.getString(R.string.error_generic)

            is AppError.Parsing -> appContext.getString(R.string.error_parsing)
            is AppError.NotFound -> appContext.getString(R.string.error_not_found)
            is AppError.Unknown -> appContext.getString(R.string.error_generic)
        }
    }

    private val successAuthDataSource: SuccessAuthLocalDataSource by lazy {
        RawResSuccessAuthDataSource(appContext, gson)
    }

    private val successAuthRepository: SuccessAuthRepository by lazy {
        SuccessAuthRepositoryImpl(successAuthDataSource, dispatchers)
    }

    private val getSuccessAuthUseCase by lazy {
        GetSuccessAuthUseCase(successAuthRepository)
    }

    fun homeViewModelFactory() = HomeViewModel.Factory(
        getUpgradePlans = getUpgradePlansUseCase,
        getSubscription = getSubscriptionUseCase,
        errorMessages = errorMessageMapper
    )

    fun subscriptionViewModelFactory() = SubscriptionViewModel.Factory(
        getSubscription = getSubscriptionUseCase,
        resolveRowAction = resolveRowActionUseCase,
        getSuccessAuth = getSuccessAuthUseCase,
        errorMessages = errorMessageMapper
    )

    fun upgradePlanViewModelFactory() = UpgradePlanViewModel.Factory(
        getUpgradePlans = getUpgradePlansUseCase,
        errorMessages = errorMessageMapper
    )
}
