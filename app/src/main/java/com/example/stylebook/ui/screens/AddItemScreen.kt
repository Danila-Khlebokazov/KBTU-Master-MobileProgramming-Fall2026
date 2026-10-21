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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingColor
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Occasion
import com.example.stylebook.domain.AddItemErrors
import com.example.stylebook.domain.AddItemForm
import com.example.stylebook.domain.ClothingItemValidator
import com.example.stylebook.ui.components.BackHeader
import com.example.stylebook.ui.components.ColorChoice
import com.example.stylebook.ui.components.EditorialChip
import com.example.stylebook.ui.components.GarmentImage
import com.example.stylebook.ui.components.PrimaryActionButton
import com.example.stylebook.ui.components.ScreenMasthead
import com.example.stylebook.ui.viewmodel.AddItemResult

/**
 * Form values are local UI state kept with [rememberSaveable], so a half-filled form survives rotation.
 * Validation and saving are delegated to the ViewModel through [onValidate] and [onSave].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddItemScreen(
    onValidate: (AddItemForm) -> AddItemErrors,
    onSave: (AddItemForm) -> AddItemResult,
    onSaved: (ClothingItem) -> Unit,
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<Category?>(null) }
    var color by rememberSaveable { mutableStateOf<ClothingColor?>(null) }
    var occasions by rememberSaveable { mutableStateOf(emptySet<Occasion>()) }
    // Errors are only shown after the first save attempt, then update live as the user fixes them.
    var submitted by rememberSaveable { mutableStateOf(false) }

    val form = AddItemForm(name, category, color, occasions)
    val errors = if (submitted) onValidate(form) else AddItemErrors()
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        BackHeader(label = "BACK", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ScreenMasthead(eyebrow = "ADD TO WARDROBE", title = "New Piece", subtitle = "Record something you own.")

            PiecePreview(form = form, modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                FieldLabel("01 — NAME", errors.name)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(ClothingItemValidator.MAX_NAME_LENGTH + 5) },
                    placeholder = { Text("e.g. Linen shirt", style = MaterialTheme.typography.bodyLarge) },
                    isError = errors.name != null,
                    supportingText = {
                        Text("${name.trim().length} / ${ClothingItemValidator.MAX_NAME_LENGTH}")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    shape = RectangleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                FieldLabel("02 — CATEGORY", errors.category)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.forEach { option ->
                        EditorialChip(
                            label = option.displayName,
                            selected = category == option,
                            onClick = { category = option }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                FieldLabel("03 — COLOR", errors.color)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ClothingColor.entries.forEach { option ->
                        ColorChoice(color = option, selected = color == option, onClick = { color = option })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                FieldLabel("04 — WHEN DO YOU WEAR IT?", errors.occasions)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Occasion.entries.forEach { option ->
                        EditorialChip(
                            label = option.displayName,
                            selected = option in occasions,
                            onClick = { occasions = if (option in occasions) occasions - option else occasions + option }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                PrimaryActionButton(
                    text = "Save to wardrobe",
                    onClick = {
                        focusManager.clearFocus()
                        when (val result = onSave(form)) {
                            is AddItemResult.Added -> onSaved(result.item)
                            is AddItemResult.Invalid -> submitted = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (errors.hasErrors) {
                    Text(
                        text = "Please fix the highlighted fields.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PiecePreview(form: AddItemForm, modifier: Modifier = Modifier) {
    val previewCategory = form.category ?: Category.TOPS
    val previewItem = ClothingItem(
        id = 0,
        name = form.name.trim().ifEmpty { "Untitled piece" },
        category = previewCategory,
        color = form.color ?: ClothingColor.IVORY,
        occasions = form.occasions,
        imageRes = previewCategory.defaultImageRes
    )
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        GarmentImage(item = previewItem, innerPadding = 10.dp, modifier = Modifier.size(width = 96.dp, height = 112.dp))
        Column(modifier = Modifier.padding(start = 18.dp)) {
            Text("PREVIEW", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Text(
                text = previewItem.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = listOfNotNull(form.category?.displayName, form.color?.displayName).joinToString(" · ")
                    .ifEmpty { "Choose a category and color" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String, error: String?) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (error != null) {
            Text(error, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
