package com.example.stylebook.data.local

import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.Outfit
import com.example.stylebook.data.model.WearEntry
import java.time.LocalDate

fun ClothingItemEntity.toModel() = ClothingItem(
    id = id,
    name = name,
    category = category,
    color = color,
    occasions = occasions,
    art = art,
    isFavorite = isFavorite,
    addedAt = addedAt
)

fun ClothingItem.toEntity() = ClothingItemEntity(
    id = id,
    name = name,
    category = category,
    color = color,
    occasions = occasions,
    art = art,
    isFavorite = isFavorite,
    addedAt = addedAt
)

/** Returns null if a piece is missing (it is being deleted in the same moment). */
fun SavedOutfitEntity.toModel(itemsById: Map<Int, ClothingItem>): Outfit? =
    outfitOf(id, topId, bottomId, shoesId, outerwearId, occasion, itemsById)

fun WearLogEntity.toModel(itemsById: Map<Int, ClothingItem>): WearEntry? =
    outfitOf(0, topId, bottomId, shoesId, outerwearId, occasion, itemsById)?.let { WearEntry(id, wornOn, it) }

fun Outfit.toSavedEntity() = SavedOutfitEntity(
    topId = top.id,
    bottomId = bottom.id,
    shoesId = shoes.id,
    outerwearId = outerwear?.id,
    occasion = occasion
)

fun Outfit.toWearEntity(date: LocalDate) = WearLogEntity(
    wornOn = date,
    topId = top.id,
    bottomId = bottom.id,
    shoesId = shoes.id,
    outerwearId = outerwear?.id,
    occasion = occasion
)

val SavedOutfitEntity.pieceIds: Set<Int> get() = setOfNotNull(topId, bottomId, shoesId, outerwearId)
val WearLogEntity.pieceIds: Set<Int> get() = setOfNotNull(topId, bottomId, shoesId, outerwearId)

private fun outfitOf(
    id: Int,
    topId: Int,
    bottomId: Int,
    shoesId: Int,
    outerwearId: Int?,
    occasion: Occasion?,
    itemsById: Map<Int, ClothingItem>
): Outfit? {
    val top = itemsById[topId] ?: return null
    val bottom = itemsById[bottomId] ?: return null
    val shoes = itemsById[shoesId] ?: return null
    val outerwear = outerwearId?.let { itemsById[it] ?: return null }
    return Outfit(id = id, top = top, bottom = bottom, shoes = shoes, outerwear = outerwear, occasion = occasion)
}
