# StyleBook: Demo Script & Defense Notes

## 1. Demo script (about 5 minutes)

Start from a fresh launch so the sample wardrobe is loaded (13 pieces, 2 favorites, 0 saved looks).

| Step | Action | What to say |
|---|---|---|
| 1 | **Home** | "Home summarizes the wardrobe: piece count, favorites, saved looks, and *Today's look*, which the generator already built from my own clothes." Tap **Shuffle** to show a new look. |
| 2 | Tap the **Knitwear** category card | "Category shortcuts open the wardrobe already filtered. That's the same ViewModel, so the filter is just state." |
| 3 | **Wardrobe**: type `sage` in search, then clear it. Tap **Favorites** chip. Type `xyz`. | "Search matches name, category, color and occasion. Filters combine. When nothing matches, the empty state offers to clear filters." |
| 4 | **Rotate** the device | "Search text and filters survive rotation because they live in the ViewModel's StateFlow." Rotate back. |
| 5 | Tap **＋** (FAB) and then **Save to wardrobe** with an empty form | "Validation runs only after the first submit, and shows every missing field." |
| 6 | Type `Linen Shirt`, choose **Tops** | "Duplicate names in the same category are rejected." Change the name to `Denim Jacket`, pick **Outerwear**, **Denim**, **Casual**. |
| 7 | **Rotate** with the form half filled | "Form fields are `rememberSaveable`, so they survive Activity recreation." Save. A snackbar confirms, and the piece is at the top of the list. |
| 8 | Open **Denim Jacket**, tap **Style this piece** | "Navigation with an argument (`item/{itemId}`). The generator opens with this piece locked." |
| 9 | **Style Me**: pick **Work**, lock the top, tap **Shuffle** a few times | "Locked pieces stay. Everything else changes, and the occasion filter and color harmony (max one accent) are applied. *Why it works* explains the choice." |
| 10 | Toggle **Add a layer** off/on, then **Save look**. Tap Save again. | "Duplicate looks are detected: *already in Favorites*." |
| 11 | **Favorites**: Looks tab, then Pieces tab | "Saved looks and favorite pieces. The selected tab is `rememberSaveable`." Remove a look, then tap **Undo**. |
| 12 | Open a piece used in a saved look, tap **Remove**, then **Undo** | "Deleting a piece also removes looks that used it. Undo restores both, in the same position." |
| 13 | (Optional) Enable dark mode | "Material 3 color scheme has a dark variant." |

Honest note to mention: data is **in memory**. It survives rotation but resets when the process is restarted. Room/DataStore would be the next step, and only `WardrobeRepository` would change.

## 2. Defense notes

### Architecture (Why three layers?)
- **UI** (`ui/screens`, `ui/components`): stateless composables. They draw the state and call lambdas, with no business logic.
- **ViewModels** (`ui/viewmodel`): hold screen state as `StateFlow<UiState>` and turn UI events into repository calls.
- **Data/Domain** (`data/`, `domain/`): `WardrobeRepository` is the single source of truth. `OutfitGenerator` and `ClothingItemValidator` are pure Kotlin, so they are unit-testable without Android.
- **Unidirectional data flow**: state goes down, events go up. Because both ViewModels read the same repository flows, an item added on one screen appears everywhere automatically.
- `AppContainer` is a tiny manual dependency container. With Hilt it would become a `@Singleton` binding; it is kept simple on purpose.

### Navigation
- One `NavHost` in `StyleBookApp.kt` with string routes in `Routes`: `home`, `wardrobe`, `generator`, `favorites`, `add_item`, and `item/{itemId}`.
- `item/{itemId}` declares `navArgument("itemId") { type = NavType.IntType }`. The id is read from `backStackEntry.arguments`. Passing only the **id** (not the whole object) keeps the route small, and the screen always reads fresh data from the state.
- Bottom-bar tabs use `navigateToTopLevel()`: `popUpTo(start) { saveState = true }`, `launchSingleTop = true`, and `restoreState = true`. This avoids stacking duplicate tabs and keeps each tab's state.
- Bottom bar and FAB are shown based on `currentBackStackEntryAsState()`. They are hidden on Add/Detail, and the FAB appears only on Wardrobe.
- Detail screen safety: if the item no longer exists, it pops itself, but only if it is still the top entry. This avoids a double-pop during the exit animation.

### ViewModel
- `WardrobeViewModel`: filters (query, category, favoritesOnly), validation and add, favorite toggle, delete plus undo.
- `OutfitViewModel`: occasion, layer toggle, locks, generate/shuffle, *Style this piece*, save/remove looks with undo.
- Created with `viewModel()` in `StyleBookApp`, so they are scoped to the Activity's `ViewModelStore`. That is why they **survive rotation**: the Activity is recreated, but the store (and therefore the ViewModel) is kept.
- Screens never receive a ViewModel. They get state and lambdas, which keeps them previewable and reusable.

