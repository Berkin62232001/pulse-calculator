package com.pulse.calculator.ui.display

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayChangeTest {

    private fun typed(previous: String, next: String) =
        DisplayChange.between(previous, previousIsResult = false, next = next, isResult = false)

    @Test
    fun onlyTheLastTypedGlyphPopsIn() {
        assertEquals(DisplayChange.Pop(0), typed("", "5"))
        assertEquals(DisplayChange.Pop(1), typed("5", "52"))
        assertEquals(DisplayChange.Pop(4), typed("5 +", "5 + 3"))
    }

    @Test
    fun groupingShiftsPopOnlyTheLastGlyph() {
        assertEquals(DisplayChange.Pop(4), typed("123", "1,234"))
    }

    @Test
    fun autoInsertedMultiplyDoesNotPopByItself() {
        assertEquals(DisplayChange.Pop("2 × (".lastIndex), typed("2", "2 × ("))
    }

    @Test
    fun replacingAnOperatorFlipsIt() {
        assertEquals(DisplayChange.Flip(2), typed("5 +", "5 ×"))
        assertEquals(DisplayChange.Flip(2), typed("5 ×", "5 −"))
        assertEquals(DisplayChange.Flip(2), typed("5 ÷", "5 +"))
    }

    @Test
    fun sameLengthChangesThatAreNotOperatorSwapsDoNotFlip() {
        assertEquals(DisplayChange.None, typed("5 + 3", "5 + 4"))
        assertEquals(DisplayChange.None, typed("1 + 2", "1 × 2"))
        assertEquals(DisplayChange.None, typed("7", "8"))
    }

    @Test
    fun shrinkingDoesNotAnimate() {
        assertEquals(DisplayChange.None, typed("52", "5"))
        assertEquals(DisplayChange.None, typed("5 ×", "5"))
        assertEquals(DisplayChange.None, typed("5", ""))
    }

    @Test
    fun aConfirmedResultNeverAnimatesPerGlyph() {
        assertEquals(
            DisplayChange.None,
            DisplayChange.between("1 ÷ 3", previousIsResult = false, next = "0.333333333333", isResult = true),
        )
    }

    @Test
    fun clearingNeverAnimatesPerGlyph() {
        assertEquals(DisplayChange.None, typed("12 + 5", ""))
    }

    @Test
    fun typingAfterAResultStartsANewExpressionWithAPop() {
        assertEquals(
            DisplayChange.Pop(0),
            DisplayChange.between("8", previousIsResult = true, next = "7", isResult = false),
        )
        assertEquals(
            DisplayChange.Pop(0),
            DisplayChange.between("17", previousIsResult = true, next = "3", isResult = false),
        )
    }

    @Test
    fun continuingFromAResultPopsTheOperator() {
        assertEquals(
            DisplayChange.Pop("17 ×".lastIndex),
            DisplayChange.between("17", previousIsResult = true, next = "17 ×", isResult = false),
        )
    }

    @Test
    fun backspacingAResultDoesNotAnimate() {
        assertEquals(
            DisplayChange.None,
            DisplayChange.between("17", previousIsResult = true, next = "1", isResult = false),
        )
        assertEquals(
            DisplayChange.None,
            DisplayChange.between("1,234", previousIsResult = true, next = "123", isResult = false),
        )
    }
}
