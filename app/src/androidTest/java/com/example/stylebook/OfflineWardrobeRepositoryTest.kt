package com.example.stylebook

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.stylebook.data.OfflineWardrobeRepository
import com.example.stylebook.data.local.StyleBookDatabase
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingColor
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.Outfit
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** CRUD against a real (in-memory) Room database. */
@RunWith(AndroidJUnit4::class)
class OfflineWardrobeRepositoryTest {
    private lateinit var db: StyleBookDatabase
    private lateinit var repository: OfflineWardrobeRepository
    private var clock = 1_000L

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, StyleBookDatabase::class.java).build()
        repository = OfflineWardrobeRepository(db, now = { clock++ })
    }

    @After
    fun closeDb() = db.close()

    private suspend fun addLook(): Outfit {
        val top = repository.addItem("Tee", Category.TOPS, ClothingColor.BLACK, setOf(Occasion.CASUAL))
        val bottom = repository.addItem("Jeans", Category.BOTTOMS, ClothingColor.DENIM, setOf(Occasion.CASUAL))
        val shoes = repository.addItem("Sneakers", Category.SHOES, ClothingColor.IVORY, setOf(Occasion.CASUAL))
        return Outfit(top = top, bottom = bottom, shoes = shoes)
    }

    @Test
    fun createReadUpdateDelete() = runBlocking {
        val added = repository.addItem("Linen Shirt", Category.TOPS, ClothingColor.IVORY, setOf(Occasion.WORK))
        assertEquals(listOf("Linen Shirt"), repository.items.first().map { it.name })

        repository.updateItem(added.id, "Oxford Shirt", Category.TOPS, ClothingColor.NAVY, setOf(Occasion.CASUAL))
        repository.toggleFavorite(added.id)
        val updated = repository.items.first().single()
        assertEquals("Oxford Shirt", updated.name)
        assertEquals(ClothingColor.NAVY, updated.color)
        assertEquals(setOf(Occasion.CASUAL), updated.occasions)
        assertTrue(updated.isFavorite)

        assertNotNull(repository.deleteItem(added.id))
        assertTrue(repository.items.first().isEmpty())
    }

    @Test
    fun newestItemComesFirst() = runBlocking {
        repository.addItem("Old", Category.TOPS, ClothingColor.IVORY, setOf(Occasion.WORK))
        repository.addItem("New", Category.TOPS, ClothingColor.IVORY, setOf(Occasion.WORK))
        assertEquals(listOf("New", "Old"), repository.items.first().map { it.name })
    }

    @Test
    fun savedLooksAndDiaryRejectDuplicates() = runBlocking {
        val look = addLook()
        val date = LocalDate.of(2026, 11, 15)

        assertNotNull(repository.saveOutfit(look))
        assertNull(repository.saveOutfit(look))
        assertTrue(repository.logWear(look, date))
        assertFalse(repository.logWear(look, date))
        assertTrue(repository.logWear(look, date.minusDays(1)))

        assertEquals(1, repository.savedOutfits.first().size)
        assertEquals(listOf(date, date.minusDays(1)), repository.wearLog.first().map { it.date })
    }

    @Test
    fun deletingAPieceRemovesItsLooksAndUndoRestoresThem() = runBlocking {
        val look = addLook()
        repository.saveOutfit(look)
        repository.logWear(look, LocalDate.of(2026, 11, 15))

        val deleted = repository.deleteItem(look.top.id)!!
        assertEquals(1, deleted.outfits.size)
        assertEquals(1, deleted.wears.size)
        assertTrue(repository.savedOutfits.first().isEmpty())
        assertTrue(repository.wearLog.first().isEmpty())

        repository.restore(deleted)
        assertEquals(3, repository.items.first().size)
        assertEquals(1, repository.savedOutfits.first().size)
        assertEquals(1, repository.wearLog.first().size)
    }

    @Test
    fun deletingADiaryEntryKeepsEverythingElse() = runBlocking {
        val look = addLook()
        repository.saveOutfit(look)
        repository.logWear(look, LocalDate.of(2026, 11, 15))

        repository.deleteWearEntry(repository.wearLog.first().single().id)

        assertTrue(repository.wearLog.first().isEmpty())
        assertEquals(1, repository.savedOutfits.first().size)
        assertEquals(3, repository.items.first().size)
    }
}
