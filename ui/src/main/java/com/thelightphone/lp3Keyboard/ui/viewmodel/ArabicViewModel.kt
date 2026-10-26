package com.thelightphone.lp3Keyboard.ui.viewmodel

import com.thelightphone.lp3Keyboard.ui.KeyboardOptions
import com.thelightphone.lp3Keyboard.ui.LayoutOptions
import com.thelightphone.lp3Keyboard.ui.Lp3KeyboardSwipeCallback
import com.thelightphone.lp3Keyboard.ui.layout.Arabic
import com.thelightphone.lp3Keyboard.ui.layout.Layout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Arabic keyboard. Reuses the shared LP3 key handling (long-press popups, key repeat, page
 * switching); Arabic has no case, so the lower/upper/caps-locked slots all point at the one
 * letters page.
 */
class ArabicLp3KeyboardViewModel<SwipeResult>(
    passedCallback: Lp3RepeatableKeyboardCallback,
    swipeCallback: Lp3KeyboardSwipeCallback<SwipeResult>? = null,
    haptic: () -> Unit = {},
    optionsForLayout: (Layout) -> LayoutOptions = {
        LayoutOptions(
            displayCloseButton = true
        )
    },
    keyboardOptionsFlow: StateFlow<KeyboardOptions> = MutableStateFlow(
        KeyboardOptions(
            defaultEmojis,
            displayReturn = true,
            displayVoice = true,
            enableKeyAnimation = true,
            // No Arabic swipe dictionary yet
            swipeEnabled = false
        )
    )
) : EnBaseViewModel<SwipeResult>(
    passedCallback = passedCallback,
    swipeCallback = swipeCallback,
    haptic = haptic,
    optionsForLayout = optionsForLayout,
    keyboardOptionsFlow = keyboardOptionsFlow,
    initialLayout = Arabic.LettersLayout,
    lowerCaseLayout = Arabic.LettersLayout,
    upperCaseLayout = Arabic.LettersLayout,
    capsLockedLayout = Arabic.LettersLayout,
    numberLayout = Arabic.NumberLayout,
    symbolsLayout = Arabic.SymbolsLayout,
    extendedCharMapping = Arabic.extendedCharMapping,
)
