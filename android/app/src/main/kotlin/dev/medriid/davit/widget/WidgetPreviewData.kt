package dev.medriid.davit.widget

import dev.medriid.davit.data.Snapshot
import dev.medriid.davit.data.UiState
import dev.medriid.davit.vtop.Assignment
import dev.medriid.davit.vtop.AttendanceRow
import dev.medriid.davit.vtop.CourseAssignments
import dev.medriid.davit.vtop.Semester
import dev.medriid.davit.vtop.TimetableCourse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun sampleState(): UiState {
    val now = LocalDateTime.now()
    val sem = Semester("VL20262701", "Fall Semester 2026-27")
    val sampleAttendance = listOf(
        AttendanceRow(
            courseCode = "BXYZ101",
            courseName = "Applied Science",
            classType = "Embedded Theory",
            attended = 30,
            total = 40,
            vtopPercent = 75,
            classId = "VL2026270100101",
        ),
        AttendanceRow(
            courseCode = "BXYZ102",
            courseName = "Programming Basics",
            classType = "Embedded Lab",
            attended = 17,
            total = 25,
            vtopPercent = 68,
            classId = "VL2026270100102",
        ),
        AttendanceRow(
            courseCode = "BXYZ103",
            courseName = "Calculus",
            classType = "Embedded Theory",
            attended = 38,
            total = 44,
            vtopPercent = 87,
            classId = "VL2026270100103",
        ),
        AttendanceRow(
            courseCode = "BXYZ104",
            courseName = "Technical English",
            classType = "Embedded Theory",
            attended = 21,
            total = 26,
            vtopPercent = 81,
            classId = "VL2026270100104",
        ),
    )
    val sampleTimetable = listOf(
        TimetableCourse(
            classId = "VL2026270100101",
            courseCode = "BXYZ101",
            courseName = "Applied Science",
            courseType = "Embedded Theory",
            credits = 3.0,
            slot = "A1+TA1",
            venue = "SJT-401",
            faculty = "ASHA RAMAN",
        ),
        TimetableCourse(
            classId = "VL2026270100102",
            courseCode = "BXYZ102",
            courseName = "Programming Basics",
            courseType = "Embedded Lab",
            credits = 1.0,
            slot = "L31+L32",
            venue = "SJT-210",
            faculty = "VIKRAM SETH",
        ),
    )
    val sampleAssignments = listOf(
        CourseAssignments(
            classId = "VL2026270100102",
            courseCode = "BXYZ102",
            courseName = "Programming Basics",
            courseType = "LO",
            items = listOf(
                Assignment(
                    title = "Lab Assessment 2",
                    max = 10.0,
                    weightage = 10.0,
                    due = now.plusDays(2).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH)),
                    updated = null,
                    code = "AST02",
                ),
            ),
        ),
    )
    val sampleSnapshot = Snapshot(
        semester = sem,
        semesters = listOf(sem),
        attendance = sampleAttendance,
        timetable = sampleTimetable,
        syncedAt = System.currentTimeMillis() - 15 * 60 * 1000L,
        changes = emptyList(),
        assignments = sampleAssignments,
    )
    return UiState(
        signedIn = true,
        snapshot = sampleSnapshot,
    )
}
