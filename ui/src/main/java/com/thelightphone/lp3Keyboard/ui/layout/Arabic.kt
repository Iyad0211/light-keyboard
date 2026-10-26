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
 * Arabic layout, using the standard Arabic phone/PC key positions (the same as Gboard and the
 * Arabic 101 PC keyboard), drawn with the stock LP3 keys, rows, space bar and icons.
 *
 * Arabic has no letter case, so there is no shift key: the third row holds ten letters instead.
 * Hamza forms, Gulf letters (چ گ ڤ پ) and Arabic-Indic digits are on long-press, and the
 * diacritics (tashkeel) are on the "#+=" page.
 *
 * Keys are laid out left-to-right exactly like a physical Arabic keyboard (ض top-left), so
 * muscle memory carries over. Text direction in the field is handled by Android's bidi.
 */
object Arabic {

    /** Labels for the key that returns to letters from the numbers/symbols pages. */
    const val LETTERS_LABEL = "أبج"

    const val ROW_1 = "ضصثقفغعهخحج"
    const val ROW_2 = "شسيبلاتنمكط"
    const val ROW_3 = "ذءؤرىةوزظد"

    /** Every character on the letter page — handy for tests and swipe dictionaries. */
    val allLetters: String = ROW_1 + ROW_2 + ROW_3

    private const val KEYS_PER_LETTER_ROW = 11

    /** Backspace on the letter page is a little wider than a letter key, like LP3's icon keys. */
    private const val BACKSPACE_EXTRA_DP = 6

    /**
     * English rows span ten 35dp keys (350dp). Arabic needs eleven per row, so shrink the keys to
     * keep the same overall footprint on the 360dp-wide LP3 screen, and grow back to 35dp on
     * wider screens.
     */
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
                // Same icon, same drawn size as the stock third row's backspace; only the
                // touch target is narrower so ten letters fit beside it.
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
            // Western digits by default (what most Gulf phone users type); hold for ١٢٣.
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

    /**
     * Tashkeel, in the order Arabic keyboards usually list them. Each is drawn on a tatweel so
     * the mark is visible on the key; tapping types only the mark onto the previous letter.
     */
    val tashkeel: List<Char> = listOf(
        'َ', // fatha      َ
        'ً', // fathatan   ً
        'ُ', // damma      ُ
        'ٌ', // dammatan   ٌ
        'ِ', // kasra      ِ
        'ٍ', // kasratan   ٍ
        'ْ', // sukun      ْ
        'ّ', // shadda     ّ
        'ٰ', // dagger alif ٰ
    )

    const val TATWEEL = 'ـ'

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

    /** Long-press alternates. Kept to one short row each, like the stock accent popups. */
    val extendedCharMapping: Map<Int, List<List<Char>>> = buildMap {
        // Hamza and alif forms
        put('ا'.code, listOf(listOf('أ', 'إ', 'آ', 'ٱ', 'ء')))
        put('ء'.code, listOf(listOf('أ', 'إ', 'ؤ', 'ئ')))
        put('ي'.code, listOf(listOf('ئ', 'ى')))
        put('ى'.code, listOf(listOf('ي', 'ئ')))
        put('و'.code, listOf(listOf('ؤ')))
        put('ه'.code, listOf(listOf('ة')))
        put('ة'.code, listOf(listOf('ه')))
        // Letters used in Gulf dialect and loanwords
        put('ج'.code, listOf(listOf('چ')))
        put('ك'.code, listOf(listOf('گ')))
        put('ق'.code, listOf(listOf('گ')))
        put('ف'.code, listOf(listOf('ڤ')))
        put('ب'.code, listOf(listOf('پ')))
        // Arabic-Indic digits on the numbers page
        for (d in 0..9) {
            put(('0' + d).code, listOf(listOf(arabicIndicDigits[d])))
        }
        // Latin punctuation behind the Arabic forms, and the symbols that the stock
        // "#+=" page had but this one gives up to make room for tashkeel
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
