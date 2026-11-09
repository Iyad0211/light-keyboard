package com.thelightphone.lp3Keyboard.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thelightphone.lp3Keyboard.ui.DarkKeyboardColors
import com.thelightphone.lp3Keyboard.ui.DefaultRow
import com.thelightphone.lp3Keyboard.ui.FinalRow
import com.thelightphone.lp3Keyboard.ui.FirstRow
import com.thelightphone.lp3Keyboard.ui.IconKey
import com.thelightphone.lp3Keyboard.ui.Key
import com.thelightphone.lp3Keyboard.ui.KeyboardOptions
import com.thelightphone.lp3Keyboard.ui.LayoutOptions
import com.thelightphone.lp3Keyboard.ui.LightKeyboardColors
import com.thelightphone.lp3Keyboard.ui.Lp3KeyboardCallback
import com.thelightphone.lp3Keyboard.ui.Lp3KeyboardTheme
import com.thelightphone.lp3Keyboard.ui.Lp3KeyboardWrapper
import com.thelightphone.lp3Keyboard.ui.MultiLabelKey
import com.thelightphone.lp3Keyboard.ui.R
import com.thelightphone.lp3Keyboard.ui.STANDARD_KEY_WIDTH_DP
import com.thelightphone.lp3Keyboard.ui.SecondRow
import com.thelightphone.lp3Keyboard.ui.SpecialKey
import com.thelightphone.lp3Keyboard.ui.ThirdRow
import com.thelightphone.lp3Keyboard.ui.previewCallback
import com.thelightphone.lp3Keyboard.ui.viewmodel.defaultEmojis

/**
 * Standard Arabic layout (same key positions as Gboard / Arabic 101). No shift row; hamza forms,
 * a few Gulf letters and Arabic-Indic digits are on long-press, tashkeel is on the #+= page.
 */
object Arabic {

    const val LETTERS_LABEL = "أبج"

    const val ROW_1 = "ضصثقفغعهخحج"
    const val ROW_2 = "شسيبلاتنمكط"
    const val ROW_3 = "ذءؤرىةوزظد"

    val allLetters: String = ROW_1 + ROW_2 + ROW_3

    private const val KEYS_PER_LETTER_ROW = 11

    private const val BACKSPACE_EXTRA_DP = 6

    // 11 keys per row, shrunk to fit the same width as the 10-key english rows
    private const val SIDE_MARGIN_DP = 10

    @Composable
    private fun letterKeyWidth(): Dp {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        val fit = (screenWidth - SIDE_MARGIN_DP - BACKSPACE_EXTRA_DP).toFloat() / KEYS_PER_LETTER_ROW
        return minOf(STANDARD_KEY_WIDTH_DP.toFloat(), fit).dp
    }

    @Composable
    private fun ColumnScope.LetterRow(
        characters: String,
        callback: Lp3KeyboardCallback,
        options: KeyboardOptions,
        keyWidth: Dp,
        trailing: @Composable RowScope.() -> Unit = {}
    ) {
        DefaultRow {
            for (char in characters) {
                Key(
                    char.code,
                    callback,
                    null,
                    options.enableKeyAnimation,
                    width = keyWidth
                )
            }
            trailing()
        }
    }

    object LettersLayout : Layout {
        override val isRootLayout: Boolean
            get() = true

        @Composable
        override fun ColumnScope.Render(
            options: KeyboardOptions,
            callback: Lp3KeyboardCallback
        ) {
            val keyWidth = letterKeyWidth()
            LetterRow(ROW_1, callback, options, keyWidth)
            LetterRow(ROW_2, callback, options, keyWidth)
            LetterRow(ROW_3, callback, options, keyWidth) {
                IconKey(
                    R.drawable.back_lp3,
                    SpecialKey.Backspace,
                    callback,
                    options.enableKeyAnimation,
                    width = keyWidth + BACKSPACE_EXTRA_DP.dp,
                    modifier = Modifier.padding(start = 9.dp, end = 7.dp, top = 10.dp, bottom = 16.dp)
                )
            }
            FinalRow(options, callback) {
                MultiLabelKey("123", SpecialKey.Numbers, callback, options.enableKeyAnimation)
            }
        }
    }

