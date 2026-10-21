package com.example.stylebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.data.model.OutfitSlot
import com.example.stylebook.ui.components.EditorialChip
import com.example.stylebook.ui.components.EmptyState
import com.example.stylebook.ui.components.OutfitBoard
import com.example.stylebook.ui.components.PrimaryActionButton
import com.example.stylebook.ui.components.ScreenMasthead
import com.example.stylebook.ui.components.SecondaryActionButton
import com.example.stylebook.ui.components.SectionLabel
import com.example.stylebook.ui.viewmodel.OutfitUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OutfitGeneratorScreen(
    state: OutfitUiState,
    onOccasionSelected: (Occasion?) -> Unit,
    onIncludeLayerChange: (Boolean) -> Unit,
    onToggleLock: (OutfitSlot) -> Unit,
    onShuffle: () -> Unit,
    onSave: () -> Unit,
    onPieceClick: (ClothingItem) -> Unit,
    onAddItem: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenMasthead(
            eyebrow = "OUTFIT GENERATOR",
            title = "Style Me",
            subtitle = "Looks built only from pieces you own."
        )
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("OCCASION")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                EditorialChip(label = "Any", selected = state.occasion == null, onClick = { onOccasionSelected(null) })
                Occasion.entries.forEach { occasion ->
                    EditorialChip(
                        label = occasion.displayName,
                        selected = state.occasion == occasion,
                        onClick = { onOccasionSelected(occasion) }
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = state.includeLayer, role = Role.Switch, onValueChange = onIncludeLayerChange)
                    .padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ADD A LAYER", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        "Include a coat or jacket when one fits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.includeLayer,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)

            val outfit = state.outfit
            if (outfit == null) {
                EmptyState(
                    eyebrow = "MISSING PIECES",
                    title = "Can't build a look yet.",
                    body = if (state.missingSlots.isEmpty()) {
                        "Tap shuffle to build a look."
                    } else {
                        val occasionText = state.occasion?.let { " for ${it.displayName.lowercase()}" } ?: ""
                        "Add ${state.missingSlots.joinToString(" and ") { it.displayName.lowercase() }}$occasionText, or choose another occasion."
                    },
                    actionLabel = if (state.missingSlots.isEmpty()) "Shuffle" else "Add a piece",
                    onAction = if (state.missingSlots.isEmpty()) onShuffle else onAddItem
                )
            } else {
                Text(
                    text = "Tap the lock to keep a piece while you shuffle the rest.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
                OutfitBoard(
                    outfit = outfit,
                    lockedSlots = state.lockedSlots,
                    onToggleLock = onToggleLock,
                    onPieceClick = onPieceClick
                )
                if (state.notes.isNotEmpty()) {
                    SectionLabel("WHY IT WORKS", modifier = Modifier.padding(top = 18.dp))
                    state.notes.forEach { note ->
                        Text(
                            text = "— $note",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryActionButton(
                        text = "Shuffle",
                        icon = Icons.Filled.Refresh,
                        onClick = onShuffle,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryActionButton(
                        text = if (state.isCurrentSaved) "Saved" else "Save look",
                        icon = if (state.isCurrentSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        onClick = onSave,
                        enabled = !state.isCurrentSaved,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
