package dev.medriid.davit.ui

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import dev.medriid.davit.data.GradePoints
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.data.Snapshot
import dev.medriid.davit.data.facultyKey
import dev.medriid.davit.data.isLabType
import dev.medriid.davit.data.neededAverage
import dev.medriid.davit.data.projectCgpa
import dev.medriid.davit.data.showOnMap
import dev.medriid.davit.data.titleCase
import dev.medriid.davit.design.ChoiceRow
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.design.highlight
import dev.medriid.davit.design.material.MaterialButton
import dev.medriid.davit.design.material.MaterialField
import dev.medriid.davit.design.printIn
import dev.medriid.davit.ui.map.MapChip
import dev.medriid.davit.vtop.Assignment
import dev.medriid.davit.vtop.CourseMarks
import dev.medriid.davit.vtop.Exam
import dev.medriid.davit.vtop.Person
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.floor

private val AcademicsSegments = listOf(
    "Marks" to RisoIcon.Marks,
    "Assignments" to RisoIcon.Assignments,
    "Exams" to RisoIcon.Exams,
    "Grades" to RisoIcon.Grades,
    "Materials" to RisoIcon.Materials,
    "Faculty" to RisoIcon.Faculty,
)

/** Dedicated Material 3 layout for Academics tab. */
@Composable
fun MaterialAcademicsTab(snapshot: Snapshot) {
    var segment by rememberSaveable { mutableIntStateOf(0) }
    val repo = LocalRepo.current
    val focus by repo.academicsFocus.collectAsState()
    LaunchedEffect(focus) {
        focus?.let { segment = it; repo.academicsFocus.value = null }
    }
    val motion = dev.medriid.davit.design.rememberMotionEnabled()
    TabColumn {
        Text(
            text = "Academics",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.padding(top = 8.dp, bottom = 14.dp),
        )
        val haptics = dev.medriid.davit.design.rememberHaptics()
        MaterialSegmentBar(AcademicsSegments, segment) {
            if (it != segment) haptics.tick()
            segment = it
        }
        Spacer(Modifier.height(14.dp))
        AnimatedContent(
            segment,
            transitionSpec = {
                if (!motion) fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                else fadeIn(tween(180)) togetherWith fadeOut(tween(120))
            },
            label = "segment",
        ) { s ->
            Column {
                when (s) {
                    0 -> MaterialMarksSegment(snapshot)
                    1 -> MaterialAssignmentsSegment(snapshot)
                    2 -> MaterialExamsSegment(snapshot.exams)
                    3 -> MaterialGradesSegment(snapshot)
                    4 -> MaterialsSegment(snapshot)
                    else -> MaterialFacultySegment(snapshot)
                }
            }
        }
    }
}

