package com.example.stylebook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylebook.data.WardrobeRepository
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.WearEntry
import com.example.stylebook.domain.WearStats
import com.example.stylebook.domain.wearStatsByItem
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiaryDay(val date: LocalDate, val entries: List<WearEntry>)

data class PieceWear(val item: ClothingItem, val stats: WearStats)

data class DiaryUiState(
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.MIN,
    val days: List<DiaryDay> = emptyList(),
    val totalEntries: Int = 0,
    val entriesThisMonth: Int = 0,
    val piecesWorn: Int = 0,
    val totalPieces: Int = 0,
    val mostWorn: PieceWear? = null,
    /** Pieces never worn or not worn for [DiaryViewModel.FORGOTTEN_AFTER_DAYS] days, longest-forgotten first. */
    val forgotten: List<PieceWear> = emptyList()
)

/** The wear diary: what was actually worn, plus statistics that show which pieces are being neglected. */
class DiaryViewModel(
    private val repository: WardrobeRepository,
    private val today: () -> LocalDate = LocalDate::now
) : ViewModel() {

    val uiState: StateFlow<DiaryUiState> = combine(repository.items, repository.wearLog, ::buildState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DiaryUiState()
        )

    private val _messages = Channel<UserMessage>(Channel.BUFFERED)
    val messages: Flow<UserMessage> = _messages.receiveAsFlow()

    fun deleteEntry(entryId: Int) {
        viewModelScope.launch {
            repository.deleteWearEntry(entryId)
            _messages.send(UserMessage("Diary entry removed"))
        }
    }

    /** Logs a look from an earlier day again, for today. */
    fun wearAgainToday(entryId: Int) {
        val entry = uiState.value.days.firstNotNullOfOrNull { day -> day.entries.firstOrNull { it.id == entryId } } ?: return
        viewModelScope.launch {
            val logged = repository.logWear(entry.outfit, today())
            _messages.send(UserMessage(if (logged) "Logged in today's diary" else "This look is already in today's diary"))
        }
    }

    private fun buildState(items: List<ClothingItem>, log: List<WearEntry>): DiaryUiState {
        val todayDate = today()
        val stats = log.wearStatsByItem()
        val forgottenBefore = todayDate.minusDays(FORGOTTEN_AFTER_DAYS)
        val forgotten = items
            .map { PieceWear(it, stats[it.id] ?: WearStats()) }
            .filter { it.stats.lastWorn == null || it.stats.lastWorn <= forgottenBefore }
            .sortedWith(compareBy(nullsFirst()) { it.stats.lastWorn })
        val mostWorn = items
            .mapNotNull { item -> stats[item.id]?.let { PieceWear(item, it) } }
            .maxWithOrNull(compareBy<PieceWear> { it.stats.count }.thenBy { it.stats.lastWorn })
        return DiaryUiState(
            isLoading = false,
            today = todayDate,
            days = log.groupBy { it.date }.map { (date, entries) -> DiaryDay(date, entries) },
            totalEntries = log.size,
            entriesThisMonth = log.count { it.date.year == todayDate.year && it.date.month == todayDate.month },
            piecesWorn = items.count { it.id in stats },
            totalPieces = items.size,
            mostWorn = mostWorn,
            forgotten = forgotten
        )
    }

    companion object {
        const val FORGOTTEN_AFTER_DAYS = 30L
    }
}
