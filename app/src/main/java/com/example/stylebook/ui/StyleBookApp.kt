package com.example.stylebook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stylebook.ui.screens.AddItemScreen
import com.example.stylebook.ui.screens.ItemDetailScreen
import com.example.stylebook.ui.screens.WardrobeScreen
import com.example.stylebook.ui.viewmodel.WardrobeViewModel

private const val WARDROBE = "wardrobe"
private const val ADD_ITEM = "add"
private const val ITEM_DETAILS = "details"

@Composable
fun StyleBookApp(viewModel: WardrobeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf(WARDROBE) }
    var selectedItemId by rememberSaveable { mutableIntStateOf(-1) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (destination != ITEM_DETAILS) {
                EditorialTabBar(
                    current = destination,
                    onSelect = { destination = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (destination) {
                ADD_ITEM -> AddItemScreen(onAddItem = viewModel::addItem)
                ITEM_DETAILS -> {
                    val item = uiState.items.firstOrNull { it.id == selectedItemId }
                    if (item == null) {
                        destination = WARDROBE
                    } else {
                        ItemDetailScreen(
                            item = item,
                            outfitPartner = uiState.items.firstOrNull {
                                it.id != item.id && it.category != item.category
                            },
                            onBack = { destination = WARDROBE },
                            onFavoriteClick = { viewModel.toggleFavorite(item.id) }
                        )
                    }
                }
                else -> WardrobeScreen(
                    items = uiState.items,
                    onItemClick = { item ->
                        selectedItemId = item.id
                        destination = ITEM_DETAILS
                    },
                    onFavoriteClick = viewModel::toggleFavorite
                )
            }
        }
    }
}

@Composable
private fun EditorialTabBar(current: String, onSelect: (String) -> Unit) {
    Column {
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        Row(modifier = Modifier.fillMaxWidth()) {
            EditorialTab(
                label = "WARDROBE",
                selected = current == WARDROBE,
                onClick = { onSelect(WARDROBE) },
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            EditorialTab(
                label = "ADD PIECE",
                selected = current == ADD_ITEM,
                onClick = { onSelect(ADD_ITEM) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EditorialTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .height(56.dp)
            .background(bg)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
