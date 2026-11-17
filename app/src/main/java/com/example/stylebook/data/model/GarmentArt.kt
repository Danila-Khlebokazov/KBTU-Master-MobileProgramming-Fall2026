package com.example.stylebook.data.model

import androidx.annotation.DrawableRes
import com.example.stylebook.R

/** Garment illustration. Stored by name in the database because drawable ids change between builds. */
enum class GarmentArt(@param:DrawableRes val res: Int) {
    SHIRT(R.drawable.garment_shirt),
    TEE(R.drawable.garment_tee),
    SWEATER(R.drawable.garment_sweater),
    TROUSERS(R.drawable.garment_trousers),
    JEANS(R.drawable.garment_jeans),
    COAT(R.drawable.garment_coat),
    SHOES(R.drawable.garment_shoes)
}
