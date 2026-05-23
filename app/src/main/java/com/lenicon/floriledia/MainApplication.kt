package com.lenicon.floriledia

import android.app.Application
import com.lenicon.floriledia.services.StorageService

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        StorageService.initialize(this)
    }
}