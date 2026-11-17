package com.example.stylebook.data.model

import java.time.LocalDate

/** One day in the wear diary: the look that was actually worn on [date]. */
data class WearEntry(
    val id: Int,
    val date: LocalDate,
    val outfit: Outfit
)
