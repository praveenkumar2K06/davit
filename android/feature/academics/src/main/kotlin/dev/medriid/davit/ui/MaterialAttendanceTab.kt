package dev.medriid.davit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.data.Snapshot
import dev.medriid.davit.data.afterMissing
import dev.medriid.davit.data.atRisk
import dev.medriid.davit.data.hint
import dev.medriid.davit.data.isLabType
import dev.medriid.davit.data.key
import dev.medriid.davit.data.percent
import dev.medriid.davit.data.remainingFor
import dev.medriid.davit.data.remarkFor
import dev.medriid.davit.data.titleCase
import dev.medriid.davit.data.vtopDate
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.material.MaterialButton
import dev.medriid.davit.design.printIn
import dev.medriid.davit.vtop.AttendanceDay
import dev.medriid.davit.vtop.AttendanceRow
import dev.medriid.davit.vtop.CourseRemark
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val FacultySection = 5

/** Dedicated Material 3 layout for Attendance Tab. */
@Composable
fun MaterialAttendanceTab(snapshot: Snapshot) {
    val rows = snapshot.attendance
    val risky = rows.count { it.atRisk }
    TabColumn {
        Text(
            text = "Attendance",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = CircleShape,
                color = if (!snapshot.attendanceExempt) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = "Threshold 75%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (!snapshot.attendanceExempt) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
            Surface(
                shape = CircleShape,
                color = if (risky > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = "$risky at risk",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (risky > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = "${rows.size - risky} safe",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        if (snapshot.attendanceExempt) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Your CGPA is 9 or above, so the 75% rule doesn't apply to you.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (rows.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            val nav = LocalNav.current
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MaterialButton("Plan a leave", { nav.go(Route.LeavePlanner) }, quiet = true, fillWidth = true, modifier = Modifier.weight(1f).sharedCard(Route.LeavePlanner.sharedKey))
                MaterialButton("Corrections", { nav.go(Route.Corrections) }, quiet = true, fillWidth = true, modifier = Modifier.weight(1f).sharedCard(Route.Corrections.sharedKey))
            }
        }

        Spacer(Modifier.height(18.dp))
        if (rows.isEmpty()) {
            Text("VTOP has no attendance for ${snapshot.semester.name} yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        val repo = LocalRepo.current
        val state by repo.state.collectAsState()
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            rows.sortedBy { it.percent }.forEachIndexed { index, row ->
                val faculty = snapshot.timetable.firstOrNull { it.classId.isNotEmpty() && it.classId == row.classId }?.faculty
                    ?: snapshot.timetable.firstOrNull { it.courseCode == row.courseCode && isLabType(it.courseType) == isLabType(row.classType) }?.faculty
                val remaining = remember(snapshot, row.key, state.calcBeforeExam) { snapshot.remainingFor(row, LocalDateTime.now(), state.calcBeforeExam) }
                key(row.key) { MaterialAttendanceCourseCard(row, remaining, snapshot.history[row.key].orEmpty(), faculty.orEmpty(), snapshot.feeds.remarkFor(row), Modifier.printIn(index)) }
            }
        }
    }
}

@Composable
private fun MaterialAttendanceCourseCard(row: AttendanceRow, remaining: Int?, history: List<AttendanceDay>, faculty: String, remark: CourseRemark?, modifier: Modifier = Modifier) {
    val risk = row.atRisk
    var open by rememberSaveable { mutableStateOf(false) }

    val haptics = LocalHapticFeedback.current
    var lastRisk by remember { mutableStateOf(risk) }
    LaunchedEffect(risk) {
        if (risk != lastRisk) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            lastRisk = risk
        }
    }

    val pctProgress = if (row.total == 0) 1f else row.attended.toFloat() / row.total
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = { open = !open },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.width(104.dp)) {
                    Text(
                        text = "${row.percent}%",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (risk) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "${row.attended}/${row.total} attended",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = row.courseName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                    Text(
                        text = "${row.courseCode} · ${if (isLabType(row.classType)) "Lab" else "Theory"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { pctProgress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = if (risk) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = row.hint(remaining).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (risk) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            if (remark != null && remark.remark.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (remark.serious) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "VTOP: ${remark.remark}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (remark.serious) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            AnimatedVisibility(open) {
                Column(Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "If you miss the next…",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.height(6.dp))
                    listOf(1, 2, 3, 5).forEach { n ->
                        val after = row.afterMissing(n)
                        Row(Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = "$n class${if (n > 1) "es" else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$after%" + if (4 * row.attended < 3 * (row.total + n) && !risk) "  ✕" else "",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (after < 75) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (history.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "History · last ${minOf(40, history.size)} classes",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.height(8.dp))
                        MaterialHistoryDots(history.takeLast(40))
                        val missed = history.filter { it.status == AttendanceDay.Status.Absent }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (missed.isEmpty()) "No absences."
                            else "Missed: " + missed.takeLast(12).joinToString(", ") { shortDate(it.date) } + if (missed.size > 12) " and earlier" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (faculty.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        val repo = LocalRepo.current
                        val nav = LocalNav.current
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.clickable {
                                repo.facultyFocus.value = faculty
                                repo.academicsFocus.value = FacultySection
                                nav.tab(2)
                            }
                        ) {
                            Text(
                                text = "Faculty: ${titleCase(faculty)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialHistoryDots(days: List<AttendanceDay>) {
    val inks = LocalInks.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        days.forEach { d ->
            Canvas(Modifier.size(12.dp)) {
                val r = size.minDimension / 2
                when (d.status) {
                    AttendanceDay.Status.Present -> drawCircle(inks.b, r)
                    AttendanceDay.Status.Absent -> drawCircle(inks.a, r)
                    AttendanceDay.Status.OnDuty -> drawCircle(inks.b, r - 1.dp.toPx(), style = Stroke(2.dp.toPx()))
                    AttendanceDay.Status.Other -> drawCircle(inks.rule, r * 0.5f)
                }
            }
        }
    }
}

private fun shortDate(text: String): String =
    vtopDate(text)?.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)) ?: text
