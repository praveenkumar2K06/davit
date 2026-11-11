package dev.medriid.davit.data

import dev.medriid.davit.vtop.AttendanceRow
import dev.medriid.davit.vtop.Meeting
import dev.medriid.davit.vtop.TimetableCourse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class CalendarTest {
    // Fri 23 Oct instructional, Sat 24 Oct runs Monday's order, Sun 25 off, Mon 26 Deepavali,
    // Tue 27 instructional, Wed 28–Thu 29 FAT.
    private val published = """
        {"metadata": {"firstInstructionalDay": "2026-07-06", "lastTheoryInstructionalDay": "2026-10-27", "lastLabInstructionalDay": "2026-10-27"},
         "data": {"2026-10": [
           {"date": "2026-10-23", "dayOfWeek": 5, "type": "Instructional", "dayOrder": 5, "note": null},
           {"date": "2026-10-24", "dayOfWeek": 6, "type": "Instructional", "dayOrder": 1, "note": null},
           {"date": "2026-10-25", "dayOfWeek": 7, "type": "Holiday", "dayOrder": 0, "note": null},
           {"date": "2026-10-26", "dayOfWeek": 1, "type": "Holiday", "dayOrder": 0, "note": "Deepavali"},
           {"date": "2026-10-27", "dayOfWeek": 2, "type": "Instructional", "dayOrder": 2, "note": "Last Theory & Lab Instructional Day"},
           {"date": "2026-10-28", "dayOfWeek": 3, "type": "FAT", "dayOrder": 0, "note": null},
           {"date": "2026-10-29", "dayOfWeek": 4, "type": "FAT", "dayOrder": 0, "note": null}
         ]}}
    """.trimIndent()
    private val cal = AcademicCalendar.fromPublished("VL1", published)

    private val course = TimetableCourse("VL1", "BACHY105", "Applied Chemistry", "ETH", 4.0, "A1", "SJT", "F")
    private fun slot(day: Int, start: Int) = ClassSlot(course, Meeting(day, start, start + 50, "A1"), null)
    private val week = mapOf(1 to listOf(slot(1, 480)), 2 to listOf(slot(2, 540)), 5 to listOf(slot(5, 600)))

    @Test
    fun dayOrdersHolidaysAndExams() {
        assertEquals(1, cal.timetableDay(LocalDate.of(2026, 10, 24)))
        assertNull(cal.timetableDay(LocalDate.of(2026, 10, 26)))
        assertNull(cal.timetableDay(LocalDate.of(2026, 10, 28)))
        // Outside the calendar: the date's own weekday.
        assertEquals(3, cal.timetableDay(LocalDate.of(2026, 12, 2)))
        assertEquals(listOf(Triple("FAT", LocalDate.of(2026, 10, 28), LocalDate.of(2026, 10, 29))), cal.examWindows())
    }

    @Test
    fun saturdayRunsMondaysClasses() {
        val plan = planFor(week, LocalDateTime.of(2026, 10, 24, 7, 0), cal)
        assertEquals(1, plan.today.size)
        assertEquals(480, plan.next!!.meeting.start)
        assertEquals(LocalDate.of(2026, 10, 24), plan.nextDate)
    }

    @Test
    fun nextSkipsTheHoliday() {
        // After Saturday's class: Sunday off, Monday is Deepavali, so next is Tuesday.
        val plan = planFor(week, LocalDateTime.of(2026, 10, 24, 12, 0), cal)
        assertEquals(3, plan.nextInDays)
        assertEquals(LocalDate.of(2026, 10, 27), plan.nextDate)
        // Without the calendar it would have been Monday.
        assertEquals(2, planFor(week, LocalDateTime.of(2026, 10, 24, 12, 0)).nextInDays)
    }

    @Test
    fun theWeekTabShowsEachDatesOwnOrder() {
        // Sat 31 Oct runs Wednesday's order, as VTOP's calendar says.
        val wednesday = AcademicCalendar(
            "VL1", null, null, null,
            listOf(
                CalDay(LocalDate.of(2026, 10, 31), "Instructional", 3, null),
                CalDay(LocalDate.of(2026, 11, 4), "Holiday", 0, "Deepavali"),
            ),
        )
        val wed = slot(3, 540)
        val week = mapOf(3 to listOf(wed))
        val saturday = LocalDate.of(2026, 10, 31)
        assertEquals(listOf(wed), classesOn(week, saturday, wednesday))
        assertEquals(emptyList<ClassSlot>(), classesOn(week, saturday, null))
        assertEquals("Runs Wednesday's timetable", wednesday.dayNote(saturday))
        // The Wednesday after is Deepavali: no classes, and it says why.
        assertEquals(emptyList<ClassSlot>(), classesOn(week, LocalDate.of(2026, 11, 4), wednesday))
        assertEquals("Holiday: Deepavali", wednesday.dayNote(LocalDate.of(2026, 11, 4)))
        assertNull(wednesday.dayNote(LocalDate.of(2026, 11, 2)))
        // Monday to Sunday of this week; on a Sunday, the coming week.
        assertEquals(LocalDate.of(2026, 10, 26), weekDates(LocalDate.of(2026, 10, 30)).first())
        assertEquals(LocalDate.of(2026, 11, 2), weekDates(LocalDate.of(2026, 11, 1)).first())
    }

    @Test
    fun cachedFormatRoundTrips() {
        assertEquals(cal, AcademicCalendar.fromJson(cal.toJson()))
    }

    @Test
    fun semesterSkipBudget() {
        val row = AttendanceRow("BACHY105", "Applied Chemistry", "Theory", 30, 40, null, "VL1")
        // 30/40 with 8 left: attend all → 38/48; can miss k while 4(38-k) ≥ 3·48 → k ≤ 2.
        assertEquals(2, row.skipTillEnd(8))
        assertEquals("can skip 2 of the 8 left", row.hint(8))
        val low = AttendanceRow("X", "Y", "Theory", 10, 30, null, "")
        assertEquals("75% can't be reached with 4 classes left", low.hint(4))
        assertEquals(low.hint(), low.hint(null))
        val safe = AttendanceRow("X", "Y", "Theory", 10, 12, null, "")
        assertEquals("can skip the last one", safe.hint(1))
        assertEquals("can skip all 2 left", AttendanceRow("X", "Y", "Theory", 30, 32, null, "").hint(2))
    }

    @Test
    fun vtopGridBecomesACalendar() {
        fun cell(day: Int, label: String, note: String = "") = dev.medriid.davit.vtop.CalendarCell(LocalDate.of(2026, 10, day), label, note)
        val cal = AcademicCalendar.fromVtop(
            "VL1", "General Freshers",
            listOf(
                cell(3, "CAT - II", "Exam Day"),
                cell(5, "Instructional Day", "First Instructional Day"),
                cell(11, ""),
                cell(17, "Instructional Day", "Thursday Day Order"),
                cell(19, "Holiday", "Ayutha Pooja"),
                cell(20, "No Instructional Day", "Gravitas '26"),
                cell(27, "Instructional Day", "Last instructional day for lab and theory classes"),
            ),
        )
        assertEquals(4, cal.timetableDay(LocalDate.of(2026, 10, 17)))
        assertNull(cal.timetableDay(LocalDate.of(2026, 10, 11)))
        assertNull(cal.timetableDay(LocalDate.of(2026, 10, 19)))
        assertNull(cal.timetableDay(LocalDate.of(2026, 10, 20)))
        assertEquals("CAT-2", cal.day(LocalDate.of(2026, 10, 3))!!.type)
        assertEquals(LocalDate.of(2026, 10, 5), cal.firstDay)
        assertEquals(LocalDate.of(2026, 10, 27), cal.lastTheory)
        assertEquals(LocalDate.of(2026, 10, 27), cal.lastLab)
        assertEquals("General Freshers", AcademicCalendar.fromJson(cal.toJson()).group)
    }

    @Test
    fun bothVtopDateFormats() {
        assertEquals(LocalDate.of(2026, 10, 17), vtopDate("17-10-2026"))
        assertEquals(LocalDate.of(2026, 10, 17), vtopDate("17-Oct-2026"))
        assertNull(vtopDate("-"))
    }

    @Test
    fun facultyNamesMatchLoosely() {
        assertEquals("ASHA RAMAN", facultyKey("Dr. Asha  Raman"))
        assertEquals("ASHA RAMAN", facultyKey("ASHA RAMAN"))
    }

    @Test
    fun remainingForCalculatesTillNextExamWindow() {
        // Mon 7 Sep to Fri 11 Sep instructional, Mon 14–Wed 16 Sep CAT-1, Thu 17–Fri 18 Sep instructional, Mon 21 Sep FAT.
        val days = (7..11).map { d -> CalDay(LocalDate.of(2026, 9, d), "Instructional", d - 6, null) } +
            (14..16).map { d -> CalDay(LocalDate.of(2026, 9, d), "CAT-1", 0, null) } +
            listOf(
                CalDay(LocalDate.of(2026, 9, 17), "Instructional", 4, null),
                CalDay(LocalDate.of(2026, 9, 18), "Instructional", 5, null),
                CalDay(LocalDate.of(2026, 9, 21), "FAT", 0, null),
            )
        val calWithCat = AcademicCalendar("VL1", LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 20), days)
        val row = AttendanceRow("BACHY105", "Applied Chemistry", "Theory", 30, 40, null, "VL1")
        val sem = dev.medriid.davit.vtop.Semester("VL1", "Fall 2026")
        val snapshot = Snapshot(sem, listOf(sem), listOf(row), listOf(course), 0, emptyList(), calendar = calWithCat)

        fun testSlot(day: Int, start: Int) = ClassSlot(course, Meeting(day, start, start + 50, "A1"), row)
        val testWeek = mapOf(1 to listOf(testSlot(1, 480)), 2 to listOf(testSlot(2, 540)), 5 to listOf(testSlot(5, 600)))
        val meetings = snapshot.meetingsFor(row, testWeek)

        // Mon 7 Sep 07:00 (before CAT-1): Mon, Tue, Fri = 3 classes left till CAT-1 starts on Mon 14 Sep.
        val beforeCat = LocalDateTime.of(2026, 9, 7, 7, 0)
        val endBefore = calWithCat.endFor(false, beforeCat.toLocalDate())!!
        var countBefore = 0
        var d = beforeCat.toLocalDate()
        while (!d.isAfter(endBefore)) {
            countBefore += countOn(meetings, calWithCat, d, beforeCat)
            d = d.plusDays(1)
        }
        assertEquals(3, countBefore)

        // Thu 17 Sep 07:00 (after CAT-1, before FAT): Thu (no class), Fri 18 Sep (1 class) = 1 class left till FAT.
        val afterCat = LocalDateTime.of(2026, 9, 17, 7, 0)
        val endAfter = calWithCat.endFor(false, afterCat.toLocalDate())!!
        var countAfter = 0
        d = afterCat.toLocalDate()
        while (!d.isAfter(endAfter)) {
            countAfter += countOn(meetings, calWithCat, d, afterCat)
            d = d.plusDays(1)
        }
        assertEquals(1, countAfter)
    }

    @Test
    fun snapshotCalendarFollowFlagBehaviors() {
        val snapshotJson = """
            {"v": 2, "semester": {"id": "VL20262701", "name": "Fall Semester 2026-27 - VLR"}, "semesters": [{"id": "VL20262701", "name": "Fall Semester 2026-27 - VLR"}], "attendance": [{"code": "BCSE303P", "name": "Operating Systems Lab", "type": "Lab Only", "attended": 11, "total": 15, "pct": 74, "classId": "VL2026270103806", "dSem": "VL20262701", "dReg": "24BKT0043", "dCourse": "VL_BCSE303P_00100", "dType": "LO"}], "timetable": [{"classId": "VL2026270103806", "code": "BCSE303P", "name": "Operating Systems Lab", "type": "Lab Only", "credits": 1, "slot": "L9+L10", "venue": "PRP233", "faculty": "MANOOV R"}], "syncedAt": 1794357248166, "changes": [], "calendar": {"sem": "VL20262701", "first": "2026-07-06", "lastTheory": "2026-11-03", "lastLab": "2026-11-03", "group": "General (Semester)", "days": [["2026-11-02", "Instructional", 1, ""], ["2026-11-03", "Instructional", 2, "Last instructional day for lab and theory classes"], ["2026-11-04", "Holiday", 0, "Deepavali"], ["2026-11-12", "None", 0, ""], ["2026-11-30", "None", 0, ""]]}}
        """.trimIndent()

        val snap = Snapshot.fromJson(snapshotJson)!!
        val cal = snap.calendar!!

        assertEquals(LocalDate.of(2026, 11, 3), cal.lastTheory)
        assertEquals(LocalDate.of(2026, 11, 3), cal.lastLab)

        val postSemesterDate = LocalDate.of(2026, 11, 11)
        assertTrue(cal.isSemesterOver(postSemesterDate))

        // When followCalendar = true, dates past semester end return null
        assertNull(cal.timetableDay(LocalDate.of(2026, 12, 1), followCalendar = true))
        // When followCalendar = false, fallback to weekday (Dec 1, 2026 is Tuesday = 2)
        assertEquals(2, cal.timetableDay(LocalDate.of(2026, 12, 1), followCalendar = false))

        val nowPostSemester = LocalDateTime.of(2026, 11, 11, 9, 0)

        // Strict plan stops after semester end
        val strictPlan = planFor(weekOf(snap), nowPostSemester, cal, followCalendar = true)
        assertTrue(strictPlan.today.isEmpty())
        assertNull(strictPlan.now)
        assertNull(strictPlan.next)

        // Unconstrained plan falls back past calendar end
        val unconstrainedPlan = planFor(weekOf(snap), nowPostSemester, cal, followCalendar = false)
        assertNotNull(unconstrainedPlan.next)
        assertEquals("BCSE303P", unconstrainedPlan.next!!.course.courseCode)

        // Remaining classes after semester end is 0
        val labRow = snap.attendance.first()
        assertEquals(0, snap.remainingFor(labRow, nowPostSemester))
    }
}
