package com.example.stylebook.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.stylebook.StyleBookApplication
import com.example.stylebook.data.WardrobeRepository

/** Creates every ViewModel with the app-wide repository from [StyleBookApplication.container]. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { WardrobeViewModel(repository()) }
        initializer { OutfitViewModel(repository()) }
        initializer { DiaryViewModel(repository()) }
    }

    private fun CreationExtras.repository(): WardrobeRepository =
        (this[APPLICATION_KEY] as StyleBookApplication).container.wardrobeRepository
}
