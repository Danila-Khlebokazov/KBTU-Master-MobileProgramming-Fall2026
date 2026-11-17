package com.example.stylebook.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.stylebook.data.SampleClothingData

@Database(
    entities = [ClothingItemEntity::class, SavedOutfitEntity::class, WearLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StyleBookDatabase : RoomDatabase() {
    abstract fun clothingItemDao(): ClothingItemDao
    abstract fun savedOutfitDao(): SavedOutfitDao
    abstract fun wearLogDao(): WearLogDao

    companion object {
        private const val NAME = "stylebook.db"

        @Volatile
        private var instance: StyleBookDatabase? = null

        fun getInstance(context: Context): StyleBookDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, StyleBookDatabase::class.java, NAME)
                .addCallback(SeedSampleWardrobe)
                .build()
                .also { instance = it }
        }
    }

    /** Fills a brand-new database with the sample wardrobe so every feature can be tried on first launch. */
    private object SeedSampleWardrobe : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            val converters = Converters()
            SampleClothingData.items.forEach { item ->
                val values = ContentValues().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("category", item.category.name)
                    put("color", item.color.name)
                    put("occasions", converters.occasionsToString(item.occasions))
                    put("art", item.art.name)
                    put("isFavorite", item.isFavorite)
                    put("addedAt", item.addedAt)
                }
                db.insert("clothing_items", SQLiteDatabase.CONFLICT_IGNORE, values)
            }
        }
    }
}