    object NumberLayout : Layout {
        @Composable
        override fun ColumnScope.Render(
            options: KeyboardOptions,
            callback: Lp3KeyboardCallback
        ) {
            FirstRow("1234567890", callback, null, options.enableKeyAnimation)
            SecondRow("-/:؛()$&@\"", callback, null, options.enableKeyAnimation)
            ThirdRow(".،؟!'", callback, null, options) {
                MultiLabelKey("#+=", SpecialKey.Symbols, callback, options.enableKeyAnimation)
            }
            FinalRow(options, callback) {
                MultiLabelKey(LETTERS_LABEL, SpecialKey.Letters, callback, options.enableKeyAnimation)
            }
        }
    }

    // drawn on a tatweel so the mark is visible; typing sends just the mark
    val tashkeel: List<Char> = listOf(
        '\u064E', // fatha
        '\u064B', // fathatan
        '\u064F', // damma
        '\u064C', // dammatan
        '\u0650', // kasra
        '\u064D', // kasratan
        '\u0652', // sukun
        '\u0651', // shadda
        '\u0670', // superscript alef
    )

    const val TATWEEL = '\u0640'

    object SymbolsLayout : Layout {
        @Composable
        override fun ColumnScope.Render(
            options: KeyboardOptions,
            callback: Lp3KeyboardCallback
        ) {
            FirstRow("[]{}#%^*+=", callback, null, options.enableKeyAnimation)
            DefaultRow {
                for (mark in tashkeel) {
                    Key(
                        mark.code,
                        callback,
                        null,
                        options.enableKeyAnimation,
                        displayLabel = "$TATWEEL$mark"
                    )
                }
                Key(TATWEEL.code, callback, null, options.enableKeyAnimation)
            }
            ThirdRow(".،؟!'", callback, null, options) {
                MultiLabelKey("123", SpecialKey.Numbers, callback, options.enableKeyAnimation)
            }
            FinalRow(options, callback) {
                MultiLabelKey(LETTERS_LABEL, SpecialKey.Letters, callback, options.enableKeyAnimation)
            }
        }
    }

    private val arabicIndicDigits = "٠١٢٣٤٥٦٧٨٩"

    val extendedCharMapping: Map<Int, List<List<Char>>> = buildMap {
        put('ا'.code, listOf(listOf('أ', 'إ', 'آ', 'ٱ', 'ء')))
        put('ء'.code, listOf(listOf('أ', 'إ', 'ؤ', 'ئ')))
        put('ي'.code, listOf(listOf('ئ', 'ى')))
        put('ى'.code, listOf(listOf('ي', 'ئ')))
        put('و'.code, listOf(listOf('ؤ')))
        put('ه'.code, listOf(listOf('ة')))
        put('ة'.code, listOf(listOf('ه')))
        put('ج'.code, listOf(listOf('چ')))
        put('ك'.code, listOf(listOf('گ')))
        put('ق'.code, listOf(listOf('گ')))
        put('ف'.code, listOf(listOf('ڤ')))
        put('ب'.code, listOf(listOf('پ')))
        for (d in 0..9) {
            put(('0' + d).code, listOf(listOf(arabicIndicDigits[d])))
        }
        put('،'.code, listOf(listOf(',')))
        put('؟'.code, listOf(listOf('?')))
        put('؛'.code, listOf(listOf(';')))
        put('%'.code, listOf(listOf('٪')))
        put('-'.code, listOf(listOf('_', '–', '—')))
        put('/'.code, listOf(listOf('\\', '|')))
        put('('.code, listOf(listOf('<')))
        put(')'.code, listOf(listOf('>')))
        put('$'.code, listOf(listOf('€', '£', '¥')))
        put('='.code, listOf(listOf('~')))
    }
}

@Preview(name = "Arabic letters", widthDp = (1080 / 3), heightDp = (1240 / 3))
@Composable
fun ArabicLettersPreview() = ArabicPreview(Arabic.LettersLayout, light = false)

@Preview(name = "Arabic symbols", widthDp = (1080 / 3), heightDp = (1240 / 3))
@Composable
fun ArabicSymbolsPreview() = ArabicPreview(Arabic.SymbolsLayout, light = true)

@Composable
private fun ArabicPreview(layout: Layout, light: Boolean) {
    Lp3KeyboardTheme(if (light) LightKeyboardColors else DarkKeyboardColors) {
        Column(verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxSize()) {
            Lp3KeyboardWrapper(
                layout,
                KeyboardOptions(
                    defaultEmojis,
                    displayReturn = true,
                    displayVoice = true,
                    enableKeyAnimation = true,
                    swipeEnabled = false
                ),
                LayoutOptions(displayCloseButton = true),
                previewCallback,
                null
            )
        }
    }
}
