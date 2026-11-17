package com.example.stylebook.domain

import com.example.stylebook.data.model.WearEntry
import java.time.LocalDate

/** How often a single piece appears in the wear diary. */
data class WearStats(val count: Int = 0, val lastWorn: LocalDate? = null)

/** Wear statistics per piece id, computed from the diary. Pieces never worn are absent. */
fun List<WearEntry>.wearStatsByItem(): Map<Int, WearStats> {
    val stats = mutableMapOf<Int, WearStats>()
    forEach { entry ->
        entry.outfit.pieces.forEach { piece ->
            val current = stats[piece.id] ?: WearStats()
            val last = current.lastWorn?.takeIf { it > entry.date } ?: entry.date
            stats[piece.id] = WearStats(count = current.count + 1, lastWorn = last)
        }
    }
    return stats
}
