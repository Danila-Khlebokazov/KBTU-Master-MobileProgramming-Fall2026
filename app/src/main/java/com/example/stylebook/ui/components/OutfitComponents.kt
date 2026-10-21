package com.example.stylebook.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Outfit
import com.example.stylebook.data.model.OutfitSlot

/** The generated look, head to toe. Each piece can be locked so it survives the next shuffle. */
@Composable
fun OutfitBoard(
    outfit: Outfit,
    lockedSlots: Set<OutfitSlot>,
    onToggleLock: (OutfitSlot) -> Unit,
    onPieceClick: (ClothingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutfitSlot.entries.forEach { slot ->
            val piece = outfit.pieceFor(slot) ?: return@forEach
            OutfitPieceRow(
                slot = slot,
                piece = piece,
                locked = slot in lockedSlots,
                onToggleLock = { onToggleLock(slot) },
                onClick = { onPieceClick(piece) }
            )
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun OutfitPieceRow(
    slot: OutfitSlot,
    piece: ClothingItem,
    locked: Boolean,
    onToggleLock: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        GarmentImage(item = piece, modifier = Modifier.size(width = 64.dp, height = 76.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = slot.displayName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = piece.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                ColorDot(piece.color, size = 8.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = piece.color.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onToggleLock) {
            Icon(
                imageVector = if (locked) Icons.Filled.Lock else Icons.Outlined.Lock,
                contentDescription = if (locked) "Unlock ${piece.name}" else "Keep ${piece.name} when shuffling",
                tint = if (locked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

/** A saved look shown on the Favorites screen. */
@Composable
fun SavedOutfitCard(
    outfit: Outfit,
    onPieceClick: (ClothingItem) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = RectangleShape,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LOOK NO. ${outfit.id.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = outfit.occasion?.let { "${it.displayName} look" } ?: "Any-occasion look",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove look", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                outfit.pieces.forEach { piece ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPieceClick(piece) }
                    ) {
                        GarmentImage(
                            item = piece,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.85f)
                        )
                        Text(
                            text = piece.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                // Keeps three-piece looks the same width as four-piece ones.
                repeat(4 - outfit.pieces.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}
