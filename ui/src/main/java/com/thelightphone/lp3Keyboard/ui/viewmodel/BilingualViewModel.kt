package com.thelightphone.lp3Keyboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thelightphone.lp3Keyboard.ui.KeyboardOptions
import com.thelightphone.lp3Keyboard.ui.LayoutOptions
import com.thelightphone.lp3Keyboard.ui.Lp3KeyboardSwipeCallback
import com.thelightphone.lp3Keyboard.ui.SpecialKey
import com.thelightphone.lp3Keyboard.ui.layout.Layout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * A keyboard that holds several single-language keyboards and switches between them with the
 * [SpecialKey.Language] key in the bottom row.
 *
 * Each language keeps its own view model untouched (its own caps state, long-press popups,
 * numbers/symbols pages); this class only forwards input to whichever one is active and mirrors
 * that one's layout to the UI. Switching always lands on the new language's letters page.
 */
class BilingualLp3KeyboardViewModel<SwipeResult>(
    private val languages: List<Language<SwipeResult>>,
    private val haptic: () -> Unit = {},
    initialIndex: Int = 0,
    /** Called with the new language's index after every switch, e.g. to remember it. */
    private val onLanguageChanged: (Int) -> Unit = {},
) : ViewModel(), Lp3KeyboardViewModel<SwipeResult> {

    /**
     * One language. [switchLabel] is shown on the language key *while this language is active*,
     * so it names where the key takes you (e.g. "ع" on the English keyboard).
     */
    class Language<SwipeResult>(
        val viewModel: Lp3KeyboardViewModel<SwipeResult>,
        val switchLabel: String,
    )

    init {
        require(languages.size >= 2) { "A bilingual keyboard needs at least two languages" }
    }

    var activeIndex: Int = initialIndex.coerceIn(languages.indices)
        private set

    private val active: Lp3KeyboardViewModel<SwipeResult>
        get() = languages[activeIndex].viewModel

    /** Last caps request from the IME, replayed to a language when it becomes active. */
    private var lastCapsRequest: Boolean? = null

    override val layoutFlow = MutableStateFlow(active.layoutFlow.value)
    override val layoutOptionsFlow = MutableStateFlow(active.layoutOptionsFlow.value)
    override val keyboardOptionsFlow: StateFlow<KeyboardOptions>
        get() = mutableKeyboardOptions
    private val mutableKeyboardOptions = MutableStateFlow(currentKeyboardOptions())

    init {
        // Keyboard settings (emoji row, voice key, animation) can change while no key is touched.
        languages.forEach { language ->
            viewModelScope.launch {
                language.viewModel.keyboardOptionsFlow.collect {
                    mutableKeyboardOptions.value = currentKeyboardOptions()
                }
            }
        }
    }

    private fun currentKeyboardOptions(): KeyboardOptions =
        active.keyboardOptionsFlow.value.copy(languageKeyLabel = languages[activeIndex].switchLabel)

    /** Every layout change in the child view models happens synchronously inside a callback. */
    private inline fun <T> forward(block: Lp3KeyboardViewModel<SwipeResult>.() -> T): T {
        val result = active.block()
        sync()
        return result
    }

    private fun sync() {
        layoutFlow.value = active.layoutFlow.value
        layoutOptionsFlow.value = active.layoutOptionsFlow.value
        mutableKeyboardOptions.value = currentKeyboardOptions()
    }

    /** Moves to the next language, on its letters page. */
    fun switchLanguage() {
        active.cancelHeldKeys()
        activeIndex = (activeIndex + 1) % languages.size
        // Land on letters even if the new language was last left on numbers/symbols/emoji.
        active.onSpecialKeyReleased(SpecialKey.Letters)
        lastCapsRequest?.let { active.setCapsMode(it) }
        sync()
        onLanguageChanged(activeIndex)
    }

    // --- Lp3KeyboardCallback ---

    override fun onKeyPressed(code: Int) = forward { onKeyPressed(code) }
    override fun onKeyReleased(code: Int) = forward { onKeyReleased(code) }
    override fun onKeyCancelled(code: Int) = forward { onKeyCancelled(code) }
    override fun onKeyLongPressed(code: Int) = forward { onKeyLongPressed(code) }
    override fun onSubmitWord(word: CharSequence) = forward { onSubmitWord(word) }

    override fun onSpecialKeyPressed(key: SpecialKey) {
        if (key == SpecialKey.Language) {
            haptic()
            return
        }
        forward { onSpecialKeyPressed(key) }
    }

    override fun onSpecialKeyReleased(key: SpecialKey) {
        if (key == SpecialKey.Language) {
            switchLanguage()
            return
        }
        forward { onSpecialKeyReleased(key) }
    }

    override fun onSpecialKeyLongPressed(key: SpecialKey) {
        // Holding the language key does nothing extra; the switch happens on release.
        if (key == SpecialKey.Language) return
        forward { onSpecialKeyLongPressed(key) }
    }

    // --- Lp3KeyboardSwipeCallback ---

    override fun onSwipeLayoutReady(letters: String, cx: FloatArray, cy: FloatArray) =
        forward { onSwipeLayoutReady(letters, cx, cy) }

    override fun onSwipeStarted() = forward { onSwipeStarted() }

    override fun onSwipeCompleted(x: FloatArray, y: FloatArray, t: FloatArray): List<SwipeResult> =
        forward { onSwipeCompleted(x, y, t) }

    override fun getWordForResult(swipeResult: SwipeResult): CharSequence? =
        active.getWordForResult(swipeResult)

    // --- Lp3KeyboardViewModel ---

    override fun cancelHeldKeys() {
        languages.forEach { it.viewModel.cancelHeldKeys() }
    }

    override fun setCapsMode(enabled: Boolean) {
        lastCapsRequest = enabled
        forward { setCapsMode(enabled) }
    }

    override fun onCleared() {
        super.onCleared()
        cancelHeldKeys()
    }

    companion object {
        const val ENGLISH = 0
        const val ARABIC = 1

        /** English QWERTY + Arabic, switched with a "ع" / "EN" key. */
        fun <SwipeResult> englishArabic(
            passedCallback: Lp3RepeatableKeyboardCallback,
            swipeCallback: Lp3KeyboardSwipeCallback<SwipeResult>? = null,
            haptic: () -> Unit = {},
            optionsForLayout: (Layout) -> LayoutOptions = {
                LayoutOptions(displayCloseButton = true)
            },
            keyboardOptionsFlow: StateFlow<KeyboardOptions> = MutableStateFlow(
                KeyboardOptions(
                    defaultEmojis,
                    displayReturn = true,
                    displayVoice = true,
                    enableKeyAnimation = true,
                    swipeEnabled = false
                )
            ),
            initialIndex: Int = ENGLISH,
            onLanguageChanged: (Int) -> Unit = {},
        ): BilingualLp3KeyboardViewModel<SwipeResult> = BilingualLp3KeyboardViewModel(
            languages = listOf(
                Language(
                    EnQwertyLp3KeyboardViewModel(
                        passedCallback, swipeCallback, haptic, optionsForLayout, keyboardOptionsFlow
                    ),
                    switchLabel = "ع"
                ),
                Language(
                    ArabicLp3KeyboardViewModel(
                        passedCallback, swipeCallback, haptic, optionsForLayout, keyboardOptionsFlow
                    ),
                    switchLabel = "EN"
                ),
            ),
            haptic = haptic,
            initialIndex = initialIndex,
            onLanguageChanged = onLanguageChanged,
        )
    }
}
