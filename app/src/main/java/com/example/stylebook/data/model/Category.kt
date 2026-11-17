package com.example.stylebook.data.model

/** Clothing categories. Each one fills exactly one [OutfitSlot] when an outfit is generated. */
enum class Category(val displayName: String, val slot: OutfitSlot, val defaultArt: GarmentArt) {
    TOPS("Tops", OutfitSlot.TOP, GarmentArt.SHIRT),
    KNITWEAR("Knitwear", OutfitSlot.TOP, GarmentArt.SWEATER),
    BOTTOMS("Bottoms", OutfitSlot.BOTTOM, GarmentArt.TROUSERS),
    OUTERWEAR("Outerwear", OutfitSlot.OUTERWEAR, GarmentArt.COAT),
    SHOES("Shoes", OutfitSlot.SHOES, GarmentArt.SHOES)
}

/** The positions of an outfit, listed head to toe. */
enum class OutfitSlot(val displayName: String) {
    OUTERWEAR("Layer"),
    TOP("Top"),
    BOTTOM("Bottom"),
    SHOES("Shoes")
}
