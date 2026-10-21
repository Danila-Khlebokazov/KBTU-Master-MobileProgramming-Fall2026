package com.example.stylebook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylebook.data.AppContainer
import com.example.stylebook.data.DeletedItem
import com.example.stylebook.data.WardrobeRepository
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.domain.AddItemErrors
import com.example.stylebook.domain.AddItemForm
import com.example.stylebook.domain.ClothingItemValidator
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

data class WardrobeFilters(
    val query: String = "",
    val category: Category? = null,
    val favoritesOnly: Boolean = false
)

data class WardrobeUiState(
    val allItems: List<ClothingItem> = emptyList(),
    val visibleItems: List<ClothingItem> = emptyList(),
    val filters: WardrobeFilters = WardrobeFilters()
) {
    val favoriteItems: List<ClothingItem> get() = allItems.filter { it.isFavorite }
    val categoryCounts: Map<Category, Int> get() = allItems.groupingBy { it.category }.eachCount()
    val hasActiveFilters: Boolean
        get() = filters.query.isNotBlank() || filters.category != null || filters.favoritesOnly

    fun itemById(id: Int): ClothingItem? = allItems.firstOrNull { it.id == id }
}

sealed interface AddItemResult {
    data class Added(val item: ClothingItem) : AddItemResult
    data class Invalid(val errors: AddItemErrors) : AddItemResult
}

/** Wardrobe pieces: search/filter state, adding, favoriting and deleting with undo. */
class WardrobeViewModel(
    private val repository: WardrobeRepository = AppContainer.wardrobeRepository
) : ViewModel() {

    private val filters = MutableStateFlow(WardrobeFilters())

    val uiState: StateFlow<WardrobeUiState> = combine(repository.items, filters, ::buildState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildState(repository.items.value, filters.value)
        )

    private val _messages = Channel<UserMessage>(Channel.BUFFERED)
    val messages: Flow<UserMessage> = _messages.receiveAsFlow()

    private var lastDeleted: DeletedItem? = null

    fun onQueryChange(query: String) = filters.update { it.copy(query = query) }

    fun onCategorySelected(category: Category?) = filters.update { it.copy(category = category) }

    fun onFavoritesOnlyChange(enabled: Boolean) = filters.update { it.copy(favoritesOnly = enabled) }

    fun clearFilters() = filters.update { WardrobeFilters() }

    /** Used by the Home screen category shortcuts: show exactly one category. */
    fun showCategory(category: Category) = filters.update { WardrobeFilters(category = category) }

    fun toggleFavorite(itemId: Int) = repository.toggleFavorite(itemId)

    fun validate(form: AddItemForm): AddItemErrors =
        ClothingItemValidator.validate(form, repository.items.value)

    fun addItem(form: AddItemForm): AddItemResult {
        val errors = validate(form)
        if (errors.hasErrors) return AddItemResult.Invalid(errors)
        val item = repository.addItem(
            name = form.name.trim(),
            category = requireNotNull(form.category),
            color = requireNotNull(form.color),
            occasions = form.occasions
        )
        send(UserMessage("${item.name} added to your wardrobe"))
        return AddItemResult.Added(item)
    }

    fun deleteItem(itemId: Int) {
        val deleted = repository.deleteItem(itemId) ?: return
        lastDeleted = deleted
        val outfitsNote = when (val count = deleted.removedOutfits.size) {
            0 -> ""
            1 -> " and 1 saved look"
            else -> " and $count saved looks"
        }
        send(UserMessage("Removed ${deleted.item.name}$outfitsNote", actionLabel = "Undo", onAction = ::undoDelete))
    }

    fun undoDelete() {
        lastDeleted?.let(repository::restore)
        lastDeleted = null
    }

    private fun send(message: UserMessage) {
        viewModelScope.launch { _messages.send(message) }
    }

    private fun buildState(items: List<ClothingItem>, filters: WardrobeFilters): WardrobeUiState {
        val query = filters.query.trim()
        val visible = items.filter { item ->
            val matchesQuery = query.isEmpty() ||
                item.name.contains(query, ignoreCase = true) ||
                item.category.displayName.contains(query, ignoreCase = true) ||
                item.color.displayName.contains(query, ignoreCase = true) ||
                item.occasions.any { it.displayName.contains(query, ignoreCase = true) }
            val matchesCategory = filters.category == null || item.category == filters.category
            matchesQuery && matchesCategory && (!filters.favoritesOnly || item.isFavorite)
        }
        return WardrobeUiState(allItems = items, visibleItems = visible, filters = filters)
    }
}
