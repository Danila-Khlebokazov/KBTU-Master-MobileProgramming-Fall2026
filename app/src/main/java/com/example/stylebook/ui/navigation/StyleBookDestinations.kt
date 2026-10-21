package com.example.stylebook.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

/** All navigation routes in one place. */
object Routes {
    const val HOME = "home"
    const val WARDROBE = "wardrobe"
    const val GENERATOR = "generator"
    const val FAVORITES = "favorites"
    const val ADD_ITEM = "add_item"

    const val ITEM_ID_ARG = "itemId"
    const val ITEM_DETAIL = "item/{$ITEM_ID_ARG}"
    fun itemDetail(itemId: Int) = "item/$itemId"
}

/** Destinations shown in the bottom navigation bar. */
enum class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    HOME(Routes.HOME, "Home", Icons.Filled.Home),
    WARDROBE(Routes.WARDROBE, "Wardrobe", Icons.AutoMirrored.Filled.List),
    GENERATOR(Routes.GENERATOR, "Style Me", Icons.Filled.Star),
    FAVORITES(Routes.FAVORITES, "Favorites", Icons.Filled.Favorite)
}
