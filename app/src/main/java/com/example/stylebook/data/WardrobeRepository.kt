package com.example.stylebook.data

import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingColor
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.Outfit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Everything removed by [WardrobeRepository.deleteItem], kept so the deletion can be undone. */
data class DeletedItem(val item: ClothingItem, val index: Int, val removedOutfits: List<Outfit>)

/**
 * Single source of truth for wardrobe pieces and saved outfits.
 *
 * Data lives in memory only: it survives configuration changes and screen navigation,
 * but is reset to the sample wardrobe when the app process is restarted.
 * Swapping the internals for Room/DataStore later will not change the public API.
 */
class WardrobeRepository(initialItems: List<ClothingItem> = SampleClothingData.items) {

    private val _items = MutableStateFlow(initialItems)
    val items: StateFlow<List<ClothingItem>> = _items.asStateFlow()

    private val _savedOutfits = MutableStateFlow<List<Outfit>>(emptyList())
    val savedOutfits: StateFlow<List<Outfit>> = _savedOutfits.asStateFlow()

    fun addItem(
        name: String,
        category: Category,
        color: ClothingColor,
        occasions: Set<Occasion>
    ): ClothingItem {
        lateinit var added: ClothingItem
        _items.update { current ->
            val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
            added = ClothingItem(nextId, name, category, color, occasions, category.defaultImageRes)
            listOf(added) + current
        }
        return added
    }

    fun toggleFavorite(itemId: Int) {
        _items.update { current ->
            current.map { if (it.id == itemId) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    /** Removes a piece and every saved outfit that used it. */
    fun deleteItem(itemId: Int): DeletedItem? {
        val index = _items.value.indexOfFirst { it.id == itemId }
        if (index < 0) return null
        val item = _items.value[index]
        val affectedOutfits = _savedOutfits.value.filter { it.contains(itemId) }
        _items.update { current -> current.filterNot { it.id == itemId } }
        _savedOutfits.update { current -> current.filterNot { it.contains(itemId) } }
        return DeletedItem(item, index, affectedOutfits)
    }

    fun restore(deleted: DeletedItem) {
        _items.update { current ->
            if (current.any { it.id == deleted.item.id }) current
            else current.toMutableList().apply { add(deleted.index.coerceAtMost(size), deleted.item) }
        }
        _savedOutfits.update { current ->
            (current + deleted.removedOutfits.filterNot { removed -> current.any { it.id == removed.id } })
                .sortedByDescending { it.id }
        }
    }

    /** Saves [outfit] unless the same combination of pieces is already saved. Returns the stored copy or null. */
    fun saveOutfit(outfit: Outfit): Outfit? {
        if (isSaved(outfit)) return null
        var saved: Outfit? = null
        _savedOutfits.update { current ->
            val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
            val stored = outfit.copy(id = nextId)
            saved = stored
            listOf(stored) + current
        }
        return saved
    }

    fun removeOutfit(outfitId: Int) {
        _savedOutfits.update { current -> current.filterNot { it.id == outfitId } }
    }

    fun isSaved(outfit: Outfit): Boolean = _savedOutfits.value.any { it.hasSamePiecesAs(outfit) }
}
