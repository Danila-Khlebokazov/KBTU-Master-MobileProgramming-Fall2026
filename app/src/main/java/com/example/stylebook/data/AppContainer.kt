package com.example.stylebook.data

import android.content.Context
import com.example.stylebook.data.local.StyleBookDatabase

/**
 * Minimal manual dependency container, created once by [com.example.stylebook.StyleBookApplication].
 * Every ViewModel receives the same repository, so a change made on one screen is visible on all others.
 */
class AppContainer(context: Context) {
    val wardrobeRepository: WardrobeRepository by lazy {
        OfflineWardrobeRepository(StyleBookDatabase.getInstance(context))
    }
}
