package com.example.stylebook.data.model

import androidx.annotation.DrawableRes

data class ClothingItem(
    val id: Int,
    val name: String,
    val category: Category,
    val color: ClothingColor,
    val occasions: Set<Occasion>,
    val art: GarmentArt = category.defaultArt,
    val isFavorite: Boolean = false,
    /** Epoch millis; the wardrobe is listed newest first. */
    val addedAt: Long = 0
) {
    @get:DrawableRes
    val imageRes: Int get() = art.res
}
