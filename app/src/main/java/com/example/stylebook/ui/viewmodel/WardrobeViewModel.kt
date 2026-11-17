package com.example.stylebook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylebook.data.DeletedItem
import com.example.stylebook.data.WardrobeRepository
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.domain.ClothingItemValidator
import com.example.stylebook.domain.ItemForm
import com.example.stylebook.domain.ItemFormErrors
import com.example.stylebook.domain.WearStats
import com.example.stylebook.domain.wearStatsByItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WardrobeSort(val label: String) {
    NEWEST("Newest"),
    NAME("A\u2013Z"),
    MOST_WORN("Most worn"),
    LEAST_WORN("Least worn")
}

data class WardrobeFilters(
    val query: String = "",
    val category: Category? = null,
    val favoritesOnly: Boolean = false,
    val sort: WardrobeSort = WardrobeSort.NEWEST
)

data class WardrobeUiState(
    /** True until the database has delivered its first result. */
    val isLoading: Boolean = true,
    val allItems: List<ClothingItem> = emptyList(),
    val visibleItems: List<ClothingItem> = emptyList(),
    val filters: WardrobeFilters = WardrobeFilters(),
    val wearStats: Map<Int, WearStats> = emptyMap()
) {
    val favoriteItems: List<ClothingItem> get() = allItems.filter { it.isFavorite }
    val categoryCounts: Map<Category, Int> get() = allItems.groupingBy { it.category }.eachCount()
    val hasActiveFilters: Boolean
        get() = filters.query.isNotBlank() || filters.category != null || filters.favoritesOnly

    fun itemById(id: Int): ClothingItem? = allItems.firstOrNull { it.id == id }

    fun wearStatsFor(itemId: Int): WearStats = wearStats[itemId] ?: WearStats()
}

sealed interface SaveItemResult {
    data object Saved : SaveItemResult
    data class Invalid(val errors: ItemFormErrors) : SaveItemResult
}

/** Wardrobe pieces: search/filter/sort state, creating, editing, favoriting and deleting with undo. */
class WardrobeViewModel(private val repository: WardrobeRepository) : ViewModel() {

    private val filters = MutableStateFlow(WardrobeFilters())

    val uiState: StateFlow<WardrobeUiState> =
        combine(repository.items, repository.wearLog, filters) { items, log, filters ->
            buildState(items, log.wearStatsByItem(), filters)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WardrobeUiState()
        )

    private val _messages = Channel<UserMessage>(Channel.BUFFERED)
    val messages: Flow<UserMessage> = _messages.receiveAsFlow()

    private var lastDeleted: DeletedItem? = null

    fun onQueryChange(query: String) = filters.update { it.copy(query = query) }

    fun onCategorySelected(category: Category?) = filters.update { it.copy(category = category) }

    fun onFavoritesOnlyChange(enabled: Boolean) = filters.update { it.copy(favoritesOnly = enabled) }

    fun onSortSelected(sort: WardrobeSort) = filters.update { it.copy(sort = sort) }

    fun clearFilters() = filters.update { WardrobeFilters(sort = it.sort) }

    /** Used by the Home screen category shortcuts: show exactly one category. */
    fun showCategory(category: Category) = filters.update { WardrobeFilters(category = category, sort = it.sort) }

    fun toggleFavorite(itemId: Int) {
        viewModelScope.launch { repository.toggleFavorite(itemId) }
    }

    fun validate(form: ItemForm, editingId: Int? = null): ItemFormErrors =
        ClothingItemValidator.validate(form, uiState.value.allItems, editingId)

    /** Creates a new piece, or updates the piece with [editingId]. */
    fun saveItem(form: ItemForm, editingId: Int? = null): SaveItemResult {
        val errors = validate(form, editingId)
        if (errors.hasErrors) return SaveItemResult.Invalid(errors)
        val name = form.name.trim()
        val category = requireNotNull(form.category)
        val color = requireNotNull(form.color)
        viewModelScope.launch {
            if (editingId == null) {
                val item = repository.addItem(name, category, color, form.occasions)
                send(UserMessage("${item.name} added to your wardrobe"))
            } else {
                val updated = repository.updateItem(editingId, name, category, color, form.occasions)
                send(UserMessage(if (updated != null) "${updated.name} updated" else "This piece no longer exists"))
            }
        }
        return SaveItemResult.Saved
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            val deleted = repository.deleteItem(itemId) ?: return@launch
            lastDeleted = deleted
            val extras = listOfNotNull(
                countLabel(deleted.outfits.size, "saved look", "saved looks"),
                countLabel(deleted.wears.size, "diary entry", "diary entries")
            )
            val note = if (extras.isEmpty()) "" else " and " + extras.joinToString(" and ")
            send(UserMessage("Removed ${deleted.item.name}$note", actionLabel = "Undo", onAction = ::undoDelete))
        }
    }

    fun undoDelete() {
        val deleted = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch { repository.restore(deleted) }
    }

    private fun send(message: UserMessage) {
        viewModelScope.launch { _messages.send(message) }
    }

    private fun countLabel(count: Int, one: String, many: String): String? = when (count) {
        0 -> null
        1 -> "1 $one"
        else -> "$count $many"
    }

    private fun buildState(
        items: List<ClothingItem>,
        wearStats: Map<Int, WearStats>,
        filters: WardrobeFilters
    ): WardrobeUiState {
        val query = filters.query.trim()
        val matching = items.filter { item ->
            val matchesQuery = query.isEmpty() ||
                item.name.contains(query, ignoreCase = true) ||
                item.category.displayName.contains(query, ignoreCase = true) ||
                item.color.displayName.contains(query, ignoreCase = true) ||
                item.occasions.any { it.displayName.contains(query, ignoreCase = true) }
            val matchesCategory = filters.category == null || item.category == filters.category
            matchesQuery && matchesCategory && (!filters.favoritesOnly || item.isFavorite)
        }
        fun stats(item: ClothingItem) = wearStats[item.id] ?: WearStats()
        val visible = when (filters.sort) {
            WardrobeSort.NEWEST -> matching
            WardrobeSort.NAME -> matching.sortedBy { it.name.lowercase() }
            WardrobeSort.MOST_WORN -> matching.sortedWith(
                compareByDescending<ClothingItem> { stats(it).count }.thenByDescending { stats(it).lastWorn }
            )
            WardrobeSort.LEAST_WORN -> matching.sortedWith(
                compareBy<ClothingItem> { stats(it).count }.thenBy { stats(it).lastWorn }
            )
        }
        return WardrobeUiState(
            isLoading = false,
            allItems = items,
            visibleItems = visible,
            filters = filters,
            wearStats = wearStats
        )
    }
}
