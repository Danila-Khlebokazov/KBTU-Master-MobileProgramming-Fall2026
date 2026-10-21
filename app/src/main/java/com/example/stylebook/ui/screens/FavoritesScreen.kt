package com.example.stylebook.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Outfit
import com.example.stylebook.ui.components.ClothingItemRow
import com.example.stylebook.ui.components.EmptyState
import com.example.stylebook.ui.components.SavedOutfitCard
import com.example.stylebook.ui.components.ScreenMasthead

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoriteItems: List<ClothingItem>,
    savedOutfits: List<Outfit>,
    onItemClick: (ClothingItem) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onRemoveOutfit: (Int) -> Unit,
    onOpenGenerator: () -> Unit,
    onOpenWardrobe: () -> Unit
) {
    // Pure UI state: which tab is open. rememberSaveable keeps it across rotation and tab switches.
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("LOOKS (${savedOutfits.size})", "PIECES (${favoriteItems.size})")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            ScreenMasthead(eyebrow = "FAVORITES", title = "Saved", subtitle = "The looks and pieces you love most.")
        }
        item {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTab, matchContentSize = false),
                        width = 120.dp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium) },
                        selectedContentColor = MaterialTheme.colorScheme.onBackground,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (selectedTab == 0) {
            if (savedOutfits.isEmpty()) {
                item {
                    EmptyState(
                        eyebrow = "NO LOOKS YET",
                        title = "Save a look you like.",
                        body = "Generate outfits from your wardrobe and keep the best ones here.",
                        actionLabel = "Open generator",
                        onAction = onOpenGenerator
                    )
                }
            }
            items(savedOutfits, key = { "outfit-${it.id}" }) { outfit ->
                SavedOutfitCard(
                    outfit = outfit,
                    onPieceClick = onItemClick,
                    onRemove = { onRemoveOutfit(outfit.id) },
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)
                )
            }
        } else {
            if (favoriteItems.isEmpty()) {
                item {
                    EmptyState(
                        eyebrow = "NO FAVORITES YET",
                        title = "Heart the pieces you love.",
                        body = "Tap the heart on any piece in your wardrobe.",
                        actionLabel = "Browse wardrobe",
                        onAction = onOpenWardrobe
                    )
                }
            }
            items(favoriteItems, key = { "item-${it.id}" }) { item ->
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    ClothingItemRow(
                        item = item,
                        onClick = { onItemClick(item) },
                        onFavoriteClick = { onFavoriteClick(item.id) }
                    )
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
