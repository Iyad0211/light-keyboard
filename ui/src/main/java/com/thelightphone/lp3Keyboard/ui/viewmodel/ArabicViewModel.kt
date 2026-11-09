package com.thelightphone.lp3Keyboard.ui.viewmodel

import com.thelightphone.lp3Keyboard.ui.KeyboardOptions
import com.thelightphone.lp3Keyboard.ui.LayoutOptions
import com.thelightphone.lp3Keyboard.ui.Lp3KeyboardSwipeCallback
import com.thelightphone.lp3Keyboard.ui.layout.Arabic
import com.thelightphone.lp3Keyboard.ui.layout.Layout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Arabic has no case, so all three letter slots use the same page.
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
