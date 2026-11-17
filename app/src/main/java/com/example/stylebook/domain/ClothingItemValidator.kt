package com.example.stylebook.domain

import com.example.stylebook.data.model.Category
import com.example.stylebook.data.model.ClothingColor
import com.example.stylebook.data.model.ClothingItem
import com.example.stylebook.data.model.Occasion

/** Raw values typed or selected on the add / edit piece form. */
data class ItemForm(
    val name: String = "",
    val category: Category? = null,
    val color: ClothingColor? = null,
    val occasions: Set<Occasion> = emptySet()
)

data class ItemFormErrors(
    val name: String? = null,
    val category: String? = null,
    val color: String? = null,
    val occasions: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(name, category, color, occasions).any { it != null }
}

object ClothingItemValidator {
    const val MAX_NAME_LENGTH = 30

    /** [editingId] is the piece being edited; it must not count as its own duplicate. */
    fun validate(form: ItemForm, existingItems: List<ClothingItem>, editingId: Int? = null): ItemFormErrors {
        val name = form.name.trim()
        val nameError = when {
            name.isEmpty() -> "Give the piece a name"
            name.length < 2 -> "Name is too short"
            name.length > MAX_NAME_LENGTH -> "Keep it under $MAX_NAME_LENGTH characters"
            existingItems.any {
                it.id != editingId && it.name.equals(name, ignoreCase = true) && it.category == form.category
            } -> "\u201C$name\u201D is already in ${form.category?.displayName}"
            else -> null
        }
        return ItemFormErrors(
            name = nameError,
            category = if (form.category == null) "Choose a category" else null,
            color = if (form.color == null) "Pick a color" else null,
            occasions = if (form.occasions.isEmpty()) "Choose at least one occasion" else null
        )
    }
}
