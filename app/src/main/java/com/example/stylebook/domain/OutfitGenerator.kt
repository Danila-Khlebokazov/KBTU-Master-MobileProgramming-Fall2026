package com.example.stylebook.domain

import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.Outfit
import com.example.stylebook.data.model.OutfitSlot
import kotlin.random.Random

sealed interface GenerationResult {
    data class Success(val outfit: Outfit, val notes: List<String>) : GenerationResult

    /** The wardrobe has no piece for these required slots (for the chosen occasion). */
    data class MissingPieces(val slots: List<OutfitSlot>) : GenerationResult
}

/**
 * Builds outfits from existing wardrobe pieces. Pure Kotlin (no Android types), so it is easy to unit test.
 *
 * Rules:
 *  1. A look needs a top (top or knitwear), a bottom and shoes; outerwear is an optional layer.
 *  2. If an occasion is chosen, every non-locked piece must be tagged with it.
 *  3. Color harmony: at most one accent (non-neutral) color per look. If no combination
 *     satisfies this, the generator still returns a look and says so in the notes.
 *  4. Locked pieces are kept as they are; everything else is re-rolled, avoiding the previous look.
 */
class OutfitGenerator(private val random: Random = Random.Default) {

    fun generate(
        items: List<ClothingItem>,
        occasion: Occasion?,
        includeLayer: Boolean,
        locked: Map<OutfitSlot, ClothingItem> = emptyMap(),
        previous: Outfit? = null
    ): GenerationResult {
        fun candidatesFor(slot: OutfitSlot): List<ClothingItem> =
            locked[slot]?.let { listOf(it) }
                ?: items.filter { it.category.slot == slot && (occasion == null || occasion in it.occasions) }

        val tops = candidatesFor(OutfitSlot.TOP)
        val bottoms = candidatesFor(OutfitSlot.BOTTOM)
        val shoes = candidatesFor(OutfitSlot.SHOES)

        val missing = REQUIRED_SLOTS.zip(listOf(tops, bottoms, shoes))
            .filter { (_, candidates) -> candidates.isEmpty() }
            .map { (slot, _) -> slot }
        if (missing.isNotEmpty()) return GenerationResult.MissingPieces(missing)

        val combinations = buildList {
            for (top in tops) for (bottom in bottoms) for (shoe in shoes) {
                add(Outfit(top = top, bottom = bottom, shoes = shoe, occasion = occasion))
            }
        }
        val harmonious = combinations.filter { isHarmonious(it.pieces) }
        val pool = harmonious.ifEmpty { combinations }
        val previousBase = previous?.let { setOf(it.top.id, it.bottom.id, it.shoes.id) }
        val fresh = pool.filter { setOf(it.top.id, it.bottom.id, it.shoes.id) != previousBase }.ifEmpty { pool }
        val base = fresh.random(random)

        val layers = if (includeLayer) candidatesFor(OutfitSlot.OUTERWEAR) else emptyList()
        val layer = layers.filter { isHarmonious(base.pieces + it) }
            .ifEmpty { layers }
            .let { options -> options.filter { it.id != previous?.outerwear?.id }.ifEmpty { options } }
            .randomOrNull(random)

        val outfit = base.copy(outerwear = layer)
        return GenerationResult.Success(outfit, notesFor(outfit, harmonious.isNotEmpty(), includeLayer))
    }

    fun isHarmonious(pieces: List<ClothingItem>): Boolean = pieces.count { !it.color.isNeutral } <= 1

    private fun notesFor(outfit: Outfit, harmonyFound: Boolean, wantedLayer: Boolean): List<String> = buildList {
        val accents = outfit.pieces.filterNot { it.color.isNeutral }
        when {
            accents.isEmpty() -> add("An all-neutral palette — calm, versatile and hard to get wrong.")
            accents.size == 1 -> {
                val accent = accents.single()
                add("The ${accent.color.displayName.lowercase()} ${accent.name.lowercase()} is the statement; the neutrals let it lead.")
            }
            !harmonyFound -> add("Bold mix: no calmer combination exists yet — add a neutral piece for balance.")
            else -> add("Two statement colors — try locking a neutral piece and shuffling again.")
        }
        outfit.occasion?.let { add("Every piece is tagged for ${it.displayName.lowercase()}.") }
        when {
            outfit.outerwear != null -> add("Finished with the ${outfit.outerwear.name.lowercase()} as a layer.")
            wantedLayer -> add("No matching layer — this look goes without outerwear.")
        }
        outfit.pieces.firstOrNull { it.isFavorite }?.let { add("Features your favorite ${it.name.lowercase()}.") }
    }

    private companion object {
        val REQUIRED_SLOTS = listOf(OutfitSlot.TOP, OutfitSlot.BOTTOM, OutfitSlot.SHOES)
    }
}
