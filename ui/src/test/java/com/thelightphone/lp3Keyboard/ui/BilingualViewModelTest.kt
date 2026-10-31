package com.thelightphone.lp3Keyboard.ui

import com.thelightphone.lp3Keyboard.ui.layout.Arabic
import com.thelightphone.lp3Keyboard.ui.layout.EnQwerty
import com.thelightphone.lp3Keyboard.ui.layout.EnShared
import com.thelightphone.lp3Keyboard.ui.layout.LayoutRegistryItem
import com.thelightphone.lp3Keyboard.ui.layout.buildRootViewModel
import com.thelightphone.lp3Keyboard.ui.viewmodel.BilingualLp3KeyboardViewModel
import com.thelightphone.lp3Keyboard.ui.viewmodel.Lp3RepeatableKeyboardCallback
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BilingualViewModelTest {

    private val callback = mockk<Lp3RepeatableKeyboardCallback>(relaxed = true)
    private val swipeCallback = mockk<Lp3KeyboardSwipeCallback<Unit>>(relaxed = true)
    private val switches = mutableListOf<Int>()

    private fun build(initial: Int = BilingualLp3KeyboardViewModel.ENGLISH) =
        BilingualLp3KeyboardViewModel.englishArabic(
            passedCallback = callback,
            swipeCallback = swipeCallback,
            initialIndex = initial,
            onLanguageChanged = { switches += it },
        )

    private fun BilingualLp3KeyboardViewModel<Unit>.tapLanguage() {
        onSpecialKeyPressed(SpecialKey.Language)
        onSpecialKeyReleased(SpecialKey.Language)
    }

    private fun BilingualLp3KeyboardViewModel<Unit>.type(c: Char) {
        onKeyPressed(c.code)
        onKeyReleased(c.code)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts on English letters with an Arabic switch label`() {
        val vm = build()
        assertSame(EnQwerty.LowerCaseLayout, vm.layoutFlow.value)
        assertEquals("ع", vm.keyboardOptionsFlow.value.languageKeyLabel)
    }

    @Test
    fun `language key switches to Arabic and back`() {
        val vm = build()
        vm.tapLanguage()
        assertSame(Arabic.LettersLayout, vm.layoutFlow.value)
        assertEquals("EN", vm.keyboardOptionsFlow.value.languageKeyLabel)

        vm.tapLanguage()
        assertSame(EnQwerty.LowerCaseLayout, vm.layoutFlow.value)
        assertEquals("ع", vm.keyboardOptionsFlow.value.languageKeyLabel)
        assertEquals(listOf(1, 0), switches)
    }

    @Test
    fun `the language key itself is never sent to the text field`() {
        val vm = build()
        vm.tapLanguage()
        vm.onSpecialKeyLongPressed(SpecialKey.Language)
        verify(exactly = 0) { callback.onSpecialKeyPressed(SpecialKey.Language) }
        verify(exactly = 0) { callback.onSpecialKeyReleased(SpecialKey.Language) }
        verify(exactly = 0) { callback.onSpecialKeyLongPressed(SpecialKey.Language) }
    }

    @Test
    fun `typing goes to whichever language is showing`() {
        val vm = build()
        vm.type('h')
        vm.tapLanguage()
        vm.type('م')
        verify(exactly = 1) { callback.onKeyReleased('h'.code) }
        verify(exactly = 1) { callback.onKeyReleased('م'.code) }
    }

    @Test
    fun `can start on Arabic`() {
        val vm = build(initial = BilingualLp3KeyboardViewModel.ARABIC)
        assertSame(Arabic.LettersLayout, vm.layoutFlow.value)
        assertEquals("EN", vm.keyboardOptionsFlow.value.languageKeyLabel)
    }

    @Test
    fun `switching from the numbers page lands on the other language's letters`() {
        val vm = build()
        vm.onSpecialKeyReleased(SpecialKey.Numbers)
        assertSame(EnShared.NumberLayout, vm.layoutFlow.value)
        vm.tapLanguage()
        assertSame(Arabic.LettersLayout, vm.layoutFlow.value)

        vm.onSpecialKeyReleased(SpecialKey.Numbers)
        assertSame(Arabic.NumberLayout, vm.layoutFlow.value)
        vm.tapLanguage()
        assertSame(EnQwerty.LowerCaseLayout, vm.layoutFlow.value)
    }

    @Test
    fun `sentence caps carry over when switching back to English`() {
        val vm = build(initial = BilingualLp3KeyboardViewModel.ARABIC)
        vm.setCapsMode(true) // e.g. the cursor moved to the start of a new sentence
        vm.tapLanguage()
        assertSame(EnQwerty.UpperCaseLayout, vm.layoutFlow.value)
    }

    @Test
    fun `long-press popups still work in both languages`() {
        val vm = build()
        vm.onKeyLongPressed('e'.code)
        assertTrue(vm.layoutFlow.value is EnShared.ExtendedCharKeyboard)
        vm.onKeyReleased('e'.code)
        vm.type('é')
        assertSame(EnQwerty.LowerCaseLayout, vm.layoutFlow.value)

        vm.tapLanguage()
        vm.onKeyLongPressed('ا'.code)
        assertTrue(vm.layoutFlow.value is EnShared.ExtendedCharKeyboard)
        vm.onKeyReleased('ا'.code)
        vm.type('أ')
        assertSame(Arabic.LettersLayout, vm.layoutFlow.value)
        verify(exactly = 1) { callback.onKeyReleased('é'.code) }
        verify(exactly = 1) { callback.onKeyReleased('أ'.code) }
    }

    @Test
    fun `single-language keyboards have no language key`() {
        val en = LayoutRegistryItem.EnQwerty.buildRootViewModel(callback, swipeCallback)
        val ar = LayoutRegistryItem.ArStandard.buildRootViewModel(callback, swipeCallback)
        assertNull(en.keyboardOptionsFlow.value.languageKeyLabel)
        assertNull(ar.keyboardOptionsFlow.value.languageKeyLabel)
    }

    @Test
    fun `bilingual layout is in the registry`() {
        val vm = LayoutRegistryItem.EnArBilingual.buildRootViewModel(callback, swipeCallback)
        assertTrue(vm is BilingualLp3KeyboardViewModel<*>)
        assertEquals("ar_bilingual-en", LayoutRegistryItem.EnArBilingual.uniqueId)
    }
}
