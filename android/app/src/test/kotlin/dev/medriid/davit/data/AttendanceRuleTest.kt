package dev.medriid.davit.data

import dev.medriid.davit.vtop.AttendanceRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The 75% rule on the exact fraction: exactly 75% is in, anything below is out, however
 * VTOP rounds it for display.
 */
class AttendanceRuleTest {
    private fun row(attended: Int, total: Int, vtop: Int? = null) =
        AttendanceRow("X", "Course", "Theory", attended, total, vtop, "")

    @Test
    fun exactlySeventyFiveIsIn() {
        val r = row(30, 40)
        assertFalse(r.atRisk)
        assertEquals(75, r.percent)
        assertEquals(0, r.canSkipNow)
        assertEquals(0, r.mustAttend)
    }

    @Test
    fun seventyFourPointFiveIsSeventyFive() {
        val r = row(149, 200, vtop = 75)
        assertFalse(r.atRisk)
        assertEquals(75, r.percent) // 74.5% rounds up to 75% with ceil
        assertEquals(0, r.mustAttend)
        assertEquals(1, r.canSkipNow)
        assertFalse(row(149, 200 + r.canSkipNow).atRisk)
        assertTrue(row(149, 201 + r.canSkipNow).atRisk)
    }

    @Test
    fun seventyThreePointFiveIsSeventyFour() {
        val r = row(30, 41, vtop = 74)
        assertTrue(r.atRisk)
        assertEquals(74, r.percent)
        assertEquals(2, r.mustAttend)
        assertEquals(0, r.canSkipNow)
        assertFalse(row(30 + r.mustAttend, 41 + r.mustAttend).atRisk)
        assertTrue(row(29 + r.mustAttend, 40 + r.mustAttend).atRisk)
    }

    @Test
    fun skipBudgetLandsExactlyOnTheLine() {
        val r = row(34, 40)
        assertEquals(5, r.canSkipNow)
        assertFalse(row(34, 40 + r.canSkipNow).atRisk)
        assertTrue(row(34, 41 + r.canSkipNow).atRisk)
    }

    @Test
    fun shownPercentRoundsUp() {
        assertEquals(75, row(7401, 10000).percent) // 74.01% -> 75%
        assertEquals(74, row(7400, 10000).percent) // 74.00% -> 74%
    }
}
