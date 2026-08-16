package com.sifu.mysub

import android.app.Application
import com.sifu.mysub.di.AppContainer

class MySubApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
