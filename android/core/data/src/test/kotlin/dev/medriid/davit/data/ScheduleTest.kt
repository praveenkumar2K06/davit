package dev.medriid.davit.data

import dev.medriid.davit.vtop.AttendanceRow
import dev.medriid.davit.vtop.TimetableCourse
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class ScheduleTest {

    @Test
    fun clockFormatsMinutesToTimeString() {
        assertEquals("08:30", clock(510))
        assertEquals("00:00", clock(0))
        assertEquals("14:45", clock(885))
    }

    @Test
    fun weekDatesReturnsMondayToSunday() {
        // Monday 26 Oct 2026
        val monday = LocalDate.of(2026, 10, 26)
        val datesMon = weekDates(monday)
        assertEquals(7, datesMon.size)
        assertEquals(monday, datesMon.first())
        assertEquals(DayOfWeek.MONDAY, datesMon.first().dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, datesMon.last().dayOfWeek)

        // Sunday 1 Nov 2026 should return the coming week starting next Monday 2 Nov 2026
        val sunday = LocalDate.of(2026, 11, 1)
        val datesSun = weekDates(sunday)
        assertEquals(LocalDate.of(2026, 11, 2), datesSun.first())
    }

    @Test
    fun matchAttendanceMatchesByClassIdThenCodeAndType() {
        val course = TimetableCourse("C101", "BACHY105", "Applied Chemistry", "ETH", 4.0, "A1", "SJT-401", "F")
        val row1 = AttendanceRow("BACHY105", "Applied Chemistry", "Embedded Theory", 30, 40, null, "C101")
        val row2 = AttendanceRow("BACHY105", "Applied Chemistry", "Embedded Lab", 10, 10, null, "C102")

        assertEquals(row1, matchAttendance(course, listOf(row2, row1)))

        // Match by code and type when classId is empty
        val courseNoId = course.copy(classId = "")
        val rowNoId = row1.copy(classId = "")
        assertEquals(rowNoId, matchAttendance(courseNoId, listOf(rowNoId)))
    }

    @Test
    fun skipTillEndCalculation() {
        val row = AttendanceRow("BACHY105", "Applied Chemistry", "Embedded Theory", 30, 40, null, "VL1") // 75%
        assertEquals(2, row.skipTillEnd(10))
    }

    @Test
    fun attendanceHintText() {
        val rowAt75 = AttendanceRow("BACHY105", "Applied Chemistry", "Embedded Theory", 30, 40, null, "VL1")
        assertEquals("can skip 2 of the 10 left", rowAt75.hint(10))

        val rowZeroRemaining = AttendanceRow("BACHY105", "Applied Chemistry", "Embedded Theory", 30, 40, null, "VL1")
        assertEquals("no classes left", rowZeroRemaining.hint(0))
    }
}
