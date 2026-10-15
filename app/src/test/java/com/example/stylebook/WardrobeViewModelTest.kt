package com.example.stylebook

import com.example.stylebook.ui.viewmodel.AddItemResult
import com.example.stylebook.ui.viewmodel.WardrobeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WardrobeViewModelTest {
    @Test
    fun addItemRejectsMissingRequiredFields() {
        val viewModel = WardrobeViewModel()
        val originalItems = viewModel.uiState.value.items

        val result = viewModel.addItem(" ", "Tops", "Ivory")

        assertEquals(AddItemResult.INVALID_INPUT, result)
        assertEquals(originalItems, viewModel.uiState.value.items)
    }

    @Test
    fun addItemTrimsInputAndUpdatesWardrobe() {
        val viewModel = WardrobeViewModel()

        val result = viewModel.addItem("  Linen shirt  ", " Tops ", " Ivory ")

        assertEquals(AddItemResult.ADDED, result)
        val addedItem = viewModel.uiState.value.items.first()
        assertEquals("Linen shirt", addedItem.name)
        assertEquals("Tops", addedItem.category)
        assertEquals("Ivory", addedItem.color)
    }

    @Test
    fun toggleFavoriteUpdatesOnlySelectedItem() {
        val viewModel = WardrobeViewModel()

        viewModel.toggleFavorite(1)

        assertTrue(viewModel.uiState.value.items.first { it.id == 1 }.isFavorite)
        assertFalse(viewModel.uiState.value.items.first { it.id == 2 }.isFavorite)
    }
}
