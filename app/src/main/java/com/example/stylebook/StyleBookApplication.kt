package com.example.stylebook

import android.app.Application
import com.example.stylebook.data.AppContainer

class StyleBookApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
