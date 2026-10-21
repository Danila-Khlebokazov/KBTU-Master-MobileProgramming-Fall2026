package com.example.stylebook.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stylebook.ui.navigation.Routes
import com.example.stylebook.ui.navigation.TopLevelDestination
import com.example.stylebook.ui.screens.AddItemScreen
import com.example.stylebook.ui.screens.FavoritesScreen
import com.example.stylebook.ui.screens.HomeScreen
import com.example.stylebook.ui.screens.ItemDetailScreen
import com.example.stylebook.ui.screens.OutfitGeneratorScreen
import com.example.stylebook.ui.screens.WardrobeScreen
import com.example.stylebook.ui.viewmodel.OutfitViewModel
import com.example.stylebook.ui.viewmodel.WardrobeViewModel
import kotlinx.coroutines.flow.merge

/**
 * App shell: owns the NavController, the Activity-scoped ViewModels and the Snackbar host.
 * Screens receive plain state + callbacks (state hoisting) and never see a ViewModel directly.
 */
@Composable
fun StyleBookApp(
    wardrobeViewModel: WardrobeViewModel = viewModel(),
    outfitViewModel: OutfitViewModel = viewModel()
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val wardrobeState by wardrobeViewModel.uiState.collectAsStateWithLifecycle()
    val outfitState by outfitViewModel.uiState.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTopLevel = TopLevelDestination.entries.firstOrNull { it.route == currentRoute }

    // One-off messages (e.g. "Removed X — Undo") from both ViewModels are shown as Snackbars.
    LaunchedEffect(wardrobeViewModel, outfitViewModel) {
        merge(wardrobeViewModel.messages, outfitViewModel.messages).collect { message ->
            val result = snackbarHostState.showSnackbar(
                message = message.text,
                actionLabel = message.actionLabel,
                duration = if (message.actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) message.onAction?.invoke()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RectangleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    actionColor = MaterialTheme.colorScheme.secondary
                )
            }
        },
        bottomBar = {
            if (currentTopLevel != null) {
                StyleBookNavigationBar(
                    current = currentTopLevel,
                    onSelect = { navController.navigateToTopLevel(it.route) }
                )
            }
        },
        floatingActionButton = {
            if (currentTopLevel == TopLevelDestination.WARDROBE) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.ADD_ITEM) },
                    shape = RoundedCornerShape(0),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add a piece")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    items = wardrobeState.allItems,
                    favoriteCount = wardrobeState.favoriteItems.size,
                    savedLookCount = outfitState.savedOutfits.size,
                    todaysLook = outfitState.outfit,
                    onOpenWardrobe = {
                        wardrobeViewModel.clearFilters()
                        navController.navigateToTopLevel(Routes.WARDROBE)
                    },
                    onOpenFavorites = { navController.navigateToTopLevel(Routes.FAVORITES) },
                    onOpenGenerator = { navController.navigateToTopLevel(Routes.GENERATOR) },
                    onShuffleLook = outfitViewModel::generate,
                    onCategoryClick = { category ->
                        wardrobeViewModel.showCategory(category)
                        navController.navigateToTopLevel(Routes.WARDROBE)
                    },
                    onItemClick = { navController.navigate(Routes.itemDetail(it.id)) },
                    onAddItem = { navController.navigate(Routes.ADD_ITEM) }
                )
            }
            composable(Routes.WARDROBE) {
                WardrobeScreen(
                    state = wardrobeState,
                    onQueryChange = wardrobeViewModel::onQueryChange,
                    onCategorySelected = wardrobeViewModel::onCategorySelected,
                    onFavoritesOnlyChange = wardrobeViewModel::onFavoritesOnlyChange,
                    onClearFilters = wardrobeViewModel::clearFilters,
                    onItemClick = { navController.navigate(Routes.itemDetail(it.id)) },
                    onFavoriteClick = wardrobeViewModel::toggleFavorite,
                    onAddItem = { navController.navigate(Routes.ADD_ITEM) }
                )
            }
            composable(Routes.GENERATOR) {
                OutfitGeneratorScreen(
                    state = outfitState,
                    onOccasionSelected = outfitViewModel::onOccasionSelected,
                    onIncludeLayerChange = outfitViewModel::onIncludeLayerChange,
                    onToggleLock = outfitViewModel::toggleLock,
                    onShuffle = outfitViewModel::generate,
                    onSave = outfitViewModel::saveCurrentOutfit,
                    onPieceClick = { navController.navigate(Routes.itemDetail(it.id)) },
                    onAddItem = { navController.navigate(Routes.ADD_ITEM) }
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    favoriteItems = wardrobeState.favoriteItems,
                    savedOutfits = outfitState.savedOutfits,
                    onItemClick = { navController.navigate(Routes.itemDetail(it.id)) },
                    onFavoriteClick = wardrobeViewModel::toggleFavorite,
                    onRemoveOutfit = outfitViewModel::removeOutfit,
                    onOpenGenerator = { navController.navigateToTopLevel(Routes.GENERATOR) },
                    onOpenWardrobe = {
                        wardrobeViewModel.clearFilters()
                        navController.navigateToTopLevel(Routes.WARDROBE)
                    }
                )
            }
            composable(Routes.ADD_ITEM) {
                AddItemScreen(
                    onValidate = wardrobeViewModel::validate,
                    onSave = wardrobeViewModel::addItem,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.ITEM_DETAIL,
                arguments = listOf(navArgument(Routes.ITEM_ID_ARG) { type = NavType.IntType })
            ) { entry ->
                val itemId = entry.arguments?.getInt(Routes.ITEM_ID_ARG) ?: -1
                val item = wardrobeState.itemById(itemId)
                if (item == null) {
                    // The id is invalid or the piece was deleted elsewhere: leave the screen,
                    // but only if this entry is still on top (it may already be animating out).
                    LaunchedEffect(itemId) {
                        if (navController.currentBackStackEntry?.id == entry.id) navController.popBackStack()
                    }
                } else {
                    ItemDetailScreen(
                        item = item,
                        savedLooksWithItem = outfitState.savedOutfits.count { it.contains(item.id) },
                        onBack = { navController.popBackStack() },
                        onFavoriteClick = { wardrobeViewModel.toggleFavorite(item.id) },
                        onStyleItem = {
                            outfitViewModel.styleAround(item)
                            navController.navigateToTopLevel(Routes.GENERATOR)
                        },
                        onDelete = {
                            navController.popBackStack()
                            wardrobeViewModel.deleteItem(item.id)
                        }
                    )
                }
            }
        }
    }
}

/** Standard bottom-bar navigation: one copy of each tab, state of other tabs saved and restored. */
private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun StyleBookNavigationBar(current: TopLevelDestination, onSelect: (TopLevelDestination) -> Unit) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
        selectedTextColor = MaterialTheme.colorScheme.onBackground,
        indicatorColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = current == destination,
                onClick = { onSelect(destination) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(destination.label.uppercase(), style = MaterialTheme.typography.labelSmall) },
                colors = itemColors
            )
        }
    }
}
