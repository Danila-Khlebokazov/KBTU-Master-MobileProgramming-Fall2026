package com.example.stylebook.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingColor
import com.example.stylebook.data.model.GarmentArt
import com.example.stylebook.data.model.Occasion
import java.time.LocalDate

@Entity(tableName = "clothing_items")
data class ClothingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: Category,
    val color: ClothingColor,
    val occasions: Set<Occasion>,
    val art: GarmentArt,
    val isFavorite: Boolean = false,
    val addedAt: Long
)

/** A look saved to Favorites. It is deleted automatically (CASCADE) when one of its pieces is deleted. */
@Entity(
    tableName = "saved_outfits",
    foreignKeys = [
        ForeignKey(ClothingItemEntity::class, ["id"], ["topId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(ClothingItemEntity::class, ["id"], ["bottomId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(ClothingItemEntity::class, ["id"], ["shoesId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(ClothingItemEntity::class, ["id"], ["outerwearId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("topId"), Index("bottomId"), Index("shoesId"), Index("outerwearId")]
)
data class SavedOutfitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topId: Int,
    val bottomId: Int,
    val shoesId: Int,
    val outerwearId: Int?,
    val occasion: Occasion?
)

/** A wear-diary entry: the pieces worn on [wornOn]. Removed (CASCADE) together with any of its pieces. */
@Entity(
    tableName = "wear_log",
    foreignKeys = [
        ForeignKey(ClothingItemEntity::class, ["id"], ["topId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(ClothingItemEntity::class, ["id"], ["bottomId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(ClothingItemEntity::class, ["id"], ["shoesId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(ClothingItemEntity::class, ["id"], ["outerwearId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("topId"), Index("bottomId"), Index("shoesId"), Index("outerwearId"), Index("wornOn")]
)
data class WearLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wornOn: LocalDate,
    val topId: Int,
    val bottomId: Int,
    val shoesId: Int,
    val outerwearId: Int?,
    val occasion: Occasion?
)
