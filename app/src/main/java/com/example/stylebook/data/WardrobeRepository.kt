package com.example.stylebook.data

import com.example.stylebook.data.local.ClothingItemEntity
import com.example.stylebook.data.local.SavedOutfitEntity
import com.example.stylebook.data.local.WearLogEntity
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingColor
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.Outfit
import com.example.stylebook.data.model.WearEntry
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/** Everything removed by [WardrobeRepository.deleteItem], kept so the deletion can be undone. */
data class DeletedItem(
    val item: ClothingItemEntity,
    val outfits: List<SavedOutfitEntity>,
    val wears: List<WearLogEntity>
)

/**
 * Single source of truth for wardrobe pieces, saved looks and the wear diary.
 * Every list is a [Flow] that emits again whenever the stored data changes.
 */
interface WardrobeRepository {
    /** Newest first. */
    val items: Flow<List<ClothingItem>>

    /** Newest first. */
    val savedOutfits: Flow<List<Outfit>>

    /** Most recent day first. */
    val wearLog: Flow<List<WearEntry>>

    suspend fun addItem(name: String, category: Category, color: ClothingColor, occasions: Set<Occasion>): ClothingItem

    /** Returns the updated piece, or null if it no longer exists. Saved looks using it are removed if it changes slot. */
    suspend fun updateItem(
        id: Int,
        name: String,
        category: Category,
        color: ClothingColor,
        occasions: Set<Occasion>
    ): ClothingItem?

    suspend fun toggleFavorite(itemId: Int)

    /** Removes a piece together with every saved look and diary entry that used it. */
    suspend fun deleteItem(itemId: Int): DeletedItem?

    suspend fun restore(deleted: DeletedItem)

    /** Saves [outfit] unless the same combination of pieces is already saved. Returns the stored copy or null. */
    suspend fun saveOutfit(outfit: Outfit): Outfit?

    suspend fun removeOutfit(outfitId: Int)

    /** Adds [outfit] to the diary for [date]. Returns false if exactly this look is already logged that day. */
    suspend fun logWear(outfit: Outfit, date: LocalDate): Boolean

    suspend fun deleteWearEntry(entryId: Int)
}
