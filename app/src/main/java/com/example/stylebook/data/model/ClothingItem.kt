package com.example.stylebook.data.model

data class ClothingItem(
    val id: Int,
    val name: String,
    val category: String,
    val color: String,
    val imageRes: Int,
    val isFavorite: Boolean = false
)
