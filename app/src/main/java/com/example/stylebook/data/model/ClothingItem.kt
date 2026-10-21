package com.example.stylebook.data.model

import androidx.annotation.DrawableRes

data class ClothingItem(
    val id: Int,
    val name: String,
    val category: Category,
    val color: ClothingColor,
    val occasions: Set<Occasion>,
    @param:DrawableRes val imageRes: Int,
    val isFavorite: Boolean = false
)
