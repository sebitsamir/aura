package com.aura.app

import android.app.Application
import com.aura.core.common.util.Logger
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AuraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Logger.i("AuraApp", "AURA application initialized")
    }
}
