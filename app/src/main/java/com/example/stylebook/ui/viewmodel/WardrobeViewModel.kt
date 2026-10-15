package com.example.stylebook.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.stylebook.data.SampleClothingData
import com.example.stylebook.data.model.ClothingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WardrobeUiState(
    val items: List<ClothingItem> = SampleClothingData.items
)

enum class AddItemResult {
    ADDED,
    INVALID_INPUT
}

class WardrobeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WardrobeUiState())
    val uiState: StateFlow<WardrobeUiState> = _uiState.asStateFlow()

    fun addItem(name: String, category: String, color: String): AddItemResult {
        val cleanName = name.trim()
        val cleanCategory = category.trim()
        val cleanColor = color.trim()
        if (cleanName.isEmpty() || cleanCategory.isEmpty() || cleanColor.isEmpty()) {
            return AddItemResult.INVALID_INPUT
        }

        val nextId = (_uiState.value.items.maxOfOrNull(ClothingItem::id) ?: 0) + 1
        val item = ClothingItem(
            id = nextId,
            name = cleanName,
            category = cleanCategory,
            color = cleanColor,
            imageRes = imageForCategory(cleanCategory)
        )
        _uiState.value = _uiState.value.copy(items = listOf(item) + _uiState.value.items)
        return AddItemResult.ADDED
    }

    fun toggleFavorite(itemId: Int) {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map { item ->
                if (item.id == itemId) item.copy(isFavorite = !item.isFavorite) else item
            }
        )
    }

    private fun imageForCategory(category: String): Int = when (category.lowercase()) {
        "bottoms", "trousers", "jeans" -> com.example.stylebook.R.drawable.garment_jeans
        "outerwear", "coat", "coats" -> com.example.stylebook.R.drawable.garment_coat
        "knitwear", "sweater", "sweaters" -> com.example.stylebook.R.drawable.garment_sweater
        "shoes", "shoe", "footwear" -> com.example.stylebook.R.drawable.garment_shoes
        else -> com.example.stylebook.R.drawable.garment_shirt
    }
}
