package com.example.stylebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.example.stylebook.ui.clickableNoRipple
import com.example.stylebook.ui.viewmodel.AddItemResult

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddItemScreen(onAddItem: (String, String, String) -> AddItemResult) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf("") }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var feedbackIsError by rememberSaveable { mutableStateOf(false) }
    val categories = remember { listOf("Tops", "Bottoms", "Outerwear", "Knitwear", "Shoes") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "CONTRIBUTE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "New Entry",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Record a piece for the collection.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))

        FieldLabel("01 — NAME")
        EditorialField(
            value = name,
            onValueChange = { name = it; feedback = null },
            placeholder = "e.g. Linen shirt"
        )

        Spacer(modifier = Modifier.height(20.dp))
        FieldLabel("02 — CATEGORY")
        EditorialField(
            value = category,
            onValueChange = { category = it; feedback = null },
            placeholder = "e.g. Tops"
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { suggestion ->
                CategoryChip(
                    label = suggestion,
                    selected = category.equals(suggestion, ignoreCase = true),
                    onClick = { category = suggestion; feedback = null }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        FieldLabel("03 — COLOR")
        EditorialField(
            value = color,
            onValueChange = { color = it; feedback = null },
            placeholder = "e.g. Ivory"
        )

        feedback?.let { message ->
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (feedbackIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (feedbackIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = {
                when (onAddItem(name, category, color)) {
                    AddItemResult.ADDED -> {
                        name = ""
                        category = ""
                        color = ""
                        feedback = "Added to the collection."
                        feedbackIsError = false
                    }
                    AddItemResult.INVALID_INPUT -> {
                        feedback = "Please complete name, category, and color."
                        feedbackIsError = true
                    }
                }
            },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("ADD TO WARDROBE", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun EditorialField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, style = MaterialTheme.typography.bodyLarge)
        },
        singleLine = true,
        shape = RectangleShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .background(bg)
            .clickableNoRipple(onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = fg)
    }
}
