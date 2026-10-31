package com.thelightphone.lp3keyboard

import android.content.Context
import com.thelightphone.lp3Keyboard.ui.layout.LayoutRegistryItem

/**
 * Persistent storage for the keyboard app
 * Right now, values in here only affect the android system keyboard, NOT those embedded in
 * LightOS/community tools.
 */
object LayoutPreferences {
    private const val PREFS_NAME = "lp3_keyboard_prefs"
    const val KEY_ACTIVE_LAYOUT = "active_layout_id"

    private val DEFAULT_LAYOUT = LayoutRegistryItem.EnQwerty

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getActiveLayout(context: Context): LayoutRegistryItem {
        val id = prefs(context).getString(KEY_ACTIVE_LAYOUT, null)
        return LayoutRegistryItem.entries.firstOrNull { it.uniqueId == id } ?: DEFAULT_LAYOUT
    }

    fun setActiveLayout(context: Context, item: LayoutRegistryItem) {
        prefs(context).edit().putString(KEY_ACTIVE_LAYOUT, item.uniqueId).apply()
    }

    /** Which language a bilingual layout was last left on, so it reopens there. */
    private const val KEY_BILINGUAL_LANGUAGE = "bilingual_language_index"

    fun getBilingualLanguage(context: Context): Int =
        prefs(context).getInt(KEY_BILINGUAL_LANGUAGE, 0)

    fun setBilingualLanguage(context: Context, index: Int) {
        prefs(context).edit().putInt(KEY_BILINGUAL_LANGUAGE, index).apply()
    }

    fun registerOnChange(
        context: Context,
        listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener,
    ): android.content.SharedPreferences =
        prefs(context).also { it.registerOnSharedPreferenceChangeListener(listener) }
}
