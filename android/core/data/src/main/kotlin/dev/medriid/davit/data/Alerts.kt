package dev.medriid.davit.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AlertChannel(val id: String, val title: String) {
    Attendance("attendance", "Attendance"),
    Marks("marks", "New marks"),
    Deadlines("deadlines", "Deadlines and exams"),
    Notices("notices", "Registration and VTOP notices"),
    Classes("classes", "Before class"),
    Meals("meals", "Meals"),
}

/** One notification. [key] identifies the event, so it's only ever posted once. */
data class Alert(val channel: AlertChannel, val key: String, val title: String, val text: String)

/** New circulars, new projects in the areas you follow, and anything that has just opened on VTOP. */
fun watchAlerts(old: HomeFeeds, new: HomeFeeds, today: LocalDate, prefs: AlertPrefs): List<Alert> {
    val out = mutableListOf<Alert>()
    if (old.circulars.isNotEmpty()) {
        val seen = old.circulars.map { it.id }.toSet()
        out += few(new.circulars.filter { it.id !in seen }, "circulars", { "circ:${it.id}" }, { "New circular" to it.title }) { n ->
            "$n new circulars" to "Open Circulars to read them."
        }
    }
    if (old.projects.isNotEmpty() && prefs.projectAreas.isNotEmpty()) {
        val seen = old.projects.map { it.id }.toSet()
        val fresh = new.projects.filter { it.id !in seen && it.area in prefs.projectAreas }
        out += few(fresh, "projects", { "proj:${it.id}" }, { "New project · ${it.area}" to it.title }) { n ->
            "$n new faculty projects" to "In the areas you follow."
        }
    }
    val before = old.windows.associateBy { it.id }
    for ((id, title, open) in new.windows) {
        if (open && before[id]?.open == false) {
            out += Alert(AlertChannel.Notices, "win:$id|$today", "$title is open", "VTOP has opened it. Check the dates and apply there.")
        }
    }
    return out
}

/** One alert each for up to three, or a single one for the lot, so a busy day doesn't fill the shade. */
private fun <T> few(
    items: List<T>,
    name: String,
    key: (T) -> String,
    one: (T) -> Pair<String, String>,
    many: (Int) -> Pair<String, String>,
): List<Alert> = if (items.size <= 3) {
    items.map { i -> one(i).let { (title, text) -> Alert(AlertChannel.Notices, key(i), title, text) } }
} else {
    many(items.size).let { (title, text) -> listOf(Alert(AlertChannel.Notices, "$name:" + items.joinToString("|", transform = key).hashCode(), title, text)) }
}

/**
 * What a background sync should tell you, from the snapshot before it and after it.
 * Nothing on a first sync ([old] null), so signing in doesn't fire a flood.
 */
