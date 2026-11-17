package com.example.stylebook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylebook.data.WardrobeRepository
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.Outfit
import com.example.stylebook.data.model.OutfitSlot
import com.example.stylebook.data.model.WearEntry
import com.example.stylebook.domain.GenerationResult
import com.example.stylebook.domain.OutfitGenerator
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Generator settings and the current result, owned by the ViewModel. */
private data class GeneratorState(
    val occasion: Occasion? = null,
    val includeLayer: Boolean = true,
    val outfit: Outfit? = null,
    val notes: List<String> = emptyList(),
    val lockedSlots: Set<OutfitSlot> = emptySet(),
    val missingSlots: List<OutfitSlot> = emptyList()
)

data class OutfitUiState(
    val isLoading: Boolean = true,
    val occasion: Occasion? = null,
    val includeLayer: Boolean = true,
    val outfit: Outfit? = null,
    val notes: List<String> = emptyList(),
    val lockedSlots: Set<OutfitSlot> = emptySet(),
    val missingSlots: List<OutfitSlot> = emptyList(),
    val isCurrentSaved: Boolean = false,
    val isCurrentWornToday: Boolean = false,
    val savedOutfits: List<Outfit> = emptyList(),
    /** Ids of saved looks that are already in today's diary. */
    val savedWornToday: Set<Int> = emptySet()
)

