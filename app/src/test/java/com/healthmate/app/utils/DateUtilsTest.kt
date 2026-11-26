package com.healthmate.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateUtilsTest {

    @Test
    fun toDisplay_formatsDatabaseDate() {
        assertEquals("18 August 2026", DateUtils.toDisplay("2026-08-18"))
    }

    @Test
    fun parse_invalidText_returnsNull() {
        assertNull(DateUtils.parse("18/08/2026"))
    }

    @Test
    fun formatNumber_addsThousandSeparator() {
        assertEquals("7,850", formatNumber(7850))
    }
}
