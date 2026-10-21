package com.example.stylebook.data.model

/**
 * A curated palette. [isNeutral] drives the outfit generator's color-harmony rule:
 * a look may contain any number of neutrals but at most one accent color.
 */
enum class ClothingColor(val displayName: String, val argb: Long, val isNeutral: Boolean) {
    IVORY("Ivory", 0xFFEDE6D7, true),
    SAND("Sand", 0xFFC9B08C, true),
    CAMEL("Camel", 0xFFB08654, true),
    BROWN("Brown", 0xFF6E4D3A, true),
    GREY("Grey", 0xFF9C9891, true),
    CHARCOAL("Charcoal", 0xFF4A4743, true),
    BLACK("Black", 0xFF2A2825, true),
    NAVY("Navy", 0xFF34405A, true),
    DENIM("Denim", 0xFF5B6F8C, true),
    SAGE("Sage", 0xFF8E9C83, false),
    OLIVE("Olive", 0xFF6D6B3F, false),
    BURGUNDY("Burgundy", 0xFF7A2E35, false),
    RUST("Rust", 0xFFA4502F, false),
    BLUSH("Blush", 0xFFDDB0A4, false)
}