@Composable
private fun MaterialSegmentBar(items: List<Pair<String, RisoIcon>>, selected: Int, onSelect: (Int) -> Unit) {
    val motion = dev.medriid.davit.design.rememberMotionEnabled()
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items.forEachIndexed { i, (label, icon) ->
            val on = i == selected
            val grow by animateFloatAsState(
                if (on) 2.8f else 1f,
                if (motion) spring(dampingRatio = 0.8f, stiffness = 500f) else snap(),
                label = "segment",
            )
            Surface(
                onClick = { onSelect(i) },
                shape = CircleShape,
                color = if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(grow)
                    .fillMaxHeight()
                    .semantics {
                        contentDescription = label
                        this.selected = on
                    },
            ) {
                Row(
                    Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconGlyph(
                        icon,
                        color = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    if (on) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = label,
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }
        }
    }
}

// ---- marks -------------------------------------------------------------------------

@Composable
private fun MaterialMarksSegment(snapshot: Snapshot) {
    if (snapshot.marks.isEmpty()) {
        Text("No marks for ${snapshot.semester.name} yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val courses = snapshot.marks.sortedBy { it.items.none { m -> m.weighted != null } }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        courses.forEach { MaterialCourseMarksCard(it) }
    }
}

@Composable
private fun MaterialCourseMarksCard(course: CourseMarks) {
    val counted = course.items.filter { it.weighted != null && it.weightage != null }
    val got = counted.sumOf { it.weighted!! }
    val outOf = counted.sumOf { it.weightage!! }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                    Text(
                        text = "${course.courseCode} · ${course.courseType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (counted.isNotEmpty()) {
                    Spacer(Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = fmt(got),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "of ${fmt(outOf)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (course.items.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Nothing marked yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            course.items.forEach { m ->
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = m.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                        val note = listOfNotNull(
                            m.status.takeIf { it.isNotBlank() && !it.equals("Present", true) },
                            m.remark.ifBlank { null }
                        ).joinToString(" · ")
                        if (note.isNotEmpty()) {
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (m.scored == null) "–" else "${fmt(m.scored!!)}/${m.max?.let(::fmt) ?: "?"}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (m.weightage != null) {
                            Text(
                                text = "${m.weighted?.let(::fmt) ?: "–"} of ${fmt(m.weightage!!)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---- assignments -------------------------------------------------------------------

@Composable
private fun MaterialAssignmentsSegment(snapshot: Snapshot) {
    val courses = snapshot.assignments.filter { it.items.isNotEmpty() }
    val repo = LocalRepo.current
    MaterialButton("Scan to PDF", { repo.scanner.open(dev.medriid.davit.scan.ScanTarget()) }, quiet = true, fillWidth = true, modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp))
    if (courses.isEmpty()) {
        Text("No assignments for ${snapshot.semester.name} yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val today = LocalDate.now()
    val ordered = courses.sortedWith(
        compareBy(nullsLast()) { c ->
            c.items.filter { it.updated == null }.mapNotNull { it.dueDay() }.filter { !it.isBefore(today) }.minOrNull()
        },
    )
    val nav = LocalNav.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ordered.forEachIndexed { ci, c ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().printIn(ci),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = c.courseName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                    Text(
                        text = "${c.courseCode} · ${c.courseType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    c.items.forEachIndexed { i, a ->
                        val route = Route.Assignment(c.classId, a.routeCode(i))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .sharedCard(route.sharedKey)
                                .clickable(role = Role.Button) { nav.go(route) }
                                .padding(top = 10.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = a.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2
                                )
                                val bits = listOfNotNull(
                                    a.dueDay()?.let { "Due ${it.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))}" },
                                    a.weightage?.let { "${fmt(it)}%" },
                                    "question paper".takeIf { a.hasQuestion },
                                    "upload open".takeIf { a.canUpload && a.updated == null },
                                )
                                if (bits.isNotEmpty()) {
                                    Text(
                                        text = bits.joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            MaterialAssignmentChip(a, today)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialAssignmentChip(a: Assignment, today: LocalDate) {
    val day = a.dueDay()
    val days = day?.let { ChronoUnit.DAYS.between(today, it).toInt() }
    when {
        a.updated != null -> Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                text = "✓ Submitted",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
        days == null -> Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text(
                text = "No date",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
        days < 0 -> Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
        ) {
            Text(
                text = "Overdue",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
        days == 0 -> Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
        days == 1 -> Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
        ) {
            Text(
                text = "Tomorrow",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
        else -> Surface(
            shape = CircleShape,
            color = if (days <= 7) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text(
                text = "$days days",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (days <= 7) FontWeight.Bold else FontWeight.Normal),
                color = if (days <= 7) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

// ---- exams -------------------------------------------------------------------------

@Composable
private fun MaterialExamsSegment(exams: List<Exam>) {
    if (exams.isEmpty()) {
        Text("No exams scheduled yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val today = LocalDate.now()
    val (done, upcoming) = exams.partition { e -> e.day()?.isBefore(today) == true }
    val nav = LocalNav.current
    if (upcoming.isNotEmpty()) {
        MaterialButton("Exam board: every paper at a glance", { nav.go(Route.ExamBoard) }, quiet = true, fillWidth = true, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        upcoming.sortedWith(compareBy(nullsLast()) { it.day() }).groupBy { it.type }.forEach { (type, list) ->
            Text(
                text = type.ifBlank { "Exams" },
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(top = 6.dp)
            )
            list.forEach { MaterialExamCard(it, today) }
        }
        if (done.isNotEmpty()) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(top = 10.dp)
            )
            done.sortedByDescending { it.day() }.forEach { MaterialExamCard(it, today, Modifier.alpha(0.55f)) }
        }
    }
}

@Composable
private fun MaterialExamCard(exam: Exam, today: LocalDate, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val day = exam.day()
    val days = day?.let { ChronoUnit.DAYS.between(today, it).toInt() }
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.width(72.dp)
            ) {
                Column(
                    Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day?.dayOfMonth?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = day?.format(DateTimeFormatter.ofPattern("MMM · EEE", Locale.ENGLISH)) ?: exam.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = exam.courseName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listOf(exam.time, exam.venue).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (exam.seat.isNotBlank()) {
                    Text(
                        text = "Seat ${exam.seat}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (days != null && days >= 0) {
                    val context = LocalContext.current
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.clickable { addToCalendar(context, exam) }
                    ) {
                        Text(
                            text = "📅 Add to calendar",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            if (days != null && days >= 0) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = if (days <= 7) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = when (days) {
                            0 -> "Today"
                            1 -> "Tomorrow"
                            else -> "$days days"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (days <= 7) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

// ---- faculty -----------------------------------------------------------------------

@Composable
private fun MaterialFacultySegment(snapshot: Snapshot) {
    val context = LocalContext.current
    val repo = LocalRepo.current
    val nav = LocalNav.current
    val inks = LocalInks.current
    val campus by repo.campus.state.collectAsState()
    LaunchedEffect(Unit) { repo.campus.refresh(dev.medriid.davit.data.Section.People) }
    var query by rememberSaveable { mutableStateOf("") }
    val wanted by repo.facultyFocus.collectAsState()
    LaunchedEffect(wanted) {
        wanted?.let {
            query = titleCase(it)
            repo.facultyFocus.value = null
        }
    }

    val details = remember(snapshot.faculty) { snapshot.faculty.associateBy { facultyKey(it.name) } }
    val courses = remember(snapshot.timetable) {
        snapshot.timetable.filter { it.courseCode.isNotBlank() }.groupBy { it.courseCode }.values.toList()
    }
    val index = remember(courses, details) {
        dev.medriid.davit.search.Fuzzy.Index(
            courses,
            listOf(
                dev.medriid.davit.search.Fuzzy.Field("course", 3f),
                dev.medriid.davit.search.Fuzzy.Field("faculty", 2.5f),
                dev.medriid.davit.search.Fuzzy.Field("code", 2f),
                dev.medriid.davit.search.Fuzzy.Field("more", 1f),
            ),
        ) { parts ->
            listOf(
                parts.first().courseName,
                parts.joinToString(" ") { titleCase(it.faculty) },
                parts.first().courseCode,
                parts.joinToString(" ") { listOf(it.slot, it.venue, it.courseType, details[facultyKey(it.faculty)]?.department.orEmpty()).joinToString(" ") },
            )
        }
    }
    val hits = remember(query, index) { if (query.isBlank()) null else index.search(query) }
    val shown = hits?.map { it.item } ?: courses

    MaterialField(query, { query = it }, "Course, code or any teacher", search = true, modifier = Modifier.fillMaxWidth(), inks = inks)
    Spacer(Modifier.height(14.dp))
    if (courses.isEmpty()) {
        Text("No timetable for ${snapshot.semester.name} yet. Type three letters of any teacher's name to look them up on VTOP.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        if (shown.isEmpty()) Text("None of your courses match \"$query\".", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            shown.forEach { parts ->
                val hit = hits?.firstOrNull { it.item === parts }
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        val name = parts.first().courseName
                        BasicText(
                            highlight(name, hit?.ranges?.get("course").orEmpty(), inks.a),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                            maxLines = 2,
                        )
                        Text(parts.first().courseCode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        parts.groupBy { facultyKey(it.faculty) }.values.forEach { same ->
                            val who = same.first().faculty
                            val f = details[facultyKey(who)]
                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                same.joinToString(" + ") { if (isLabType(it.courseType)) "Lab" else if ("theory" in it.courseType.lowercase() || it.courseType.isBlank()) "Theory" else it.courseType } +
                                    " · " + same.joinToString(", ") { it.slot },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                            )
                            Spacer(Modifier.height(4.dp))
                            BasicText(
                                highlight(titleCase(who.ifBlank { "Not assigned yet" }), hit?.ranges?.get("faculty").orEmpty().takeIf { parts.size == 1 }.orEmpty(), inks.a),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface),
                            )
                            if (f != null) {
                                listOfNotNull(f.designation.ifBlank { null }, listOf(f.department, f.school).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { null })
                                    .forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2) }
                            }
                            if (who.isNotBlank()) FacultyRatingLink(f?.empId, who, Modifier.padding(top = 8.dp))
                            if (f?.cabin?.isNotBlank() == true || f?.email?.isNotBlank() == true) {
                                Spacer(Modifier.height(8.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (f.cabin.isNotBlank()) MapChip(f.cabin, label = "Cabin ${f.cabin}") { nav.showOnMap(repo, f.cabin) }
                                    if (f.email.isNotBlank()) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.clickable {
                                                runCatching {
                                                    context.startActivity(Intent(Intent.ACTION_SENDTO, ("mailto:" + f.email).toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = f.email,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    VtopFacultySearch(query)

    if (query.isBlank() && (campus.proctor != null || campus.hodDean.isNotEmpty())) {
        SectionTitle("Your proctor")
        campus.proctor?.let {
            MaterialPersonCard(it, 0)
            Spacer(Modifier.height(10.dp))
        }
        if (campus.hodDean.isNotEmpty()) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .sharedCard(Route.People.sharedKey)
                    .clickable { nav.go(Route.People) }
                    .padding(bottom = 14.dp)
            ) {
                Text(
                    text = "HOD and dean: " + campus.hodDean.joinToString(", ") { titleCase(it.name.removePrefix("Dr. ")) },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
    if (snapshot.faculty.isEmpty() && courses.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        Text("Designations, cabins and emails arrive with the next sync.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---- grades ------------------------------------------------------------------------

@Composable
private fun MaterialGradesSegment(snapshot: Snapshot) {
    val g = snapshot.grades
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "CGPA",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(4.dp))
                g?.creditsEarned?.let {
                    Text(
                        text = "${fmt(it)} credits earned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
            Text(
                text = g?.cgpa?.let { "%.2f".format(Locale.ENGLISH, floor(it * 100) / 100) } ?: "–",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
    val nav = LocalNav.current
    Spacer(Modifier.height(12.dp))
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ElevatedCard(
            modifier = Modifier.weight(1f).fillMaxHeight().sharedCard(Route.SemesterGrades.sharedKey),
            onClick = { nav.go(Route.SemesterGrades) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Semester GPA",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Each term",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ElevatedCard(
            modifier = Modifier.weight(1f).fillMaxHeight().sharedCard(Route.Curriculum.sharedKey),
            onClick = { nav.go(Route.Curriculum) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Curriculum",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Syllabus PDFs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    if (g?.curriculum?.isNotEmpty() == true) {
        SectionTitle("Credits")
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                g.curriculum.forEachIndexed { i, d ->
                    if (i > 0) Spacer(Modifier.height(12.dp))
                    Row {
                        Text(d.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1)
                        Text("${fmt(d.earned)} / ${fmt(d.required)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    val progress = if (d.required > 0) (d.earned / d.required).toFloat().coerceIn(0f, 1f) else 1f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                }
            }
        }
    }

    val byGrade = g?.entries.orEmpty().filter { it.grade != null && it.credits > 0 }.groupBy { it.grade!!.uppercase() }
    if (byGrade.isNotEmpty()) {
        SectionTitle("Credits by grade")
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                val most = byGrade.values.maxOf { l -> l.sumOf { it.credits } }
                (GradePoints.map { it.first } + byGrade.keys.filter { k -> GradePoints.none { it.first == k } }).forEach { grade ->
                    val credits = byGrade[grade]?.sumOf { it.credits } ?: return@forEach
                    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(grade, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(32.dp))
                        Box(Modifier.weight(1f)) {
                            LinearProgressIndicator(
                                progress = { (credits / most).toFloat() },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            )
                        }
                        Text(fmt(credits), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 10.dp).width(36.dp))
                    }
                }
            }
        }
    }

    MaterialPlannerCard(snapshot)

    val graded = g?.entries.orEmpty()
    if (graded.isEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text(
            if (g == null) "Grades arrive with the next sync." else "No grades yet. They appear after your first semester's results.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    graded.groupBy { it.examMonth }.entries.reversed().forEach { (month, list) ->
        SectionTitle(month.ifBlank { "Courses" })
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                list.forEachIndexed { i, e ->
                    if (i > 0) Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(e.courseTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
                            Text("${e.courseCode} · ${fmt(e.credits)} credits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(10.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (e.grade != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = e.grade ?: "–",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (e.grade != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialPlannerCard(snapshot: Snapshot) {
    val courses = snapshot.timetable.filter { it.credits > 0 }.distinctBy { it.courseCode + "|" + it.courseType }
    if (courses.isEmpty()) return
    val picks = remember(courses.size) { mutableStateListOf(*Array(courses.size) { 1 }) }
    val out = remember(courses.size) { mutableStateListOf(*Array(courses.size) { false }) }
    val counted = courses.indices.filter { !out[it] }
    val planned = counted.map { i -> courses[i].credits to GradePoints[picks[i]].second }
    val projected = projectCgpa(snapshot.grades, planned)
    val needed = neededAverage(snapshot.grades, counted.sumOf { courses[it].credits })

    SectionTitle("Plan this semester")
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "CGPA IF THESE HOLD",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when {
                            needed == null -> ""
                            needed <= 0 -> "You're above 9 whatever happens."
                            needed > 10 -> "9 is out of reach this semester."
                            else -> "Needs an average of ${"%.2f".format(Locale.ENGLISH, needed)} for 9."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                    )
                    val left = courses.size - counted.size
                    if (left > 0) Text("$left course${if (left == 1) "" else "s"} left out", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Text(
                    text = projected?.let { "%.2f".format(Locale.ENGLISH, floor(it * 100) / 100) } ?: "–",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f))
            courses.forEachIndexed { i, c ->
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).alpha(if (out[i]) 0.5f else 1f)) {
                        Text(
                            text = c.courseName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 2
                        )
                        Text(
                            text = "${c.courseCode} · ${if (isLabType(c.courseType)) "Lab" else "Theory"} · ${fmt(c.credits)} credit${if (c.credits == 1.0) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = !out[i],
                        onCheckedChange = { out[i] = !it }
                    )
                }
                if (out[i]) Text("Left out of the estimate until you count it again.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f), modifier = Modifier.padding(top = 2.dp))
                else ChoiceRow(GradePoints.map { it.first }, picks[i]) { picks[i] = it }
            }
        }
    }
}

private fun fmt(v: Double): String =
    if (v == floor(v)) v.toLong().toString() else "%.2f".format(Locale.ENGLISH, v).trimEnd('0').trimEnd('.')

@Composable
private fun MaterialPersonCard(p: Person, index: Int = 0) {
    val name = p.fields.firstOrNull { it.first == "Name" }?.second ?: p.role
    val title = p.fields.firstOrNull { it.first == "Designation" }?.second
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
