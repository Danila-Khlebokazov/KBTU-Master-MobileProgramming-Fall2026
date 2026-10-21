package com.example.stylebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.ui.components.BackHeader
import com.example.stylebook.ui.components.ColorDot
import com.example.stylebook.ui.components.GarmentImage
import com.example.stylebook.ui.components.PrimaryActionButton
import com.example.stylebook.ui.components.SecondaryActionButton

@Composable
fun ItemDetailScreen(
    item: ClothingItem,
    savedLooksWithItem: Int,
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit,
    onStyleItem: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        BackHeader(label = "BACK", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            GarmentImage(
                item = item,
                innerPadding = 48.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            )
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = item.category.displayName.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                SpecRow(label = "COLOR") {
                    ColorDot(item.color, size = 12.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    SpecValue(item.color.displayName + if (item.color.isNeutral) " (neutral)" else " (accent)")
                }
                SpecRow(label = "WEAR FOR") {
                    SpecValue(item.occasions.sorted().joinToString(", ") { it.displayName })
                }
                SpecRow(label = "SAVED LOOKS") {
                    SpecValue(if (savedLooksWithItem == 0) "Not in a look yet" else "In $savedLooksWithItem saved look(s)")
                }

                Spacer(modifier = Modifier.height(24.dp))
                PrimaryActionButton(
                    text = "Style this piece",
                    icon = Icons.Filled.Star,
                    onClick = onStyleItem,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryActionButton(
                        text = if (item.isFavorite) "Favorite" else "Favorite it",
                        icon = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentColor = if (item.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                        onClick = onFavoriteClick,
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryActionButton(
                        text = "Remove",
                        icon = Icons.Filled.Delete,
                        contentColor = MaterialTheme.colorScheme.error,
                        onClick = onDelete,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: @Composable () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) { value() }
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun SpecValue(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
}
