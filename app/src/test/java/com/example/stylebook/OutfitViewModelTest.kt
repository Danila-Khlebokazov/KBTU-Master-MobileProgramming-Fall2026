package com.example.stylebook

import com.example.stylebook.data.WardrobeRepository
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.OutfitSlot
import com.example.stylebook.domain.OutfitGenerator
import com.example.stylebook.ui.viewmodel.OutfitViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class OutfitViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = WardrobeRepository()
    private val viewModel by lazy { OutfitViewModel(repository, OutfitGenerator(Random(7))) }

    private fun runWithCollector(block: suspend () -> Unit) = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        block()
    }

    @Test
    fun firstLookIsGeneratedImmediately() = runWithCollector {
        assertNotNull(viewModel.uiState.value.outfit)
    }

    @Test
    fun savingMarksLookAsSavedAndPreventsDuplicates() = runWithCollector {
        viewModel.saveCurrentOutfit()
        viewModel.saveCurrentOutfit()

        assertTrue(viewModel.uiState.value.isCurrentSaved)
        assertEquals(1, viewModel.uiState.value.savedOutfits.size)
    }

    @Test
    fun lockedSlotSurvivesShuffle() = runWithCollector {
        val top = viewModel.uiState.value.outfit!!.top
        viewModel.toggleLock(OutfitSlot.TOP)

        repeat(5) { viewModel.generate() }

        assertEquals(top.id, viewModel.uiState.value.outfit!!.top.id)
        assertEquals(setOf(OutfitSlot.TOP), viewModel.uiState.value.lockedSlots)
    }

    @Test
    fun styleAroundLocksTheChosenPiece() = runWithCollector {
        val loafers = repository.items.value.first { it.id == 5 }

        viewModel.styleAround(loafers)

        assertEquals(loafers.id, viewModel.uiState.value.outfit!!.shoes.id)
        assertTrue(OutfitSlot.SHOES in viewModel.uiState.value.lockedSlots)
    }

    @Test
    fun deletingAPieceRemovesSavedLooksAndRebuildsCurrentLook() = runWithCollector {
        viewModel.saveCurrentOutfit()
        val topId = viewModel.uiState.value.outfit!!.top.id

        repository.deleteItem(topId)

        assertTrue(viewModel.uiState.value.savedOutfits.isEmpty())
        val rebuilt = viewModel.uiState.value.outfit
        assertNotNull(rebuilt)
        assertFalse(rebuilt!!.contains(topId))
    }

    @Test
    fun occasionChangeRegeneratesForThatOccasion() = runWithCollector {
        viewModel.onOccasionSelected(Occasion.WORK)

        val outfit = viewModel.uiState.value.outfit!!
        assertTrue(outfit.pieces.all { Occasion.WORK in it.occasions })
        assertFalse(viewModel.uiState.value.isCurrentSaved)
    }
}
