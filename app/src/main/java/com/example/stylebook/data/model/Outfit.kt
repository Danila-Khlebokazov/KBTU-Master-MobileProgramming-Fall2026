package com.example.stylebook.data.model

/**
 * A complete look built from existing wardrobe pieces.
 * [id] is 0 for a freshly generated outfit and becomes positive once it is saved.
 */
data class Outfit(
    val id: Int = 0,
    val top: ClothingItem,
    val bottom: ClothingItem,
    val shoes: ClothingItem,
    val outerwear: ClothingItem? = null,
    val occasion: Occasion? = null
) {
    /** Pieces ordered head to toe. */
    val pieces: List<ClothingItem>
        get() = listOfNotNull(outerwear, top, bottom, shoes)

    val pieceIds: Set<Int>
        get() = pieces.mapTo(mutableSetOf()) { it.id }

    fun pieceFor(slot: OutfitSlot): ClothingItem? = when (slot) {
        OutfitSlot.OUTERWEAR -> outerwear
        OutfitSlot.TOP -> top
        OutfitSlot.BOTTOM -> bottom
        OutfitSlot.SHOES -> shoes
    }

    fun contains(itemId: Int): Boolean = pieces.any { it.id == itemId }

    /** True when both outfits use exactly the same pieces (ids and order of slots are ignored). */
    fun hasSamePiecesAs(other: Outfit): Boolean = pieceIds == other.pieceIds
}
