package dev.medriid.davit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.data.Reminder
import dev.medriid.davit.data.Snapshot
import dev.medriid.davit.data.atRisk
import dev.medriid.davit.data.clock
import dev.medriid.davit.data.hint
import dev.medriid.davit.data.percent
import dev.medriid.davit.data.planFor
import dev.medriid.davit.data.remainingFor
import dev.medriid.davit.data.showOnMap
import dev.medriid.davit.data.vtopDate
import dev.medriid.davit.data.weekOf
import dev.medriid.davit.design.ChoiceRow
import dev.medriid.davit.design.DatePickerModal
import dev.medriid.davit.design.HalftoneBar
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.design.material.MaterialButton
import dev.medriid.davit.design.material.MaterialField
import dev.medriid.davit.ui.map.MapChip
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private fun greeting(hour: Int) = when (hour) {
    in 5..11 -> "Good morning ☀️"
    in 12..16 -> "Good afternoon 🌤️"
    else -> "Good evening 🌙"
}

private fun dayWord(days: Long) = when (days) {
    0L -> "today"
    1L -> "tomorrow"
    else -> "in $days days"
}

private val dueFormat = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH)
private val ReminderPresets = listOf("Mess change", "Go to Allmart", "Offline quiz", "Project review", "Report submission")

/**
 * Material 3 Expressive Home Screen Layout:
 * Clean surface containers, tonal card hierarchies, dynamic wallpapers & colors,
 * greeting header with student avatar, hero attendance banner, "Now & Next" timeline,
 * 2x2 expressive metrics dashboard, personal reminders, and categorized M3 Hub navigation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MaterialHome(
    snapshot: Snapshot,
    reminders: List<Reminder>,
    onOpen: (Route) -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenWeek: () -> Unit,
    onOpenAcademics: () -> Unit,
    onAddReminder: (String, LocalDate) -> Unit,
    onRemoveReminder: (Long) -> Unit,
) {
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()
    val nav = LocalNav.current
    val now = remember { LocalDateTime.now() }
    val today = now.toLocalDate()

    val plan = remember(snapshot, state.followCalendar) { planFor(weekOf(snapshot), now, snapshot.calendar, state.followCalendar) }
    val att = snapshot.attendance
    val riskyCount = att.count { it.atRisk }

    val dueAssignments = remember(snapshot, today) {
        snapshot.assignments.flatMap { c -> c.items.map { c to it } }
            .filter { (_, a) -> a.updated == null }
            .mapNotNull { (c, a) ->
                vtopDate(a.due)?.takeIf { !it.isBefore(today) && ChronoUnit.DAYS.between(today, it) <= 7 }?.let { Triple(c, a, it) }
            }
            .sortedBy { it.third }
    }

    val nextExam = remember(snapshot, today) {
        snapshot.exams.mapNotNull { e -> vtopDate(e.date)?.takeIf { !it.isBefore(today) }?.let { e to it } }
            .minByOrNull { it.second }
    }

    var addingReminder by rememberSaveable { mutableStateOf(false) }

    TabColumn {
        // -------------------------------------------------------------
        // 1. MERGED TOP APPBAR & MATERIAL 3 HERO BANNER
        // (Unified single container with display card customization support)
        // -------------------------------------------------------------
        MaterialBanner(
            state = state,
            mascot = Mascot.parse(state.mascot),
            photo = repo.bannerPhoto,
            onSync = repo::sync,
            onProfile = { onOpen(Route.Settings) },
            onMore = { onOpen(Route.Campus) },
            onOpenAttendance = onOpenAttendance,
        )

        Spacer(Modifier.height(14.dp))

        // -------------------------------------------------------------
        // 3. "NOW & NEXT" CLASS SCHEDULE TIMELINE CARD
        // -------------------------------------------------------------
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isNow = plan.now != null
                    Surface(
                        shape = CircleShape,
                        color = if (isNow) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (isNow) "🟢 ON NOW" else when (plan.nextInDays) {
                                0 -> "🗓️ NEXT UP"
                                1 -> "🌤️ TOMORROW"
                                else -> "📅 IN ${plan.nextInDays} DAYS"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isNow) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                        text = "Full Week →",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable { onOpenWeek() }
                    )
                }

                Spacer(Modifier.height(12.dp))

                val slot = plan.now ?: plan.next
                if (slot != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = clock(slot.meeting.start) + " – " + clock(slot.meeting.end),
                                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = slot.course.courseName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                        if (slot.course.venue.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            MapChip(slot.course.venue) { nav.showOnMap(repo, slot.course.venue) }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    slot.attendance?.let { a ->
                        HalftoneBar(
                            if (a.total == 0) 1f else a.attended.toFloat() / a.total,
                            threshold = 0.75f,
                            atRisk = a.atRisk,
                            height = 8.dp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${a.percent}% · ${a.hint(snapshot.attendance.let { snapshot.remainingFor(a, now) })}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "No classes scheduled for today 🎉",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Enjoy your free time or catch up on study goals.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // -------------------------------------------------------------
        // 4. EXPRESSIVE DASHBOARD METRIC TILES GRID (2x2)
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Due Assignments Tile
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        dueAssignments.firstOrNull()?.let { (c, a, _) ->
                            nav.go(Route.Assignment(c.classId, a.code.ifEmpty { "#0" }))
                        } ?: onOpenAcademics()
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = "DUE THIS WEEK",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = dueAssignments.size.toString(),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dueAssignments.firstOrNull()?.let { (_, a, d) -> "${a.title} · ${dayWord(ChronoUnit.DAYS.between(today, d))}" } ?: "Nothing due",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 2
                    )
                }
            }

            // Exams Countdown Tile
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpen(Route.ExamBoard) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = nextExam?.first?.type?.uppercase() ?: "EXAMS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(8.dp))
                    if (nextExam != null) {
                        val days = ChronoUnit.DAYS.between(today, nextExam.second).toInt()
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = days.toString(),
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "days",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${nextExam.first.courseName} · ${nextExam.second.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 2
                        )
                    } else {
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "None scheduled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // At Risk Courses Tile
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenAttendance() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (riskyCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = "AT RISK COURSES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (riskyCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = riskyCount.toString(),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (riskyCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (riskyCount > 0) "Needs immediate attendance attention" else "All courses safely above 75%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (riskyCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 2
                    )
                }
            }

            // Plan a Leave Tile
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpen(Route.LeavePlanner) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = "LEAVE PLANNER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Plan 🏖️",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Calculate attendance impact per day away",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // -------------------------------------------------------------
        // 5. PERSONAL REMINDERS & TASK COMPOSER
        // -------------------------------------------------------------
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Personal Reminders",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { addingReminder = !addingReminder }
                    ) {
                        Text(
                            text = if (addingReminder) "Cancel" else "+ Add",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                if (reminders.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        reminders.forEach { r ->
                            val days = r.date.toEpochDay() - today.toEpochDay()
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = r.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${r.date.format(dueFormat)} · " + when {
                                                days < 0 -> "Overdue"
                                                days == 0L -> "Today"
                                                days == 1L -> "Tomorrow"
                                                else -> "in $days days"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { onRemoveReminder(r.id) }
                                    ) {
                                        Text(
                                            text = "Done ✓",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = addingReminder,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(Modifier.padding(top = 14.dp)) {
                        AddReminderWidget(onSave = { title, date ->
                            onAddReminder(title, date)
                            addingReminder = false
                        })
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // -------------------------------------------------------------
        // 6. CATEGORIZED EXPRESSIVE MATERIAL 3 HUB NAVIGATION
        // -------------------------------------------------------------
        Text(
            text = "Quick Access Hub",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(8.dp))

        MaterialHubGrid(onOpen = onOpen, onOpenWeek = onOpenWeek)

        Spacer(Modifier.height(16.dp))
    }
}

/** Form composer widget for creating new reminders in Material 3 style. */
@Composable
private fun AddReminderWidget(onSave: (String, LocalDate) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        ChoiceRow(ReminderPresets, ReminderPresets.indexOf(title)) { title = ReminderPresets[it] }
        Spacer(Modifier.height(10.dp))
        MaterialField(
            value = title,
            onValueChange = { title = it.take(80) },
            label = "Reminder Title",
            inks = LocalInks.current,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = date.format(dueFormat),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            MaterialButton(
                text = "Change Date",
                onClick = { showDatePicker = true },
                quiet = true
            )
        }
        Spacer(Modifier.height(12.dp))
        MaterialButton(
            text = "Save Reminder",
            onClick = { onSave(title, date) },
            enabled = title.isNotBlank(),
            fillWidth = true
        )
    }

    if (showDatePicker) {
        DatePickerModal(
            initialDate = date,
            onDateSelected = { pickedDate ->
                date = pickedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

private class MaterialHubItem(
    val label: String,
    val icon: RisoIcon,
    val description: String,
    val onClick: () -> Unit
)

/** Categorized Material 3 Navigation Hub with expressive tonal surface tiles. */
@Composable
private fun MaterialHubGrid(onOpen: (Route) -> Unit, onOpenWeek: () -> Unit) {
    val repo = LocalRepo.current
    val nav = LocalNav.current

    fun academics(section: Int): () -> Unit = {
        repo.academicsFocus.value = section
        nav.tab(2)
    }

    val categories = listOf(
        "Study & Academics" to listOf(
            MaterialHubItem("Marks & CAT", RisoIcon.Marks, "Internal scores & weightage", academics(0)),
            MaterialHubItem("Assignments", RisoIcon.Assignments, "DA & uploads tracker", academics(1)),
            MaterialHubItem("Exam Board", RisoIcon.Exams, "CAT & FAT schedules", { onOpen(Route.ExamBoard) }),
            MaterialHubItem("Grades & CGPA", RisoIcon.Grades, "GPA calculation", academics(3)),
            MaterialHubItem("Materials", RisoIcon.Materials, "Course PDFs & slides", academics(4)),
            MaterialHubItem("Faculty Reviews", RisoIcon.Faculty, "Teacher feedback", academics(5))
        ),
        "Schedule & Planning" to listOf(
            MaterialHubItem("Week Timetable", RisoIcon.Week, "Class schedule view", onOpenWeek),
            MaterialHubItem("Curriculum", RisoIcon.Book, "Course credits & slots", { onOpen(Route.Curriculum) }),
            MaterialHubItem("FFCS Planner", RisoIcon.Today, "Slot timetable maker", { onOpen(Route.Planner) }),
            MaterialHubItem("FFCS Result", RisoIcon.Next, "Allotted timetable", { onOpen(Route.FfcsResult) }),
            MaterialHubItem("Free with Friends", RisoIcon.People, "Compare free slots", { onOpen(Route.Friends) })
        ),
        "Campus & Services" to listOf(
            MaterialHubItem("Campus Map", RisoIcon.Map, "Buildings & food spots", { nav.tab(4) }),
            MaterialHubItem("Hostel & Gate", RisoIcon.Gate, "Night slip & leave", { onOpen(Route.Hostel) }),
            MaterialHubItem("Mess Menu", RisoIcon.Receipt, "Breakfast, lunch, dinner", { onOpen(Route.MessMenu) }),
            MaterialHubItem("Spot-light", RisoIcon.Next, "Student projects & news", { onOpen(Route.Spotlight) }),
            MaterialHubItem("Events & Clubs", RisoIcon.Next, "Upcoming campus events", { onOpen(Route.Events) })
        ),
        "VTOP Records & Requests" to listOf(
            MaterialHubItem("Leave Planner", RisoIcon.Next, "Attendance safety impact", { onOpen(Route.LeavePlanner) }),
            MaterialHubItem("Course Feedback", RisoIcon.Next, "Submit VTOP feedback", { onOpen(Route.CourseFeedback) }),
            MaterialHubItem("Paper Seeing", RisoIcon.Next, "Review graded papers", { onOpen(Route.PaperSeeing) }),
            MaterialHubItem("Corrections", RisoIcon.Next, "Attendance corrections", { onOpen(Route.Corrections) }),
            MaterialHubItem("Downloads", RisoIcon.Next, "Saved offline files", { onOpen(Route.Downloads) })
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        categories.forEach { (categoryTitle, items) ->
            Column {
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { item ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { item.onClick() },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                IconGlyph(item.icon, MaterialTheme.colorScheme.onSecondaryContainer, Modifier.size(18.dp))
                                            }
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = item.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