fun alertsFor(
    old: Snapshot?,
    new: Snapshot,
    today: LocalDate,
    reminders: List<Reminder> = emptyList(),
    prefs: AlertPrefs = AlertPrefs(),
): List<Alert> {
    if (old == null) return emptyList()
    val out = mutableListOf<Alert>()

    // Your own reminders: today and tomorrow, like assignments.
    for ((id, title, date) in reminders) {
        if (!prefs.deadlines) break
        val days = date.toEpochDay() - today.toEpochDay()
        if (days == 0L || days == 1L) {
            out += Alert(AlertChannel.Deadlines, "rem:$id|$days", (if (days == 0L) "Today: " else "Tomorrow: ") + title, "Your reminder")
        }
    }

    // Attendance, as you set it up: crossing below 75%, below your own line, or down to
    // your last few skips. Each once per change; muted courses stay quiet.
    if (prefs.attendance && !new.attendanceExempt && old.semester.id == new.semester.id) {
        val before = old.attendance.associateBy { it.courseCode + "|" + it.classType }
        for (row in new.attendance) {
            val prev = before[row.courseCode + "|" + row.classType] ?: continue
            if (prev.attended == row.attended && prev.total == row.total) continue
            if (prefs.mutes(row)) continue
            val tag = "${row.courseCode}|${row.classType}|${row.attended}/${row.total}"
            val line = prefs.warnBelow
            when {
                row.atRisk && !prev.atRisk -> out += Alert(
                    AlertChannel.Attendance, "risk:$tag",
                    "${row.courseName} is below 75%",
                    "${row.percent}% (${row.attended}/${row.total}). Attend the next ${row.mustAttend} to get back.",
                )
                line > 75 && !row.atRisk && below(row, line) && !below(prev, line) -> out += Alert(
                    AlertChannel.Attendance, "line$line:$tag",
                    "${row.courseName} is below $line%",
                    "${row.percent}% (${row.attended}/${row.total}). Still above 75%: " +
                        (if (row.canSkipNow == 0) "no skips left." else "${row.canSkipNow} skip${if (row.canSkipNow == 1) "" else "s"} left."),
                )
                prefs.skipsLeft >= 0 && !row.atRisk && row.canSkipNow <= prefs.skipsLeft && prev.canSkipNow > prefs.skipsLeft -> out += Alert(
                    AlertChannel.Attendance, "edge:$tag",
                    if (row.canSkipNow == 0) "No skips left in ${row.courseName}" else "${row.canSkipNow} skip${if (row.canSkipNow == 1) "" else "s"} left in ${row.courseName}",
                    "${row.attended}/${row.total}. " +
                        (if (row.canSkipNow == 0) "Missing the next class takes you below 75%." else "Miss ${row.canSkipNow + 1} more and you're below 75%."),
                )
            }
        }
    }

    // Attendance remarks: a course that turns Debarred or Revoked (or changes which exams it
    // covers). Not on the first remarks fetch: that state is on screen already.
    if (prefs.remarks && old.feeds.remarks.isNotEmpty()) {
        val before = old.feeds.remarks.associateBy { it.code + "|" + it.type }
        for (r in new.feeds.remarks) {
            if (!r.serious) continue
            val prev = before[r.code + "|" + r.type]
            if (prev != null && prev.remark == r.remark) continue
            out += Alert(
                AlertChannel.Attendance, "remark:${r.code}|${r.type}|${r.remark}",
                (if (r.debarred) "Debarred: " else "Revoked: ") + r.name.ifBlank { r.code },
                "VTOP's remark: ${r.remark}" + (r.attendance?.let { " · attendance ${num(it)}%" } ?: ""),
            )
        }
    }

    // Registration: a window VTOP has just announced.
    if (prefs.notices && old.feeds.dailyAt > 0) {
        val clock = DateTimeFormatter.ofPattern("EEE d MMM, h:mm a", Locale.ENGLISH)
        for ((_, semester, start1, end) in new.feeds.registration) {
            val start = start1 ?: continue
            if (start.toLocalDate().isBefore(today) || old.feeds.registration.any { it.start == start }) continue
            out += Alert(
                AlertChannel.Notices, "reg:$start",
                "${semester.ifBlank { "Course" }} registration",
                "Opens ${start.format(clock)}" + (end?.let { " · closes ${it.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))}" } ?: ""),
            )
        }
    }

    // Circulars, projects and registration windows, once there's an earlier reading to compare with.
    if (prefs.notices && old.feeds.watchAt > 0) out += watchAlerts(old.feeds, new.feeds, today, prefs)

    // Marks: an item that has a score now and didn't before. Not on the first marks fetch, nor
    // on a change of semester, where every mark would look new.
    if (prefs.marks && old.marks.isNotEmpty() && old.semester.id == new.semester.id) {
        val seen = old.marks.flatMap { c -> c.items.filter { it.scored != null }.map { c.classId + "|" + it.title } }.toSet()
        for ((classId, _, courseName, _, items) in new.marks) for ((title, max, _, _, scored1) in items) {
            val scored = scored1 ?: continue
            if ((classId + "|" + title) in seen) continue
            val score = "${num(scored)}/${max?.let(::num) ?: "?"}"
            out += Alert(AlertChannel.Marks, "mark:$classId|$title", "New mark · $courseName", "$title: $score")
        }
    }

    // Deadlines: unsubmitted assignments due today or tomorrow, exams tomorrow.
    for ((classId, _, courseName, _, items) in new.assignments) for ((title, _, _, due1, updated) in items) {
        if (!prefs.deadlines) break
        if (updated != null) continue
        val due = vtopDate(due1) ?: continue
        val days = due.toEpochDay() - today.toEpochDay()
        if (days == 0L || days == 1L) {
            out += Alert(
                AlertChannel.Deadlines, "due:$classId|$title|$due1|$days",
                (if (days == 0L) "Due today: " else "Due tomorrow: ") + title,
                courseName + " · not submitted yet",
            )
        }
    }
    for ((classId, _, courseName, type, date, _, time, venue, seat) in new.exams) {
        if (!prefs.exams) break
        val day = vtopDate(date) ?: continue
        if (day.toEpochDay() - today.toEpochDay() != 1L) continue
        out += Alert(
            AlertChannel.Deadlines, "exam:$classId|$type|$date",
            "${type.ifBlank { "Exam" }} tomorrow: $courseName",
            listOf(time, venue, seat.takeIf { it.isNotBlank() }?.let { "Seat $it" })
                .filter { !it.isNullOrBlank() }.joinToString(" · "),
        )
    }
    return out
}

