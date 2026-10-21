package com.example.stylebook.ui.viewmodel

/** One-off feedback shown in a Snackbar (not part of screen state, so it is not re-shown after rotation). */
data class UserMessage(
    val text: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)
