package com.example.stylebook.data

import com.example.stylebook.R
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingColor
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.Occasion.CASUAL
import com.example.stylebook.data.model.Occasion.EVENING
import com.example.stylebook.data.model.Occasion.WORK

/** Starter wardrobe so every feature can be demonstrated on first launch (kept in memory only). */
object SampleClothingData {
    val items: List<ClothingItem> = listOf(
        item(1, "Linen Shirt", Category.TOPS, ClothingColor.IVORY, setOf(CASUAL, WORK), R.drawable.garment_shirt, favorite = true),
        item(2, "Straight Jeans", Category.BOTTOMS, ClothingColor.DENIM, setOf(CASUAL), R.drawable.garment_jeans),
        item(3, "Everyday Trench", Category.OUTERWEAR, ClothingColor.SAND, setOf(CASUAL, WORK), R.drawable.garment_coat, favorite = true),
        item(4, "Soft Knit", Category.KNITWEAR, ClothingColor.SAGE, setOf(CASUAL, WORK), R.drawable.garment_sweater),
        item(5, "Leather Loafers", Category.SHOES, ClothingColor.BROWN, setOf(WORK, EVENING), R.drawable.garment_shoes),
        item(6, "Tailored Trousers", Category.BOTTOMS, ClothingColor.CHARCOAL, setOf(WORK, EVENING), R.drawable.garment_trousers),
        item(7, "Silk Blouse", Category.TOPS, ClothingColor.BLUSH, setOf(WORK, EVENING), R.drawable.garment_shirt),
        item(8, "Cotton Tee", Category.TOPS, ClothingColor.BLACK, setOf(CASUAL), R.drawable.garment_tee),
        item(9, "Canvas Sneakers", Category.SHOES, ClothingColor.IVORY, setOf(CASUAL), R.drawable.garment_shoes),
        item(10, "Wool Coat", Category.OUTERWEAR, ClothingColor.CAMEL, setOf(WORK, EVENING), R.drawable.garment_coat),
        item(11, "Cashmere Crewneck", Category.KNITWEAR, ClothingColor.BURGUNDY, setOf(CASUAL, EVENING), R.drawable.garment_sweater),
        item(12, "Ankle Boots", Category.SHOES, ClothingColor.BLACK, setOf(CASUAL, EVENING), R.drawable.garment_shoes),
        item(13, "Pleated Chinos", Category.BOTTOMS, ClothingColor.SAND, setOf(CASUAL, WORK), R.drawable.garment_trousers)
    )

    private fun item(
        id: Int,
        name: String,
        category: Category,
        color: ClothingColor,
        occasions: Set<Occasion>,
        imageRes: Int,
        favorite: Boolean = false
    ) = ClothingItem(id, name, category, color, occasions, imageRes, favorite)
}
