package com.example.stylebook

import com.example.stylebook.data.WardrobeRepository
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingColor
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.domain.AddItemForm
import com.example.stylebook.ui.viewmodel.AddItemResult
import com.example.stylebook.ui.viewmodel.WardrobeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WardrobeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = WardrobeRepository()
    // Lazy: the ViewModel must be created after MainDispatcherRule has replaced Dispatchers.Main.
    private val viewModel by lazy { WardrobeViewModel(repository) }

    private fun runWithCollector(block: suspend () -> Unit) = runTest {
        // WhileSubscribed StateFlows only update while someone collects them, just like the UI does.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        block()
    }

    @Test
    fun invalidFormReportsEveryMissingField() = runWithCollector {
        val result = viewModel.addItem(AddItemForm(name = " "))

        val errors = (result as AddItemResult.Invalid).errors
        assertNotNull(errors.name)
        assertNotNull(errors.category)
        assertNotNull(errors.color)
        assertNotNull(errors.occasions)
        assertEquals(repository.items.value, viewModel.uiState.value.allItems)
    }

    @Test
    fun duplicateNameInSameCategoryIsRejected() = runWithCollector {
        val form = AddItemForm("linen shirt", Category.TOPS, ClothingColor.IVORY, setOf(Occasion.CASUAL))

        val errors = viewModel.validate(form)

        assertTrue(errors.name!!.contains("already"))
        assertNull(viewModel.validate(form.copy(category = Category.KNITWEAR)).name)
    }

    @Test
    fun validItemIsTrimmedAddedFirstAndAnnounced() = runWithCollector {
        val form = AddItemForm("  Denim Jacket  ", Category.OUTERWEAR, ClothingColor.DENIM, setOf(Occasion.CASUAL))

        val result = viewModel.addItem(form)

        assertTrue(result is AddItemResult.Added)
        val first = viewModel.uiState.value.allItems.first()
        assertEquals("Denim Jacket", first.name)
        assertEquals(Category.OUTERWEAR, first.category)
        assertEquals("Denim Jacket added to your wardrobe", viewModel.messages.first().text)
    }

    @Test
    fun searchMatchesNameColorAndOccasion() = runWithCollector {
        viewModel.onQueryChange("jeans")
        assertEquals(listOf(2), viewModel.uiState.value.visibleItems.map { it.id })

        viewModel.onQueryChange("burgundy")
        assertEquals(listOf(11), viewModel.uiState.value.visibleItems.map { it.id })

        viewModel.onQueryChange("evening")
        assertTrue(viewModel.uiState.value.visibleItems.all { Occasion.EVENING in it.occasions })
    }

    @Test
    fun categoryAndFavoriteFiltersCombine() = runWithCollector {
        viewModel.onCategorySelected(Category.OUTERWEAR)
        assertEquals(setOf(3, 10), viewModel.uiState.value.visibleItems.map { it.id }.toSet())

        viewModel.onFavoritesOnlyChange(true)
        assertEquals(listOf(3), viewModel.uiState.value.visibleItems.map { it.id })

        viewModel.clearFilters()
        assertEquals(viewModel.uiState.value.allItems, viewModel.uiState.value.visibleItems)
    }

    @Test
    fun toggleFavoriteUpdatesOnlySelectedItem() = runWithCollector {
        viewModel.toggleFavorite(2)

        assertTrue(viewModel.uiState.value.itemById(2)!!.isFavorite)
        assertEquals(false, viewModel.uiState.value.itemById(4)!!.isFavorite)
    }

    @Test
    fun deleteCanBeUndoneAtTheSamePosition() = runWithCollector {
        val originalIds = viewModel.uiState.value.allItems.map { it.id }

        viewModel.deleteItem(5)
        assertNull(viewModel.uiState.value.itemById(5))

        val message = viewModel.messages.first()
        assertEquals("Undo", message.actionLabel)
        message.onAction!!.invoke()
        assertEquals(originalIds, viewModel.uiState.value.allItems.map { it.id })
    }
}
