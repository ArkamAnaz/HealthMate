package com.healthmate.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Unit tests for the Add / Edit Record form validation. */
class RecordValidatorTest {

    private val today = DateUtils.today()

    @Test
    fun validInput_passesValidation() {
        val result = RecordValidator.validate(today, "7850", "420", "2000")
        assertTrue(result.isValid)
    }

    @Test
    fun emptySteps_showsRequiredError() {
        val result = RecordValidator.validate(today, "", "420", "2000")
        assertFalse(result.isValid)
        assertEquals("Steps is required", result.stepsError)
    }

    @Test
    fun negativeCalories_showsError() {
        val result = RecordValidator.validate(today, "5000", "-50", "2000")
        assertEquals("Calories cannot be negative", result.caloriesError)
    }

    @Test
    fun decimalOrText_showsWholeNumberError() {
        assertEquals(
            "Water intake must be a whole number",
            RecordValidator.validate(today, "5000", "300", "1.5").waterError
        )
        assertEquals(
            "Steps must be a whole number",
            RecordValidator.validate(today, "abc", "300", "1500").stepsError
        )
    }

    @Test
    fun valueAboveMaximum_showsError() {
        val result = RecordValidator.validate(today, "200000", "300", "1500")
        assertEquals("Steps cannot be more than 100,000", result.stepsError)
    }

    @Test
    fun futureDate_showsError() {
        val tomorrow = DateUtils.format(LocalDate.now().plusDays(1))
        assertEquals("Date cannot be in the future", RecordValidator.validateDate(tomorrow))
    }

    @Test
    fun emptyDate_showsError() {
        assertEquals("Please select a date", RecordValidator.validateDate(""))
    }

    @Test
    fun zeroValues_areAllowed() {
        assertNull(RecordValidator.validateNumber("0", "Steps", RecordValidator.MAX_STEPS))
    }

    @Test
    fun goalOfZero_isRejected() {
        assertEquals(
            "Steps goal must be greater than 0",
            RecordValidator.validateGoal("0", "Steps", RecordValidator.MAX_STEPS)
        )
    }

    @Test
    fun validGoal_isAccepted() {
        assertNull(RecordValidator.validateGoal("8000", "Steps", RecordValidator.MAX_STEPS))
    }
}
