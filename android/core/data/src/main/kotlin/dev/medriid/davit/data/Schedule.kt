package dev.medriid.davit.data

import dev.medriid.davit.vtop.AttendanceRow
import dev.medriid.davit.vtop.Meeting
import dev.medriid.davit.vtop.SlotTimes
import dev.medriid.davit.vtop.TimetableCourse
import java.math.BigInteger
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

const val THRESHOLD = 0.75

/** One class in the week, with its attendance row when we can match it. */
data class ClassSlot(val course: TimetableCourse, val meeting: Meeting, val attendance: AttendanceRow?) {
    val isLab: Boolean get() = isLabType(course.courseType) || meeting.slot.startsWith("L")
}

/** Matches a timetable course to its attendance row: class id first, then code + theory/lab. */
fun matchAttendance(course: TimetableCourse, rows: List<AttendanceRow>): AttendanceRow? =
    rows.firstOrNull { it.classId.isNotEmpty() && it.classId == course.classId }
        ?: rows.firstOrNull { it.courseCode == course.courseCode && isLabType(it.classType) == isLabType(course.courseType) }
        ?: rows.firstOrNull { it.courseCode == course.courseCode }

/** Every class of the week, by weekday (1 = Monday), in time order. */
fun weekOf(snapshot: Snapshot): Map<Int, List<ClassSlot>> =
    snapshot.timetable
        .flatMap { course ->
            val att = matchAttendance(course, snapshot.attendance)
            SlotTimes.meetings(course.slot).map { ClassSlot(course, it, att) }
        }
        .groupBy { it.meeting.day }
        .mapValues { (_, v) -> v.sortedBy { it.meeting.start } }

data class TodayPlan(
    val today: List<ClassSlot>,
    val now: ClassSlot?,
    /** The next class, today or on a later day. */
    val next: ClassSlot?,
    /** Days from today to [next] (0 = today). */
    val nextInDays: Int,
    val done: Int,
    /** The date [next] falls on (its weekday can differ from the slot's when a day order swaps it). */
    val nextDate: LocalDate? = null,
    /** Today's calendar entry, when the calendar knows it (a holiday, an exam day…). */
    val calendarDay: CalDay? = null,
)

/**
 * Now and next. With a [calendar], holidays and exam days have no classes and a
 * substituted day (a Saturday running Monday's order) uses that weekday's timetable.
 */
fun planFor(week: Map<Int, List<ClassSlot>>, at: LocalDateTime, calendar: AcademicCalendar? = null, followCalendar: Boolean = true): TodayPlan {
    val date = at.toLocalDate()
    fun classesOn(d: LocalDate) = classesOn(week, d, calendar, followCalendar)
    val minute = at.hour * 60 + at.minute
    val today = classesOn(date)
    val now = today.firstOrNull { minute >= it.meeting.start && minute < it.meeting.end }
    var next = today.firstOrNull { it.meeting.start > minute }
    var inDays = 0
    if (next == null) {
        for (d in 1..21) {
            val first = classesOn(date.plusDays(d.toLong())).firstOrNull()
            if (first != null) {
                next = first
                inDays = d
                break
            }
        }
    }
    return TodayPlan(
        today, now, next, inDays, today.count { it.meeting.end <= minute },
        next?.let { date.plusDays(inDays.toLong()) }, calendar?.day(date),
    )
}

/**
 * The classes on [date]: the timetable of the weekday the calendar says it runs (a
 * Saturday on Wednesday's order gets Wednesday's classes); none on holidays and exam days.
 */
fun classesOn(week: Map<Int, List<ClassSlot>>, date: LocalDate, calendar: AcademicCalendar?, followCalendar: Boolean = true): List<ClassSlot> {
    val wd = if (calendar != null) calendar.timetableDay(date, followCalendar) else date.dayOfWeek.value
    val slots = wd?.let { week[it] }.orEmpty()
    if (followCalendar && calendar != null && slots.isNotEmpty()) {
        return slots.filter { slot ->
            val end = if (slot.isLab) calendar.lastLab ?: calendar.lastTheory else calendar.lastTheory ?: calendar.lastLab
            end == null || !date.isAfter(end)
        }
    }
    return slots
}

/** The dates the Week tab shows: Monday to Sunday of this week, or of the coming one on a Sunday. */
fun weekDates(today: LocalDate): List<LocalDate> {
    val monday = if (today.dayOfWeek == DayOfWeek.SUNDAY) today.plusDays(1) else today.with(DayOfWeek.MONDAY)
    return (0..6).map { monday.plusDays(it.toLong()) }
}

/**
 * What the calendar changes about [date], in a few words ("Runs Wednesday's timetable",
 * "Holiday: Deepavali", "CAT-2: no regular classes"); null for an ordinary day.
 */
fun AcademicCalendar.dayNote(date: LocalDate): String? {
    val d = day(date) ?: return null
    return when {
        d.instructional -> d.dayOrder.takeIf { it in 1..7 && it != date.dayOfWeek.value }
            ?.let { "Runs ${DayOfWeek.of(it).getDisplayName(TextStyle.FULL, Locale.ENGLISH)}'s timetable" }
        d.exam -> "${d.type}: no regular classes"
        d.type.equals("Holiday", ignoreCase = true) -> d.note?.let { "Holiday: $it" }
        d.type.equals("None", ignoreCase = true) -> null
        else -> d.note ?: d.type
    }
}

