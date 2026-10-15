package com.example.stylebook.data

import com.example.stylebook.R
import com.example.stylebook.data.model.ClothingItem

object SampleClothingData {
    val items = listOf(
        ClothingItem(1, "Linen Shirt", "Tops", "Ivory", R.drawable.garment_shirt),
        ClothingItem(2, "Straight Jeans", "Bottoms", "Indigo", R.drawable.garment_jeans),
        ClothingItem(3, "Everyday Trench", "Outerwear", "Sand", R.drawable.garment_coat),
        ClothingItem(4, "Soft Knit", "Knitwear", "Sage", R.drawable.garment_sweater)
    )
}
