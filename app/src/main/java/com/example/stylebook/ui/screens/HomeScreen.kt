package com.example.stylebook.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Outfit
import com.example.stylebook.ui.components.ClothingItemTile
import com.example.stylebook.ui.components.GarmentImage
import com.example.stylebook.ui.components.PrimaryActionButton
import com.example.stylebook.ui.components.ScreenMasthead
import com.example.stylebook.ui.components.SecondaryActionButton
import com.example.stylebook.ui.components.SectionLabel
import java.util.Calendar

@Composable
fun HomeScreen(
    items: List<ClothingItem>,
    favoriteCount: Int,
    savedLookCount: Int,
    todaysLook: Outfit?,
    onOpenWardrobe: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenGenerator: () -> Unit,
    onShuffleLook: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onItemClick: (ClothingItem) -> Unit,
    onAddItem: () -> Unit
) {
    // Computed once per composition entry; there is no need to recalculate it on every recomposition.
    val greeting = remember { greetingFor(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    val categoryCounts = remember(items) { items.groupingBy { it.category }.eachCount() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            ScreenMasthead(
                eyebrow = greeting,
                title = "Stylebook",
                subtitle = "Your wardrobe, edited.",
                trailingEyebrow = "NO. ${items.size.toString().padStart(2, '0')}"
            )
        }
        item {
            StatsRow(
                pieces = items.size,
                favorites = favoriteCount,
                looks = savedLookCount,
                onPiecesClick = onOpenWardrobe,
                onFavoritesClick = onOpenFavorites
            )
        }
        item {
            TodaysLookCard(
                outfit = todaysLook,
                onOpen = onOpenGenerator,
                onShuffle = onShuffleLook,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
        item {
            SectionLabel(
                text = "BROWSE BY CATEGORY",
                modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 12.dp)
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(Category.entries) { category ->
                    CategoryCard(
                        category = category,
                        count = categoryCounts[category] ?: 0,
                        onClick = { onCategoryClick(category) }
                    )
                }
            }
        }
        if (items.isNotEmpty()) {
            item {
                SectionLabel(
                    text = "RECENTLY ADDED",
                    actionLabel = "SEE ALL",
                    onAction = onOpenWardrobe,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 20.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(items.take(6), key = { it.id }) { item ->
                        ClothingItemTile(item = item, onClick = { onItemClick(item) })
                    }
                }
            }
        }
        item {
            PrimaryActionButton(
                text = "Add a piece",
                icon = Icons.Filled.Add,
                onClick = onAddItem,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 28.dp)
            )
        }
    }
}

@Composable
private fun StatsRow(
    pieces: Int,
    favorites: Int,
    looks: Int,
    onPiecesClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
        ) {
            StatBlock(value = pieces, label = "PIECES", onClick = onPiecesClick, modifier = Modifier.weight(1f))
            VerticalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 14.dp))
            StatBlock(value = favorites, label = "FAVORITES", onClick = onFavoritesClick, modifier = Modifier.weight(1f))
            VerticalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 14.dp))
            StatBlock(value = looks, label = "SAVED LOOKS", onClick = onFavoritesClick, modifier = Modifier.weight(1f))
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun StatBlock(value: Int, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodaysLookCard(
    outfit: Outfit?,
    onOpen: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onOpen,
        shape = RectangleShape,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("TODAY'S LOOK", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Text(
                text = if (outfit != null) "Ready when you are" else "Not enough pieces yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (outfit != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    outfit.pieces.forEach { piece ->
                        GarmentImage(
                            item = piece,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                        )
                    }
                    repeat(4 - outfit.pieces.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
                Text(
                    text = outfit.pieces.joinToString(" · ") { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
            } else {
                Text(
                    text = "A look needs a top, a bottom and shoes. Add the missing pieces to start styling.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 14.dp)
            ) {
                SecondaryActionButton(
                    text = "Shuffle",
                    icon = Icons.Filled.Refresh,
                    onClick = onShuffle,
                    enabled = outfit != null,
                    modifier = Modifier.weight(1f)
                )
                PrimaryActionButton(text = "Style me", onClick = onOpen, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CategoryCard(category: Category, count: Int, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        shape = RectangleShape,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.width(112.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = count.toString().padStart(2, '0'),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = category.displayName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun greetingFor(hour: Int): String = when (hour) {
    in 5..11 -> "GOOD MORNING"
    in 12..17 -> "GOOD AFTERNOON"
    else -> "GOOD EVENING"
}
