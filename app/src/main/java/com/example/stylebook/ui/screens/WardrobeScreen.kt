package com.example.stylebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.ui.components.ClothingItemRow
import com.example.stylebook.ui.components.EditorialChip
import com.example.stylebook.ui.components.EmptyState
import com.example.stylebook.ui.components.ScreenMasthead
import com.example.stylebook.ui.viewmodel.WardrobeUiState

/** Stateless: every value comes from [WardrobeUiState] and every change is reported through a callback. */
@Composable
fun WardrobeScreen(
    state: WardrobeUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onItemClick: (ClothingItem) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onAddItem: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item(key = "masthead") {
            ScreenMasthead(
                eyebrow = "MY WARDROBE",
                title = "The Edit",
                subtitle = "Every piece you own, in one place.",
                trailingEyebrow = "${state.allItems.size} PIECES"
            )
        }
        item(key = "filters") {
            WardrobeFilterBar(
                query = state.filters.query,
                selectedCategory = state.filters.category,
                favoritesOnly = state.filters.favoritesOnly,
                categoryCounts = state.categoryCounts,
                onQueryChange = onQueryChange,
                onCategorySelected = onCategorySelected,
                onFavoritesOnlyChange = onFavoritesOnlyChange
            )
        }
        item(key = "summary") {
            ResultSummary(
                shown = state.visibleItems.size,
                total = state.allItems.size,
                hasActiveFilters = state.hasActiveFilters,
                onClearFilters = onClearFilters
            )
        }
        if (state.visibleItems.isEmpty()) {
            item(key = "empty") {
                if (state.allItems.isEmpty()) {
                    EmptyState(
                        eyebrow = "THE PAGE IS BLANK",
                        title = "Your first piece writes the story.",
                        body = "Add clothes you own to start building outfits.",
                        actionLabel = "Add a piece",
                        onAction = onAddItem
                    )
                } else {
                    EmptyState(
                        eyebrow = "NOTHING HERE",
                        title = "No pieces match.",
                        body = "Try another word or category.",
                        actionLabel = "Clear filters",
                        onAction = onClearFilters
                    )
                }
            }
        }
        items(state.visibleItems, key = { it.id }) { item ->
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

@Composable
fun WardrobeFilterBar(
    query: String,
    selectedCategory: Category?,
    favoritesOnly: Boolean,
    categoryCounts: Map<Category, Int>,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier.padding(top = 16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search name, color, occasion", style = MaterialTheme.typography.bodyLarge) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            shape = RectangleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 10.dp)
        ) {
            item {
                EditorialChip(
                    label = "Favorites",
                    selected = favoritesOnly,
                    onClick = { onFavoritesOnlyChange(!favoritesOnly) },
                    leading = { Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
            item {
                EditorialChip(label = "All", selected = selectedCategory == null, onClick = { onCategorySelected(null) })
            }
            items(Category.entries) { category ->
                EditorialChip(
                    label = "${category.displayName} ${categoryCounts[category] ?: 0}",
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(if (selectedCategory == category) null else category) }
                )
            }
        }
    }
}

@Composable
private fun ResultSummary(shown: Int, total: Int, hasActiveFilters: Boolean, onClearFilters: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(
                text = if (hasActiveFilters) "SHOWING $shown OF $total" else "ALL $total PIECES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 14.dp)
            )
            if (hasActiveFilters) {
                TextButton(onClick = onClearFilters) {
                    Text("CLEAR", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    }
}