/** A reminder to post at [at]. */
data class ClassReminder(val at: LocalDateTime, val alert: Alert)

/**
 * Before-class reminders for the classes starting in the next [hours], [AlertPrefs.beforeClass]
 * minutes ahead: every class, or only the ones near the line. Each says the room and what
 * missing it would cost. Holidays and swapped day orders follow the calendar, as Today does.
 */
fun classReminders(
    snapshot: Snapshot,
    prefs: AlertPrefs,
    now: LocalDateTime,
    hours: Long = 36,
    week: Map<Int, List<ClassSlot>> = weekOf(snapshot),
): List<ClassReminder> {
    if (prefs.beforeClass <= 0) return emptyList()

    val end = now.plusHours(hours)
    val out = ArrayList<ClassReminder>()
    var day = now.toLocalDate()
    while (!day.atStartOfDay().isAfter(end)) {
        val order = snapshot.calendar?.timetableDay(day, true) ?: if (snapshot.calendar == null) day.dayOfWeek.value else null
        for (slot in order?.let { week[it] }.orEmpty()) {
            val start = day.atTime(slot.meeting.start / 60, slot.meeting.start % 60)
            val at = start.minusMinutes(prefs.beforeClass.toLong())
            if (!at.isAfter(now) || start.isAfter(end)) continue
            val row = slot.attendance?.takeIf { !snapshot.attendanceExempt && prefs.attendance && !prefs.mutes(it) }
            if (prefs.beforeClassNearLine && (row == null || !prefs.nearLine(row))) continue
            val title = "${slot.course.courseName} at ${clock(slot.meeting.start)}" + (slot.course.venue.ifBlank { null }?.let { " · $it" } ?: "")
            val text = when {
                row == null -> "In ${prefs.beforeClass} min · " + if (slot.isLab) "Lab" else "Theory"
                row.atRisk -> "You're at ${row.percent}%. Attend the next ${row.mustAttend} to get back to 75%."
                row.canSkipNow == 0 -> "${row.percent}% now. Miss this one and you drop to ${row.afterMissing(1)}%, below 75."
                else -> "${row.percent}% · ${row.canSkipNow} skip${if (row.canSkipNow == 1) "" else "s"} left."
            }
            out += ClassReminder(at, Alert(AlertChannel.Classes, "class:${slot.course.classId}|${slot.meeting.slot}|$start", title, text))
        }
        day = day.plusDays(1)
    }
    return out.sortedBy { it.at }
}
