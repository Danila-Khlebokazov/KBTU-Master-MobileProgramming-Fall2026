package com.example.stylebook.data

/**
 * Minimal manual dependency container. Both ViewModels receive the same repository,
 * so a change made on one screen (e.g. deleting a piece) is instantly visible on every other screen.
 */
object AppContainer {
    val wardrobeRepository: WardrobeRepository by lazy { WardrobeRepository() }
}