### StateFlow & `collectAsStateWithLifecycle()`
- Each ViewModel keeps private `MutableStateFlow`s (e.g. `filters`) and exposes one read-only `StateFlow<UiState>`:
  ```kotlin
  val uiState = combine(repository.items, filters, ::buildState)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = buildState(...))
  ```
  - `combine` recomputes the UI state whenever the items **or** the filters change.
  - `WhileSubscribed(5_000)` keeps the flow alive for 5 seconds without collectors, so a rotation does not restart it.
  - The `initialValue` is computed synchronously, so the very first frame already shows data.
- `collectAsStateWithLifecycle()` turns the flow into Compose `State` and **stops collecting when the app is in the background** (below STARTED). Plain `collectAsState()` would keep collecting.
- **One-off events** (snackbars with Undo) use `Channel(BUFFERED).receiveAsFlow()`, not StateFlow. A StateFlow would replay the last message after rotation, while a Channel delivers it exactly once. They are collected in a `LaunchedEffect` with `merge(...)`.

### Coroutines
- `viewModelScope.launch { _messages.send(...) }` is cancelled automatically when the ViewModel is cleared.
- `OutfitViewModel.init` launches `repository.items.collect { ... }`. If a piece of the current look is deleted, a new look is generated.
- Tests use `kotlinx-coroutines-test`. `MainDispatcherRule` replaces `Dispatchers.Main`, and `UnconfinedTestDispatcher` collects `uiState` so `WhileSubscribed` is active.

### `remember` vs `rememberSaveable`
| | Survives recomposition | Survives rotation / Activity recreation | Used for |
|---|---|---|---|
| `remember` | ✅ | ❌ | Home greeting, per-category counts `remember(items)`, `SnackbarHostState`, `rememberNavController()` |
| `rememberSaveable` | ✅ | ✅ (stored in the saved-instance `Bundle`) | Add form: `name`, `category`, `color`, `occasions`, `submitted`; Favorites selected tab |
| ViewModel `StateFlow` | ✅ | ✅ (not process death) | Wardrobe, filters, current outfit, locks, saved looks |

- The rule of thumb: **app/business data goes in the ViewModel**. **Short-lived UI-only state** (an unfinished form, a selected tab) goes in `rememberSaveable`. **Cheap derived or helper objects** go in `remember`.
- `rememberSaveable` works here because enums and `Set` are `Serializable`, so they fit in a `Bundle`.

### State hoisting
- A composable is stateless when its state comes from its caller. For example:
  ```kotlin
  @Composable
  fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)
  ```
- `WardrobeScreen(state, onQueryChange, onCategorySelected, onFavoritesOnlyChange, onItemClick, ...)` contains no ViewModel. `StyleBookApp` connects lambdas such as `wardrobeViewModel::onQueryChange`.
- Benefits: single source of truth, reusability (`ClothingItemRow` is used in both Wardrobe and Favorites), easy previews and tests.
- `AddItemScreen` keeps only the field values locally (`rememberSaveable`). Validation is hoisted: it calls `onValidate(form): AddItemErrors` to show inline errors and `onSave(form): AddItemResult` to save. The ViewModel (through `ClothingItemValidator`) decides validity, and the screen only displays the result.

### Configuration changes (what happens on rotation)
1. The Activity is destroyed and recreated, and Compose rebuilds the tree from scratch.
2. The ViewModels are **retained**, so the wardrobe, filters, current outfit and locks are unchanged.
3. `rememberSaveable` values are restored from the Bundle (form fields, tab). Plain `remember` values are recomputed.
4. `NavHost` restores the back stack, so you stay on the same screen with the same `itemId`.
5. **Process death** is different. The OS can restore `rememberSaveable` and the nav stack, but the in-memory repository resets to sample data. This is stated in the README.

### Outfit Generator logic (likely question)
1. Filter pieces by occasion (if one is selected). A locked piece is the only candidate for its slot.
2. Required slots: top (Tops or Knitwear category), bottom, shoes. If any slot has no candidates, return `MissingPieces(slots)`. The UI then shows e.g. *"Add shoes for work, or choose another occasion."*
3. Build every top × bottom × shoes combination and keep the **harmonious** ones (at most one accent color, the rest neutrals). If none are harmonious, fall back to all combinations.
4. Remove the previous look (so Shuffle always changes something, when possible), then pick one at random. `Random` is injected, so tests are deterministic.
5. If *Add a layer* is on, add a matching outerwear piece (harmonious if possible, different from the last one).
6. Return `Success(outfit, notes)`. The notes are the *"Why it works"* lines.

### Possible "modify the code" requests (where to change)
- **Add a category** (e.g. Accessories): add an entry to the `Category` enum (with its slot and image). The chips and filters update automatically.
- **New validation rule**: edit `ClothingItemValidator.validate()` and add an error field to `AddItemErrors`.
- **New screen**: add a route to `Routes`, a `composable(...)` in `NavHost`, and optionally a `TopLevelDestination` entry for the bottom bar.
- **Persist data**: back `WardrobeRepository` with Room. Its public `StateFlow`s become `Flow`s from DAO queries, and the ViewModels stay the same.
- **StyleProfile / weather / try-on**: see *Future extensions* in the README.
