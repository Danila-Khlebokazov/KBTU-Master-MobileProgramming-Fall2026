package com.example.stylebook

import com.example.stylebook.data.SampleClothingData
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.OutfitSlot
import com.example.stylebook.domain.GenerationResult
import com.example.stylebook.domain.OutfitGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class OutfitGeneratorTest {
    private val items = SampleClothingData.items
    private val generator = OutfitGenerator(Random(42))

    private fun success(result: GenerationResult) = result as GenerationResult.Success

    @Test
    fun outfitHasOnePieceForEachRequiredSlot() {
        repeat(20) {
            val outfit = success(generator.generate(items, occasion = null, includeLayer = true)).outfit
            assertEquals(OutfitSlot.TOP, outfit.top.category.slot)
            assertEquals(OutfitSlot.BOTTOM, outfit.bottom.category.slot)
            assertEquals(OutfitSlot.SHOES, outfit.shoes.category.slot)
            assertEquals(Category.OUTERWEAR, outfit.outerwear?.category)
        }
    }

    @Test
    fun occasionFilterAndColorHarmonyAreRespected() {
        repeat(20) {
            val outfit = success(generator.generate(items, Occasion.EVENING, includeLayer = true)).outfit
            assertTrue(outfit.pieces.all { Occasion.EVENING in it.occasions })
            assertTrue(generator.isHarmonious(outfit.pieces))
        }
    }

    @Test
    fun layerIsOmittedWhenDisabled() {
        val outfit = success(generator.generate(items, null, includeLayer = false)).outfit
        assertNull(outfit.outerwear)
    }

    @Test
    fun lockedPieceIsKeptAndShuffleChangesTheRest() {
        val knit = items.first { it.id == 4 }
        val first = success(generator.generate(items, null, false, locked = mapOf(OutfitSlot.TOP to knit))).outfit

        val second = success(
            generator.generate(items, null, false, locked = mapOf(OutfitSlot.TOP to knit), previous = first)
        ).outfit

        assertEquals(knit, first.top)
        assertEquals(knit, second.top)
        assertNotEquals(first.pieceIds, second.pieceIds)
    }

    @Test
    fun missingRequiredSlotsAreReported() {
        val noShoes = items.filterNot { it.category == Category.SHOES }

        val result = generator.generate(noShoes, null, includeLayer = true)

        assertEquals(GenerationResult.MissingPieces(listOf(OutfitSlot.SHOES)), result)
    }
}
