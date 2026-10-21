package com.example.stylebook.data.model

import androidx.annotation.DrawableRes
import com.example.stylebook.R

/** Clothing categories. Each one fills exactly one [OutfitSlot] when an outfit is generated. */
enum class Category(val displayName: String, val slot: OutfitSlot, @param:DrawableRes val defaultImageRes: Int) {
    TOPS("Tops", OutfitSlot.TOP, R.drawable.garment_shirt),
    KNITWEAR("Knitwear", OutfitSlot.TOP, R.drawable.garment_sweater),
    BOTTOMS("Bottoms", OutfitSlot.BOTTOM, R.drawable.garment_trousers),
    OUTERWEAR("Outerwear", OutfitSlot.OUTERWEAR, R.drawable.garment_coat),
    SHOES("Shoes", OutfitSlot.SHOES, R.drawable.garment_shoes)
}

/** The positions of an outfit, listed head to toe. */
enum class OutfitSlot(val displayName: String) {
    OUTERWEAR("Layer"),
    TOP("Top"),
    BOTTOM("Bottom"),
    SHOES("Shoes")
}