/**
 * Which weekday's timetable each past date actually followed, read off your attendance
 * history: a class in slot A1 (which meets on Monday) on a Saturday means that Saturday
 * ran Monday's order. Only dates whose slots agree on one weekday are kept.
 */
fun Snapshot.observedDayOrders(): Map<LocalDate, Int> =
    history.values.asSequence().flatten()
        .mapNotNull { d ->
            // VTOP's Day/Time column says the order outright; the slot is the fallback.
            val days = d.dayOrder?.let { setOf(it) } ?: SlotTimes.meetings(d.slot).map { m -> m.day }.toSet()
            vtopDate(d.date)?.let { it to days }
        }
        .filter { (_, days) -> days.isNotEmpty() }
        .groupBy({ it.first }, { it.second })
        .mapNotNull { (date, sets) ->
            // Every class that day must fit one weekday; a lab pair and a theory slot can both point to it.
            sets.reduce { a, b -> a intersect b }.singleOrNull()?.let { date to it }
        }.toList()
        .toMap()

/** The calendar, corrected wherever your attendance shows a past day followed another order. */
fun Snapshot.effectiveCalendar(): AcademicCalendar? {
    val cal = calendar ?: return null
    val seen = observedDayOrders()
    if (seen.isEmpty()) return cal
    return cal.copy(days = cal.days.map { d ->
        val actual = seen[d.date] ?: return@map d
        if (d.instructional && d.dayOrder == actual) d else d.copy(type = "Instructional", dayOrder = actual)
    })
}

/**
 * Classes of [row]'s course still to come before the next exam (or end of semester),
 * from [at] to the start of the next exam day (or last theory/lab day).
 * Null without a calendar, since the end date is unknown.
 */
fun Snapshot.remainingFor(row: AttendanceRow, at: LocalDateTime, calcBeforeExam: Boolean = true): Int? {
    val cal = effectiveCalendar() ?: return null
    val meetings = meetingsFor(row)
    if (meetings.isEmpty()) return null
    val end = cal.endFor(isLabType(row.classType), if (calcBeforeExam) at.toLocalDate() else null) ?: return null
    var count = 0
    var d = at.toLocalDate()
    while (!d.isAfter(end)) {
        count += countOn(meetings, cal, d, at)
        d = d.plusDays(1)
    }
    return count
}

/** The weekly meetings of [row]'s course (a lab session is one meeting, as VTOP counts it). */
fun Snapshot.meetingsFor(row: AttendanceRow, week: Map<Int, List<ClassSlot>> = weekOf(this)): List<Meeting> =
    week.values.flatten().filter { it.attendance?.key == row.key }.map { it.meeting }

/** The last teaching day for labs or theory before the next exam (if one starts sooner), or for the full semester. */
fun AcademicCalendar.endFor(lab: Boolean, from: LocalDate? = null): LocalDate? {
    val semesterEnd = if (lab) lastLab else lastTheory
    if (from == null) return semesterEnd
    val nextExamStart = examWindows().firstOrNull { !it.second.isBefore(from) }?.second
    return if (nextExamStart != null && (semesterEnd == null || nextExamStart.isBefore(semesterEnd))) {
        nextExamStart
    } else {
        semesterEnd
    }
}

/**
 * How many of [meetings] are held on [d]: by the calendar's day order (none on a holiday
 * or exam day), or the date's own weekday without a calendar. On [at]'s day, only the
 * ones that haven't started yet.
 */
fun countOn(meetings: List<Meeting>, cal: AcademicCalendar?, d: LocalDate, at: LocalDateTime): Int {
    val wd = (if (cal != null) cal.timetableDay(d) else d.dayOfWeek.value) ?: return 0
    val minute = at.hour * 60 + at.minute
    return meetings.count { it.day == wd && (d != at.toLocalDate() || it.start > minute) }
}

/** How many of the [remaining] classes you can miss and still finish at or above 75% (ceil). Negative: out of reach. */
fun AttendanceRow.skipTillEnd(remaining: Int): Int = Math.floorDiv(100 * attended + 26 * remaining - 74 * total - 1, 100)

/** The hint under a course: a semester budget when [remaining] is known, else the next-classes version. */
fun AttendanceRow.hint(remaining: Int?): String {
    if (remaining == null) return hint()
    val k = skipTillEnd(remaining)
    return when {
        remaining == 0 -> if (atRisk) "no classes left to reach 75%" else "no classes left"
        k < 0 -> "75% can't be reached with $remaining classes left"
        atRisk -> "attend the next $mustAttend to reach 75% ($remaining left)"
        k >= remaining -> if (remaining == 1) "can skip the last one" else "can skip all $remaining left"
        else -> "can skip $k of the $remaining left"
    }
}

fun clock(minutes: Int) = "%02d:%02d".format(minutes / 60, minutes % 60)
