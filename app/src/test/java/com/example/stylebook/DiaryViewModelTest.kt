package com.example.stylebook

import com.example.stylebook.data.model.Outfit
import com.example.stylebook.ui.viewmodel.DiaryViewModel
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 15)
    private val repository = FakeWardrobeRepository()
    private val viewModel by lazy { DiaryViewModel(repository, today = { today }) }

    private val items get() = repository.currentItems.associateBy { it.id }
    private val casualLook get() = Outfit(top = items.getValue(8), bottom = items.getValue(2), shoes = items.getValue(9))
    private val workLook get() = Outfit(top = items.getValue(1), bottom = items.getValue(6), shoes = items.getValue(5))

    private fun runWithCollector(block: suspend () -> Unit) = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        block()
    }

    @Test
    fun entriesAreGroupedByDayNewestFirst() = runWithCollector {
        repository.logWear(casualLook, today.minusDays(1))
        repository.logWear(workLook, today)
        repository.logWear(casualLook, today)

        val days = viewModel.uiState.value.days
        assertEquals(listOf(today, today.minusDays(1)), days.map { it.date })
        assertEquals(2, days.first().entries.size)
        assertEquals(3, viewModel.uiState.value.totalEntries)
    }

    @Test
    fun statsShowMostWornAndForgottenPieces() = runWithCollector {
        repository.logWear(casualLook, today)
        repository.logWear(casualLook, today.minusDays(2))
        repository.logWear(workLook, today.minusDays(40))

        val state = viewModel.uiState.value
        assertEquals(6, state.piecesWorn)
        assertEquals(2, state.mostWorn!!.stats.count)
        assertTrue(state.mostWorn!!.item.id in setOf(8, 2, 9))
        val forgottenIds = state.forgotten.map { it.item.id }
        assertTrue("Worn 40 days ago counts as forgotten", forgottenIds.containsAll(listOf(1, 6, 5)))
        assertFalse(forgottenIds.any { it in setOf(8, 2, 9) })
        assertEquals("Never-worn pieces come first", null, state.forgotten.first().stats.lastWorn)
    }

    @Test
    fun deletingAnEntryKeepsThePieces() = runWithCollector {
        repository.logWear(casualLook, today)
        val entryId = viewModel.uiState.value.days.single().entries.single().id

        viewModel.deleteEntry(entryId)

        assertTrue(viewModel.uiState.value.days.isEmpty())
        assertEquals(13, viewModel.uiState.value.totalPieces)
    }

    @Test
    fun wearAgainCopiesAPastLookToToday() = runWithCollector {
        repository.logWear(workLook, today.minusDays(3))
        val entryId = viewModel.uiState.value.days.single().entries.single().id

        viewModel.wearAgainToday(entryId)
        viewModel.wearAgainToday(entryId)

        val todays = viewModel.uiState.value.days.first()
        assertEquals(today, todays.date)
        assertEquals(1, todays.entries.size)
        assertEquals(workLook.pieceIds, todays.entries.single().outfit.pieceIds)
    }
}
