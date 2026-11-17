package com.example.stylebook

import com.example.stylebook.data.DeletedItem
import com.example.stylebook.data.SampleClothingData
import com.example.stylebook.data.WardrobeRepository
import com.example.stylebook.data.local.ClothingItemEntity
import com.example.stylebook.data.local.SavedOutfitEntity
import com.example.stylebook.data.local.WearLogEntity
import com.example.stylebook.data.local.pieceIds
import com.example.stylebook.data.local.toEntity
import com.example.stylebook.data.local.toModel
import com.example.stylebook.data.local.toSavedEntity
import com.example.stylebook.data.local.toWearEntity
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingColor
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.Outfit
import com.example.stylebook.data.model.WearEntry
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [WardrobeRepository] with the same ordering and delete rules as the Room implementation. */
class FakeWardrobeRepository(initial: List<ClothingItem> = SampleClothingData.items) : WardrobeRepository {
    private val itemRows = MutableStateFlow(initial.map { it.toEntity() })
    private val outfitRows = MutableStateFlow<List<SavedOutfitEntity>>(emptyList())
    private val wearRows = MutableStateFlow<List<WearLogEntity>>(emptyList())
    private var clock = 2_000_000_000_000L

    override val items: Flow<List<ClothingItem>> = itemRows.map { rows ->
        rows.sortedWith(compareByDescending<ClothingItemEntity> { it.addedAt }.thenByDescending { it.id }).map { it.toModel() }
    }

    override val savedOutfits: Flow<List<Outfit>> = combine(items, outfitRows) { items, rows ->
        val byId = items.associateBy { it.id }
        rows.sortedByDescending { it.id }.mapNotNull { it.toModel(byId) }
    }

    override val wearLog: Flow<List<WearEntry>> = combine(items, wearRows) { items, rows ->
        val byId = items.associateBy { it.id }
        rows.sortedWith(compareByDescending<WearLogEntity> { it.wornOn }.thenByDescending { it.id })
            .mapNotNull { it.toModel(byId) }
    }

    val currentItems: List<ClothingItem> get() = itemRows.value.map { it.toModel() }

    override suspend fun addItem(name: String, category: Category, color: ClothingColor, occasions: Set<Occasion>): ClothingItem {
        val id = (itemRows.value.maxOfOrNull { it.id } ?: 0) + 1
        val item = ClothingItem(id, name, category, color, occasions, addedAt = clock++)
        itemRows.update { it + item.toEntity() }
        return item
    }

    override suspend fun updateItem(
        id: Int,
        name: String,
        category: Category,
        color: ClothingColor,
        occasions: Set<Occasion>
    ): ClothingItem? {
        val current = itemRows.value.firstOrNull { it.id == id } ?: return null
        val updated = current.copy(
            name = name,
            category = category,
            color = color,
            occasions = occasions,
            art = if (category == current.category) current.art else category.defaultArt
        )
        itemRows.update { rows -> rows.map { if (it.id == id) updated else it } }
        if (category.slot != current.category.slot) outfitRows.update { rows -> rows.filterNot { id in it.pieceIds } }
        return updated.toModel()
    }

    override suspend fun toggleFavorite(itemId: Int) {
        itemRows.update { rows -> rows.map { if (it.id == itemId) it.copy(isFavorite = !it.isFavorite) else it } }
    }

    override suspend fun deleteItem(itemId: Int): DeletedItem? {
        val item = itemRows.value.firstOrNull { it.id == itemId } ?: return null
        val deleted = DeletedItem(
            item,
            outfitRows.value.filter { itemId in it.pieceIds },
            wearRows.value.filter { itemId in it.pieceIds }
        )
        outfitRows.update { rows -> rows.filterNot { itemId in it.pieceIds } }
        wearRows.update { rows -> rows.filterNot { itemId in it.pieceIds } }
        itemRows.update { rows -> rows.filterNot { it.id == itemId } }
        return deleted
    }

    override suspend fun restore(deleted: DeletedItem) {
        if (itemRows.value.any { it.id == deleted.item.id }) return
        itemRows.update { it + deleted.item }
        outfitRows.update { it + deleted.outfits }
        wearRows.update { it + deleted.wears }
    }

    override suspend fun saveOutfit(outfit: Outfit): Outfit? {
        if (outfitRows.value.any { it.pieceIds == outfit.pieceIds }) return null
        val id = (outfitRows.value.maxOfOrNull { it.id } ?: 0) + 1
        outfitRows.update { it + outfit.toSavedEntity().copy(id = id) }
        return outfit.copy(id = id)
    }

    override suspend fun removeOutfit(outfitId: Int) {
        outfitRows.update { rows -> rows.filterNot { it.id == outfitId } }
    }

    override suspend fun logWear(outfit: Outfit, date: LocalDate): Boolean {
        if (wearRows.value.any { it.wornOn == date && it.pieceIds == outfit.pieceIds }) return false
        val id = (wearRows.value.maxOfOrNull { it.id } ?: 0) + 1
        wearRows.update { it + outfit.toWearEntity(date).copy(id = id) }
        return true
    }

    override suspend fun deleteWearEntry(entryId: Int) {
        wearRows.update { rows -> rows.filterNot { it.id == entryId } }
    }
}
