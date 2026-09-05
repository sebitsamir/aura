package com.aura.core.model

/**
 * User-selectable AURA appearance modes.
 *
 * SYSTEM      - Follows Android light/dark appearance.
 * OBSIDIAN    - AURA's primary premium dark appearance.
 * IVORY       - Warm premium light appearance.
 * AMOLED      - True-black dark appearance.
 * ATMOSPHERE  - Obsidian foundation with album-derived atmosphere.
 */
enum class AuraAppearance {
    SYSTEM,
    OBSIDIAN,
    IVORY,
    AMOLED,
    ATMOSPHERE;

    companion object {
        fun fromStoredValue(value: String?): AuraAppearance {
            if (value.isNullOrBlank()) {
                return OBSIDIAN
            }

            return values().firstOrNull { appearance ->
                appearance.name.equals(value, ignoreCase = true)
            } ?: OBSIDIAN
        }
    }
}