/** Outfit generator, saved outfits (favorite looks) and logging looks to the wear diary. */
class OutfitViewModel(
    private val repository: WardrobeRepository,
    private val generator: OutfitGenerator = OutfitGenerator(),
    private val today: () -> LocalDate = LocalDate::now
) : ViewModel() {

    private val generatorState = MutableStateFlow(GeneratorState())

    // Kept hot because the generator reads the wardrobe synchronously; null until the database has loaded.
    private val items: StateFlow<List<ClothingItem>?> =
        repository.items.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _messages = Channel<UserMessage>(Channel.BUFFERED)
    val messages: Flow<UserMessage> = _messages.receiveAsFlow()

    init {
        // Build the first look once the wardrobe is loaded, and rebuild it if one of its pieces is deleted.
        viewModelScope.launch {
            items.filterNotNull().collect { items ->
                val outfit = generatorState.value.outfit
                if (outfit == null || refresh(outfit, items) == null) generate()
            }
        }
    }

    val uiState: StateFlow<OutfitUiState> =
        combine(generatorState, items.filterNotNull(), repository.savedOutfits, repository.wearLog, ::buildState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = OutfitUiState()
            )

    /** Locked pieces are kept even if they are not tagged for the new occasion (the user chose them explicitly). */
    fun onOccasionSelected(occasion: Occasion?) {
        generatorState.update { it.copy(occasion = occasion) }
        generate()
    }

    fun onIncludeLayerChange(include: Boolean) {
        generatorState.update { state ->
            state.copy(includeLayer = include, lockedSlots = state.lockedSlots - OutfitSlot.OUTERWEAR)
        }
        generate()
    }

    fun toggleLock(slot: OutfitSlot) {
        generatorState.update { state ->
            if (state.outfit?.pieceFor(slot) == null) return@update state
            val locked = if (slot in state.lockedSlots) state.lockedSlots - slot else state.lockedSlots + slot
            state.copy(lockedSlots = locked)
        }
    }

    /** Re-rolls every unlocked piece using the current wardrobe. */
    fun generate() {
        val current = currentOutfit()
        val locked = generatorState.value.lockedSlots
            .mapNotNull { slot -> current?.pieceFor(slot)?.let { slot to it } }
            .toMap()
        runGenerator(locked, previous = current)
    }

    /** Opens the generator around a specific piece ("Style this piece" on the details screen). */
    fun styleAround(item: ClothingItem) {
        generatorState.update { state ->
            state.copy(
                occasion = state.occasion?.takeIf { it in item.occasions },
                includeLayer = state.includeLayer || item.category.slot == OutfitSlot.OUTERWEAR
            )
        }
        runGenerator(locked = mapOf(item.category.slot to item), previous = null)
    }

    fun saveCurrentOutfit() {
        val outfit = currentOutfit() ?: return
        viewModelScope.launch {
            val saved = repository.saveOutfit(outfit)
            send(UserMessage(if (saved != null) "Look saved to Favorites" else "This look is already in Favorites"))
        }
    }

    fun removeOutfit(outfitId: Int) {
        val outfit = uiState.value.savedOutfits.firstOrNull { it.id == outfitId } ?: return
        viewModelScope.launch {
            repository.removeOutfit(outfitId)
            send(
                UserMessage(
                    "Look removed",
                    actionLabel = "Undo",
                    onAction = { viewModelScope.launch { repository.saveOutfit(outfit) } }
                )
            )
        }
    }

    /** Logs the look on screen to today's page of the wear diary. */
    fun wearCurrentOutfit() {
        currentOutfit()?.let(::logWear)
    }

    fun wearSavedOutfit(outfitId: Int) {
        uiState.value.savedOutfits.firstOrNull { it.id == outfitId }?.let(::logWear)
    }

    private fun logWear(outfit: Outfit) {
        viewModelScope.launch {
            val logged = repository.logWear(outfit, today())
            send(UserMessage(if (logged) "Logged in today's diary" else "This look is already in today's diary"))
        }
    }

    private fun runGenerator(locked: Map<OutfitSlot, ClothingItem>, previous: Outfit?) {
        val state = generatorState.value
        val result = generator.generate(
            items = items.value.orEmpty(),
            occasion = state.occasion,
            includeLayer = state.includeLayer,
            locked = locked,
            previous = previous
        )
        generatorState.update {
            when (result) {
                is GenerationResult.Success -> it.copy(
                    outfit = result.outfit,
                    notes = result.notes,
                    lockedSlots = locked.keys,
                    missingSlots = emptyList()
                )
                is GenerationResult.MissingPieces -> it.copy(
                    outfit = null,
                    notes = emptyList(),
                    lockedSlots = emptySet(),
                    missingSlots = result.slots
                )
            }
        }
    }

    private fun currentOutfit(): Outfit? =
        generatorState.value.outfit?.let { refresh(it, items.value.orEmpty()) }

    private fun send(message: UserMessage) {
        viewModelScope.launch { _messages.send(message) }
    }

    private fun buildState(
        state: GeneratorState,
        items: List<ClothingItem>,
        saved: List<Outfit>,
        log: List<WearEntry>
    ): OutfitUiState {
        val outfit = state.outfit?.let { refresh(it, items) }
        val todayDate = today()
        val wornToday = log.filter { it.date == todayDate }.map { it.outfit.pieceIds }.toSet()
        return OutfitUiState(
            isLoading = false,
            occasion = state.occasion,
            includeLayer = state.includeLayer,
            outfit = outfit,
            notes = if (outfit != null) state.notes else emptyList(),
            lockedSlots = if (outfit != null) state.lockedSlots else emptySet(),
            missingSlots = state.missingSlots,
            isCurrentSaved = outfit != null && saved.any { it.hasSamePiecesAs(outfit) },
            isCurrentWornToday = outfit != null && outfit.pieceIds in wornToday,
            savedOutfits = saved,
            savedWornToday = saved.filter { it.pieceIds in wornToday }.mapTo(mutableSetOf()) { it.id }
        )
    }

    /**
     * Replaces the outfit's pieces with their latest versions (e.g. after a favorite toggle or an edit).
     * Returns null if a piece has been deleted or moved to another slot by an edit.
     */
    private fun refresh(outfit: Outfit, items: List<ClothingItem>): Outfit? {
        val byId = items.associateBy { it.id }
        fun latest(piece: ClothingItem, slot: OutfitSlot) = byId[piece.id]?.takeIf { it.category.slot == slot }
        val top = latest(outfit.top, OutfitSlot.TOP) ?: return null
        val bottom = latest(outfit.bottom, OutfitSlot.BOTTOM) ?: return null
        val shoes = latest(outfit.shoes, OutfitSlot.SHOES) ?: return null
        val outerwear = outfit.outerwear?.let { latest(it, OutfitSlot.OUTERWEAR) ?: return null }
        return outfit.copy(top = top, bottom = bottom, shoes = shoes, outerwear = outerwear)
    }
}
