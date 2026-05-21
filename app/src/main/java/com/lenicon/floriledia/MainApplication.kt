package com.lenicon.floriledia

import android.app.Application
import com.lenicon.floriledia.services.StorageService

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize storage instantly across the entire app lifetime
        StorageService.initialize(this)
    }
}