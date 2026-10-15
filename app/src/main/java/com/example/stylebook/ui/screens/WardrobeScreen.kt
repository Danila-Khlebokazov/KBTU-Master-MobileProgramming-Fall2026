package com.example.stylebook.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.ui.clickableNoRipple

@Composable
fun WardrobeScreen(
    items: List<ClothingItem>,
    onItemClick: (ClothingItem) -> Unit,
    onFavoriteClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WardrobeHeader(itemCount = items.size, favoriteCount = items.count { it.isFavorite })
        if (items.isEmpty()) {
            EmptyWardrobeMessage()
        } else {
            ClothingItemList(
                items = items,
                onItemClick = onItemClick,
                onFavoriteClick = onFavoriteClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WardrobeHeader(itemCount: Int, favoriteCount: Int) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "THE EDIT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "NO. ${itemCount.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Stylebook",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "A curated wardrobe journal",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$itemCount PIECES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$favoriteCount SAVED",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun ClothingItemList(
    items: List<ClothingItem>,
    onItemClick: (ClothingItem) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 4.dp,
            bottom = 28.dp
        )
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            ClothingItemCard(
                item = item,
                index = index + 1,
                onClick = { onItemClick(item) },
                onFavoriteClick = { onFavoriteClick(item.id) }
            )
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun ClothingItemCard(
    item: ClothingItem,
    index: Int,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple(onClick)
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(38.dp)
        )
        Box(
            modifier = Modifier
                .size(width = 76.dp, height = 92.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Image(
                painter = painterResource(item.imageRes),
                contentDescription = "${item.name} illustration",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = item.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(colorFor(item.color))
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = item.color,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        FavoriteButton(isFavorite = item.isFavorite, onClick = onFavoriteClick)
    }
}

@Composable
fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isFavorite) "\u2605" else "\u2606",
            color = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 22.sp
        )
    }
}

@Composable
private fun EmptyWardrobeMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "THE PAGE IS BLANK",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Your first piece\nwrites the story.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

private fun colorFor(colorName: String): Color = when (colorName.lowercase()) {
    "ivory", "cream", "white" -> Color(0xFFEDE6D7)
    "indigo", "navy", "blue" -> Color(0xFF3C4A63)
    "sand", "beige", "tan" -> Color(0xFFC5A982)
    "sage", "green" -> Color(0xFF7E8B76)
    "black", "charcoal" -> Color(0xFF2A2825)
    "red", "burgundy" -> Color(0xFF8E3B34)
    else -> Color(0xFF9A8576)
}
