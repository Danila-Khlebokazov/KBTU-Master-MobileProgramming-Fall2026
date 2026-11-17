package com.example.stylebook.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingItemDao {
    @Query("SELECT * FROM clothing_items ORDER BY addedAt DESC, id DESC")
    fun observeAll(): Flow<List<ClothingItemEntity>>

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    suspend fun getById(id: Int): ClothingItemEntity?

    @Insert
    suspend fun insert(item: ClothingItemEntity): Long

    @Update
    suspend fun update(item: ClothingItemEntity)

    @Query("UPDATE clothing_items SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Int)

    @Query("DELETE FROM clothing_items WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}

@Dao
interface SavedOutfitDao {
    @Query("SELECT * FROM saved_outfits ORDER BY id DESC")
    fun observeAll(): Flow<List<SavedOutfitEntity>>

    @Query("SELECT * FROM saved_outfits")
    suspend fun getAll(): List<SavedOutfitEntity>

    @Query("SELECT * FROM saved_outfits WHERE :itemId IN (topId, bottomId, shoesId, outerwearId)")
    suspend fun getContaining(itemId: Int): List<SavedOutfitEntity>

    @Insert
    suspend fun insert(outfit: SavedOutfitEntity): Long

    @Insert
    suspend fun insertAll(outfits: List<SavedOutfitEntity>)

    @Query("DELETE FROM saved_outfits WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("DELETE FROM saved_outfits WHERE :itemId IN (topId, bottomId, shoesId, outerwearId)")
    suspend fun deleteContaining(itemId: Int)
}

@Dao
interface WearLogDao {
    @Query("SELECT * FROM wear_log ORDER BY wornOn DESC, id DESC")
    fun observeAll(): Flow<List<WearLogEntity>>

    @Query("SELECT * FROM wear_log WHERE wornOn = :date")
    suspend fun getOn(date: LocalDate): List<WearLogEntity>

    @Query("SELECT * FROM wear_log WHERE :itemId IN (topId, bottomId, shoesId, outerwearId)")
    suspend fun getContaining(itemId: Int): List<WearLogEntity>

    @Query("SELECT * FROM wear_log WHERE id = :id")
    suspend fun getById(id: Int): WearLogEntity?

    @Insert
    suspend fun insert(entry: WearLogEntity): Long

    @Insert
    suspend fun insertAll(entries: List<WearLogEntity>)

    @Query("DELETE FROM wear_log WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("DELETE FROM wear_log WHERE :itemId IN (topId, bottomId, shoesId, outerwearId)")
    suspend fun deleteContaining(itemId: Int)
}
