package com.example.vesit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NexusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize things like Timber for logging or PaySetu configurations here
    }
}