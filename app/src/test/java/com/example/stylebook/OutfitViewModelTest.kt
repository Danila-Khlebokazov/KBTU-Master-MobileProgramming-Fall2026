package com.example.stylebook

import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.OutfitSlot
import com.example.stylebook.domain.OutfitGenerator
import com.example.stylebook.ui.viewmodel.OutfitViewModel
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

    private val today = LocalDate.of(2026, 11, 15)
    private val repository = FakeWardrobeRepository()
    private val viewModel by lazy { OutfitViewModel(repository, OutfitGenerator(Random(7)), today = { today }) }

    private fun runWithCollector(block: suspend () -> Unit) = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        block()
    }

    @Test
    fun firstLookIsGeneratedOnceTheWardrobeLoads() = runWithCollector {
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
        val loafers = repository.currentItems.first { it.id == 5 }

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

    @Test
    fun wearingTheCurrentLookLogsItOncePerDay() = runWithCollector {
        viewModel.wearCurrentOutfit()
        assertEquals("Logged in today's diary", viewModel.messages.first().text)
        viewModel.wearCurrentOutfit()
        assertEquals("This look is already in today's diary", viewModel.messages.first().text)

        val log = repository.wearLog.first()
        assertEquals(1, log.size)
        assertEquals(today, log.single().date)
        assertTrue(viewModel.uiState.value.isCurrentWornToday)
    }

    @Test
    fun wearingASavedLookMarksItAsWornToday() = runWithCollector {
        viewModel.saveCurrentOutfit()
        val saved = viewModel.uiState.value.savedOutfits.single()

        viewModel.wearSavedOutfit(saved.id)

        assertEquals(setOf(saved.id), viewModel.uiState.value.savedWornToday)
    }
}
