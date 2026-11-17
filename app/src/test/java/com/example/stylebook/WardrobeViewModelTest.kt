package com.example.stylebook

import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingColor
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.Outfit
import com.example.stylebook.domain.ItemForm
import com.example.stylebook.ui.viewmodel.SaveItemResult
import com.example.stylebook.ui.viewmodel.WardrobeSort
import com.example.stylebook.ui.viewmodel.WardrobeViewModel
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WardrobeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeWardrobeRepository()
    // Lazy: the ViewModel must be created after MainDispatcherRule has replaced Dispatchers.Main.
    private val viewModel by lazy { WardrobeViewModel(repository) }

    private fun runWithCollector(block: suspend () -> Unit) = runTest {
        // WhileSubscribed StateFlows only update while someone collects them, just like the UI does.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        block()
    }

    private fun item(id: Int) = viewModel.uiState.value.itemById(id)!!

    @Test
    fun stateIsLoadingUntilTheRepositoryEmits() = runTest {
        assertTrue(viewModel.uiState.value.isLoading)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun invalidFormReportsEveryMissingField() = runWithCollector {
        val result = viewModel.saveItem(ItemForm(name = " "))

        val errors = (result as SaveItemResult.Invalid).errors
        assertNotNull(errors.name)
        assertNotNull(errors.category)
        assertNotNull(errors.color)
        assertNotNull(errors.occasions)
        assertEquals(13, viewModel.uiState.value.allItems.size)
    }

    @Test
    fun duplicateNameInSameCategoryIsRejected() = runWithCollector {
        val form = ItemForm("linen shirt", Category.TOPS, ClothingColor.IVORY, setOf(Occasion.CASUAL))

        val errors = viewModel.validate(form)

        assertTrue(errors.name!!.contains("already"))
        assertNull(viewModel.validate(form.copy(category = Category.KNITWEAR)).name)
    }

    @Test
    fun validItemIsTrimmedAddedFirstAndAnnounced() = runWithCollector {
        val form = ItemForm("  Denim Jacket  ", Category.OUTERWEAR, ClothingColor.DENIM, setOf(Occasion.CASUAL))

        val result = viewModel.saveItem(form)

        assertEquals(SaveItemResult.Saved, result)
        val first = viewModel.uiState.value.allItems.first()
        assertEquals("Denim Jacket", first.name)
        assertEquals(Category.OUTERWEAR, first.category)
        assertEquals("Denim Jacket added to your wardrobe", viewModel.messages.first().text)
    }

    @Test
    fun editingKeepsItsOwnNameValidAndUpdatesThePiece() = runWithCollector {
        val shirt = item(1)
        val form = ItemForm(shirt.name, shirt.category, ClothingColor.NAVY, setOf(Occasion.EVENING))

        assertNull(viewModel.validate(form, editingId = shirt.id).name)
        assertEquals(SaveItemResult.Saved, viewModel.saveItem(form, editingId = shirt.id))

        val updated = item(1)
        assertEquals(ClothingColor.NAVY, updated.color)
        assertEquals(setOf(Occasion.EVENING), updated.occasions)
        assertTrue("Favorite flag is kept", updated.isFavorite)
        assertEquals("Linen Shirt updated", viewModel.messages.first().text)
    }

    @Test
    fun editingCannotTakeAnotherPiecesName() = runWithCollector {
        val form = ItemForm("Cotton Tee", Category.TOPS, ClothingColor.IVORY, setOf(Occasion.CASUAL))

        val result = viewModel.saveItem(form, editingId = 1)

        assertTrue(result is SaveItemResult.Invalid)
        assertEquals("Linen Shirt", item(1).name)
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
    fun sortingByWearUsesTheDiary() = runWithCollector {
        val look = Outfit(top = item(8), bottom = item(2), shoes = item(9))
        repository.logWear(look, LocalDate.of(2026, 11, 1))
        repository.logWear(look, LocalDate.of(2026, 11, 2))

        viewModel.onSortSelected(WardrobeSort.MOST_WORN)
        assertEquals(setOf(8, 2, 9), viewModel.uiState.value.visibleItems.take(3).map { it.id }.toSet())
        assertEquals(2, viewModel.uiState.value.wearStatsFor(8).count)
        assertEquals(LocalDate.of(2026, 11, 2), viewModel.uiState.value.wearStatsFor(8).lastWorn)

        viewModel.onSortSelected(WardrobeSort.LEAST_WORN)
        assertFalse(viewModel.uiState.value.visibleItems.take(10).any { it.id in setOf(8, 2, 9) })
    }

    @Test
    fun toggleFavoriteUpdatesOnlySelectedItem() = runWithCollector {
        viewModel.toggleFavorite(2)

        assertTrue(item(2).isFavorite)
        assertEquals(false, item(4).isFavorite)
    }

    @Test
    fun deleteRemovesRelatedDataAndUndoRestoresEverything() = runWithCollector {
        val originalIds = viewModel.uiState.value.allItems.map { it.id }
        val look = Outfit(top = item(1), bottom = item(6), shoes = item(5))
        repository.saveOutfit(look)
        repository.logWear(look, LocalDate.of(2026, 11, 3))

        viewModel.deleteItem(5)
        assertNull(viewModel.uiState.value.itemById(5))
        assertTrue(repository.savedOutfits.first().isEmpty())
        assertEquals(0, viewModel.uiState.value.wearStatsFor(1).count)

        val message = viewModel.messages.first()
        assertEquals("Removed Leather Loafers and 1 saved look and 1 diary entry", message.text)
        message.onAction!!.invoke()
        assertEquals(originalIds, viewModel.uiState.value.allItems.map { it.id })
        assertEquals(1, repository.savedOutfits.first().size)
        assertEquals(1, viewModel.uiState.value.wearStatsFor(1).count)
    }
}
