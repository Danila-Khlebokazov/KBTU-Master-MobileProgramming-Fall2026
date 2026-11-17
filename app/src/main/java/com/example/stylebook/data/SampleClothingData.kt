package com.example.stylebook.data

import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingColor
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.GarmentArt
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.Occasion.CASUAL
import com.example.stylebook.data.model.Occasion.EVENING
import com.example.stylebook.data.model.Occasion.WORK

/** Starter wardrobe written into the database the first time it is created. */
object SampleClothingData {
    // Older ids get older timestamps, so the sample list keeps this order (newest first).
    private const val SEEDED_AT = 1_767_225_600_000L

    val items: List<ClothingItem> = listOf(
        item(1, "Linen Shirt", Category.TOPS, ClothingColor.IVORY, setOf(CASUAL, WORK), GarmentArt.SHIRT, favorite = true),
        item(2, "Straight Jeans", Category.BOTTOMS, ClothingColor.DENIM, setOf(CASUAL), GarmentArt.JEANS),
        item(3, "Everyday Trench", Category.OUTERWEAR, ClothingColor.SAND, setOf(CASUAL, WORK), GarmentArt.COAT, favorite = true),
        item(4, "Soft Knit", Category.KNITWEAR, ClothingColor.SAGE, setOf(CASUAL, WORK), GarmentArt.SWEATER),
        item(5, "Leather Loafers", Category.SHOES, ClothingColor.BROWN, setOf(WORK, EVENING), GarmentArt.SHOES),
        item(6, "Tailored Trousers", Category.BOTTOMS, ClothingColor.CHARCOAL, setOf(WORK, EVENING), GarmentArt.TROUSERS),
        item(7, "Silk Blouse", Category.TOPS, ClothingColor.BLUSH, setOf(WORK, EVENING), GarmentArt.SHIRT),
        item(8, "Cotton Tee", Category.TOPS, ClothingColor.BLACK, setOf(CASUAL), GarmentArt.TEE),
        item(9, "Canvas Sneakers", Category.SHOES, ClothingColor.IVORY, setOf(CASUAL), GarmentArt.SHOES),
        item(10, "Wool Coat", Category.OUTERWEAR, ClothingColor.CAMEL, setOf(WORK, EVENING), GarmentArt.COAT),
        item(11, "Cashmere Crewneck", Category.KNITWEAR, ClothingColor.BURGUNDY, setOf(CASUAL, EVENING), GarmentArt.SWEATER),
        item(12, "Ankle Boots", Category.SHOES, ClothingColor.BLACK, setOf(CASUAL, EVENING), GarmentArt.SHOES),
        item(13, "Pleated Chinos", Category.BOTTOMS, ClothingColor.SAND, setOf(CASUAL, WORK), GarmentArt.TROUSERS)
    )

    private fun item(
        id: Int,
        name: String,
        category: Category,
        color: ClothingColor,
        occasions: Set<Occasion>,
        art: GarmentArt,
        favorite: Boolean = false
    ) = ClothingItem(id, name, category, color, occasions, art, favorite, addedAt = SEEDED_AT - id)
}
