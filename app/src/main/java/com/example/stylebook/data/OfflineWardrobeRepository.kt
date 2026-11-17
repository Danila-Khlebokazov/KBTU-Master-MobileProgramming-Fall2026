package com.example.stylebook.data

import androidx.room.withTransaction
import com.example.stylebook.data.local.StyleBookDatabase
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** [WardrobeRepository] backed by the Room database, so everything survives an app restart. */
class OfflineWardrobeRepository(
    private val db: StyleBookDatabase,
    private val now: () -> Long = System::currentTimeMillis
) : WardrobeRepository {

    private val itemDao = db.clothingItemDao()
    private val outfitDao = db.savedOutfitDao()
    private val wearDao = db.wearLogDao()

    override val items: Flow<List<ClothingItem>> =
        itemDao.observeAll().map { entities -> entities.map { it.toModel() } }

    override val savedOutfits: Flow<List<Outfit>> =
        combine(items, outfitDao.observeAll()) { items, outfits ->
            val byId = items.associateBy { it.id }
            outfits.mapNotNull { it.toModel(byId) }
        }

    override val wearLog: Flow<List<WearEntry>> =
        combine(items, wearDao.observeAll()) { items, entries ->
            val byId = items.associateBy { it.id }
            entries.mapNotNull { it.toModel(byId) }
        }

    override suspend fun addItem(
        name: String,
        category: Category,
        color: ClothingColor,
        occasions: Set<Occasion>
    ): ClothingItem {
        val item = ClothingItem(0, name, category, color, occasions, addedAt = now())
        val id = itemDao.insert(item.toEntity())
        return item.copy(id = id.toInt())
    }

    override suspend fun updateItem(
        id: Int,
        name: String,
        category: Category,
        color: ClothingColor,
        occasions: Set<Occasion>
    ): ClothingItem? = db.withTransaction {
        val current = itemDao.getById(id) ?: return@withTransaction null
        val updated = current.copy(
            name = name,
            category = category,
            color = color,
            occasions = occasions,
            art = if (category == current.category) current.art else category.defaultArt
        )
        itemDao.update(updated)
        // A saved look needs one piece per slot, so looks become invalid if the piece moved to another slot.
        if (category.slot != current.category.slot) outfitDao.deleteContaining(id)
        updated.toModel()
    }

    override suspend fun toggleFavorite(itemId: Int) = itemDao.toggleFavorite(itemId)

    override suspend fun deleteItem(itemId: Int): DeletedItem? = db.withTransaction {
        val item = itemDao.getById(itemId) ?: return@withTransaction null
        val deleted = DeletedItem(item, outfitDao.getContaining(itemId), wearDao.getContaining(itemId))
        // The foreign keys would cascade too; deleting explicitly keeps this independent of PRAGMA foreign_keys.
        outfitDao.deleteContaining(itemId)
        wearDao.deleteContaining(itemId)
        itemDao.deleteById(itemId)
        deleted
    }

    override suspend fun restore(deleted: DeletedItem) = db.withTransaction {
        if (itemDao.getById(deleted.item.id) != null) return@withTransaction
        itemDao.insert(deleted.item)
        outfitDao.insertAll(deleted.outfits)
        wearDao.insertAll(deleted.wears)
    }

    override suspend fun saveOutfit(outfit: Outfit): Outfit? = db.withTransaction {
        if (outfitDao.getAll().any { it.pieceIds == outfit.pieceIds }) return@withTransaction null
        val id = outfitDao.insert(outfit.toSavedEntity())
        outfit.copy(id = id.toInt())
    }

    override suspend fun removeOutfit(outfitId: Int) {
        outfitDao.deleteById(outfitId)
    }

    override suspend fun logWear(outfit: Outfit, date: LocalDate): Boolean = db.withTransaction {
        if (wearDao.getOn(date).any { it.pieceIds == outfit.pieceIds }) return@withTransaction false
        wearDao.insert(outfit.toWearEntity(date))
        true
    }

    override suspend fun deleteWearEntry(entryId: Int) {
        wearDao.deleteById(entryId)
    }
}
