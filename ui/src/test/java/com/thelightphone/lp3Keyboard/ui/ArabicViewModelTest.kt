package com.thelightphone.lp3Keyboard.ui

import com.thelightphone.lp3Keyboard.ui.layout.Arabic
import com.thelightphone.lp3Keyboard.ui.layout.EnShared
import com.thelightphone.lp3Keyboard.ui.layout.LayoutRegistryItem
import com.thelightphone.lp3Keyboard.ui.viewmodel.ArabicLp3KeyboardViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArabicViewModelTest {

    private val callback = mockk<Lp3RepeatableKeyboardCallback>(relaxed = true)
    private val swipeCallback = mockk<Lp3KeyboardSwipeCallback<Unit>>(relaxed = true)

    private val vm = ArabicLp3KeyboardViewModel(
        passedCallback = callback,
        swipeCallback = swipeCallback,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `letter page covers the full Arabic alphabet plus hamza and taa marbuta`() {
        val required = "ابتثجحخدذرزسشصضطظعغفقكلمنهوي" + "ءؤىة"
        for (c in required) {
            assertTrue("missing $c", c in Arabic.allLetters)
        }
        assertEquals(
            "no letter should appear twice",
            Arabic.allLetters.length,
            Arabic.allLetters.toSet().size
        )
        assertEquals(11, Arabic.ROW_1.length)
        assertEquals(11, Arabic.ROW_2.length)
        assertEquals(10, Arabic.ROW_3.length)
    }

    @Test
    fun `starts on the Arabic letters page`() {
        assertSame(Arabic.LettersLayout, vm.layoutFlow.value)
    }

    @Test
    fun `tapping a letter commits it`() {
        vm.onKeyPressed('ض'.code)
        vm.onKeyReleased('ض'.code)
        verify(exactly = 1) { callback.onKeyReleased('ض'.code) }
        assertSame(Arabic.LettersLayout, vm.layoutFlow.value)
    }

    @Test
    fun `system caps requests leave the Arabic letters page in place`() {
        vm.setCapsMode(true)
        assertSame(Arabic.LettersLayout, vm.layoutFlow.value)
        vm.onKeyPressed('ب'.code)
        vm.onKeyReleased('ب'.code)
        assertSame(Arabic.LettersLayout, vm.layoutFlow.value)
        verify(exactly = 1) { callback.onKeyReleased('ب'.code) }
    }

    @Test
    fun `123 and symbols keys open the Arabic pages, not the English ones`() {
        vm.onSpecialKeyReleased(SpecialKey.Numbers)
        assertSame(Arabic.NumberLayout, vm.layoutFlow.value)
        vm.onSpecialKeyReleased(SpecialKey.Symbols)
        assertSame(Arabic.SymbolsLayout, vm.layoutFlow.value)
        vm.onSpecialKeyReleased(SpecialKey.Letters)
        assertSame(Arabic.LettersLayout, vm.layoutFlow.value)
    }

    @Test
    fun `space on the numbers page returns to Arabic letters`() {
        vm.onSpecialKeyReleased(SpecialKey.Numbers)
        vm.onSpecialKeyPressed(SpecialKey.Space)
        vm.onSpecialKeyReleased(SpecialKey.Space)
        assertSame(Arabic.LettersLayout, vm.layoutFlow.value)
    }

    @Test
    fun `long-press alif opens hamza forms and picking one returns to letters`() {
        vm.onKeyLongPressed('ا'.code)
        assertTrue(vm.layoutFlow.value is EnShared.ExtendedCharKeyboard)

        vm.onKeyReleased('ا'.code) // finger lifts off the held key; swallowed
        vm.onKeyPressed('أ'.code)
        vm.onKeyReleased('أ'.code)

        verify(exactly = 1) { callback.onKeyReleased('أ'.code) }
        verify(exactly = 0) { callback.onKeyReleased('ا'.code) }
        assertSame(Arabic.LettersLayout, vm.layoutFlow.value)
    }

    @Test
    fun `long-press digit offers the Arabic-Indic digit`() {
        assertEquals(listOf(listOf('٣')), Arabic.extendedCharMapping['3'.code])
        vm.onSpecialKeyReleased(SpecialKey.Numbers)
        vm.onKeyLongPressed('3'.code)
        assertTrue(vm.layoutFlow.value is EnShared.ExtendedCharKeyboard)
    }

    @Test
    fun `Latin accent popups do not leak into Arabic`() {
        assertFalse(Arabic.extendedCharMapping.containsKey('a'.code))
        vm.onKeyLongPressed('a'.code)
        assertFalse(vm.layoutFlow.value is EnShared.ExtendedCharKeyboard)
    }

    @Test
    fun `tashkeel keys type the bare combining mark`() {
        for (mark in Arabic.tashkeel) {
            assertEquals(
                "$mark should be a non-spacing mark",
                Character.NON_SPACING_MARK.toInt(),
                Character.getType(mark)
            )
        }
    }

    @Test
    fun `Arabic is listed in the registry`() {
        val item = LayoutRegistryItem.ArStandard
        assertEquals("ar_standard", item.uniqueId)
        assertTrue(LayoutRegistryItem.entries.contains(item))
    }
}
