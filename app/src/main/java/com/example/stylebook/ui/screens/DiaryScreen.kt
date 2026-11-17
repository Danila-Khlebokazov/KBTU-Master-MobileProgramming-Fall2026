package com.example.stylebook.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.WearEntry
import com.example.stylebook.ui.components.ClothingItemTile
import com.example.stylebook.ui.components.ConfirmDialog
import com.example.stylebook.ui.components.EmptyState
import com.example.stylebook.ui.components.OutfitPiecesStrip
import com.example.stylebook.ui.components.ScreenMasthead
import com.example.stylebook.ui.components.SectionLabel
import com.example.stylebook.ui.components.dayLabel
import com.example.stylebook.ui.viewmodel.DiaryUiState
import com.example.stylebook.ui.viewmodel.DiaryViewModel
import com.example.stylebook.ui.viewmodel.PieceWear
import java.time.LocalDate

@Composable
fun DiaryScreen(
    state: DiaryUiState,
    onItemClick: (ClothingItem) -> Unit,
    onWearAgain: (Int) -> Unit,
    onDeleteEntry: (Int) -> Unit,
    onOpenGenerator: () -> Unit
) {
    // Id of the entry waiting for confirmation; saveable so the dialog survives rotation.
    var pendingDelete by rememberSaveable { mutableStateOf<Int?>(null) }
    pendingDelete?.let { entryId ->
        ConfirmDialog(
            title = "Delete this entry?",
            text = "Only the diary entry is removed. The pieces stay in your wardrobe and saved looks are kept.",
            confirmLabel = "Delete",
            onConfirm = {
                pendingDelete = null
                onDeleteEntry(entryId)
            },
            onDismiss = { pendingDelete = null }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item(key = "masthead") {
            ScreenMasthead(
                eyebrow = "WEAR DIARY",
                title = "Diary",
                subtitle = "What you actually wore, day by day.",
                trailingEyebrow = "${state.totalEntries} ENTRIES"
            )
        }
        if (state.isLoading) return@LazyColumn
        item(key = "stats") {
            DiaryStats(state)
        }
        if (state.forgotten.isNotEmpty()) {
            item(key = "forgotten-label") {
                Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)) {
                    SectionLabel("FORGOTTEN PIECES \u00B7 ${state.forgotten.size}")
                    Text(
                        text = "Not worn in ${DiaryViewModel.FORGOTTEN_AFTER_DAYS}+ days. Tap one to give it a turn.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item(key = "forgotten") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    items(state.forgotten, key = { it.item.id }) { piece ->
                        ForgottenPieceTile(piece, today = state.today, onClick = { onItemClick(piece.item) })
                    }
                }
            }
        }
        item(key = "log-label") {
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)) {
                SectionLabel("LOG")
                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        if (state.days.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    eyebrow = "NOTHING LOGGED YET",
                    title = "Wear a look to start.",
                    body = "Tap \u201CWear today\u201D on a generated or saved look and it will appear here.",
                    actionLabel = "Open generator",
                    onAction = onOpenGenerator
                )
            }
        }
        state.days.forEach { day ->
            item(key = "day-${day.date.toEpochDay()}") {
                Text(
                    text = dayLabel(day.date, state.today).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .animateItem()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp)
                )
            }
            items(day.entries, key = { "wear-${it.id}" }) { entry ->
                WearEntryCard(
                    entry = entry,
                    canWearAgain = day.date != state.today,
                    onPieceClick = onItemClick,
                    onWearAgain = { onWearAgain(entry.id) },
                    onDelete = { pendingDelete = entry.id },
                    modifier = Modifier
                        .animateItem()
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DiaryStats(state: DiaryUiState) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
        ) {
            DiaryStat(value = state.entriesThisMonth.toString(), label = "THIS MONTH", modifier = Modifier.weight(1f))
            VerticalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 16.dp))
            DiaryStat(
                value = "${state.piecesWorn}/${state.totalPieces}",
                label = "PIECES WORN",
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 16.dp))
            DiaryStat(
                value = state.mostWorn?.let { "${it.stats.count}\u00D7" } ?: "\u2014",
                label = state.mostWorn?.item?.name?.uppercase() ?: "MOST WORN",
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun DiaryStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.padding(horizontal = 4.dp)) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ForgottenPieceTile(piece: PieceWear, today: LocalDate, onClick: () -> Unit) {
    Column {
        ClothingItemTile(item = piece.item, onClick = onClick)
        Text(
            text = piece.stats.lastWorn?.let { "Last: ${dayLabel(it, today)}" } ?: "Never worn",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun WearEntryCard(
    entry: WearEntry,
    canWearAgain: Boolean,
    onPieceClick: (ClothingItem) -> Unit,
    onWearAgain: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = RectangleShape,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.outfit.occasion?.let { "${it.displayName} look" } ?: "Any-occasion look",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                if (canWearAgain) {
                    TextButton(onClick = onWearAgain) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text(
                            " WEAR AGAIN",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete diary entry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutfitPiecesStrip(
                outfit = entry.outfit,
                onPieceClick = onPieceClick,
                modifier = Modifier.padding(end = 12.dp, top = 4.dp)
            )
        }
    }
}
