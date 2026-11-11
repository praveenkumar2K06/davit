@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.medriid.davit.ui.material3

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.Alert
import dev.medriid.davit.data.AlertChannel
import dev.medriid.davit.data.AlertPrefs
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.data.courseKey
import dev.medriid.davit.data.percent
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.sync.Notifier
import dev.medriid.davit.ui.LocalNav
import dev.medriid.davit.ui.Route
import dev.medriid.davit.ui.RouteScreen
import dev.medriid.davit.ui.alertSummary

@Composable
fun MaterialNotificationsScreen(route: Route) {
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()
    val nav = LocalNav.current
    val context = LocalContext.current
    val p = state.alertPrefs
    fun set(next: AlertPrefs) = repo.setAlertPrefs(next)
    val blocked = state.alerts && !Notifier.permitted(context)
    val openAndroid = {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    RouteScreen(route, "Notifications", if (state.alerts) alertSummary(p) else "Off") {
        MaterialSegmentedToggleRow(
            title = "Notifications",
            body = if (blocked) "Blocked in Android settings. Tap to open them." else "Checked after each background sync; reminders at the minute you set.",
            checked = state.alerts,
            index = 0,
            count = 1,
            onCheckedChange = { if (blocked) openAndroid() else repo.setAlerts(!state.alerts) },
        )
        if (!state.alerts) return@RouteScreen

        val exempt = state.snapshot?.attendanceExempt == true
        MaterialSettingsSectionHeader("Attendance", icon = RisoIcon.Bell)
        if (exempt) {
            Text(
                "Your CGPA is 9 or more, so attendance doesn't hold you back. These stay off until it does.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }

        MaterialSegmentedToggleRow(
            title = "Attendance warnings",
            body = "When a course slips, once per change.",
            checked = p.attendance,
            index = 0,
            count = 1,
            onCheckedChange = { set(p.copy(attendance = it)) },
        )

        if (p.attendance) {
            Spacer(Modifier.height(8.dp))
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Warn me below",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        "75% is VTOP's line; a higher one warns while there's still room.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AlertPrefs.WarnLines.forEach { line ->
                            FilterChip(
                                selected = p.warnBelow == line,
                                onClick = { set(p.copy(warnBelow = line)) },
                                label = { Text("$line%") },
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        "When skips run low",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        "Skips are classes you can miss in a row and stay at 75%.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    val skipLabels = listOf("Never", "None left", "1 left", "2 left", "3 left")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AlertPrefs.SkipOptions.forEachIndexed { i, opt ->
                            FilterChip(
                                selected = p.skipsLeft == opt,
                                onClick = { set(p.copy(skipsLeft = opt)) },
                                label = { Text(skipLabels[i]) },
                            )
                        }
                    }
                }
            }
        }

        val rows = state.snapshot?.attendance.orEmpty().sortedBy { it.percent }
        if (p.attendance && rows.isNotEmpty()) {
            MaterialSettingsSectionHeader("Muted Courses")
            rows.forEachIndexed { idx, r ->
                val key = courseKey(r.courseCode, r.classType)
                val on = key !in p.muted
                MaterialSegmentedToggleRow(
                    title = r.courseName,
                    body = "${r.classType} · ${r.percent}% · ${r.attended}/${r.total}",
                    checked = on,
                    index = idx,
                    count = rows.size,
                    onCheckedChange = {
                        set(p.copy(muted = if (on) p.muted + key else p.muted - key))
                    },
                )
            }
        }

        MaterialSettingsSectionHeader("Before class")
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Reminder timing",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    "A reminder before class, with the room and what missing it would cost.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AlertPrefs.BeforeClassOptions.forEach { opt ->
                        FilterChip(
                            selected = p.beforeClass == opt,
                            onClick = { set(p.copy(beforeClass = opt)) },
                            label = { Text(if (opt == 0) "Off" else "$opt min") },
                        )
                    }
                }

                if (p.beforeClass > 0) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "For",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = p.beforeClassNearLine,
                            onClick = { set(p.copy(beforeClassNearLine = true)) },
                            label = { Text("Courses near the line") },
                        )
                        FilterChip(
                            selected = !p.beforeClassNearLine,
                            onClick = { set(p.copy(beforeClassNearLine = false)) },
                            label = { Text("Every class") },
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (p.beforeClassNearLine) "Near the line: below ${p.warnBelow}%, or two skips or fewer from 75%."
                        else "Every class, with its room.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        MaterialSettingsSectionHeader("Meals")
        MaterialSegmentedActionRow(
            title = "Meal reminders",
            body = if (p.meals.isEmpty()) "Off. Set them from the bell on the mess menu." else "${p.meals.size} of 4 meals, set from the bell on the mess menu.",
            action = "Open",
            index = 0,
            count = 1,
            onClick = { nav.go(Route.MessMenu) },
        )

        MaterialSettingsSectionHeader("Additional alerts")
        val alsoItems = listOf(
            Triple("VTOP remarks", "A course marked Debarred or Revoked, or a change in which exams it covers.", p.remarks) to { set(p.copy(remarks = !p.remarks)) },
            Triple("New marks", "A quiz, CAT or assignment score as it appears.", p.marks) to { set(p.copy(marks = !p.marks)) },
            Triple("Deadlines", "Assignments due today or tomorrow, and your own reminders.", p.deadlines) to { set(p.copy(deadlines = !p.deadlines)) },
            Triple("Exams", "The day before, with the time, venue and seat.", p.exams) to { set(p.copy(exams = !p.exams)) },
            Triple("Registration", "A new registration window, and 30 minutes before it opens.", p.notices) to { set(p.copy(notices = !p.notices)) },
        )
        alsoItems.forEachIndexed { i, (info, action) ->
            val (title, body, checked) = info
            MaterialSegmentedToggleRow(
                title = title,
                body = body,
                checked = checked,
                index = i,
                count = alsoItems.size,
                onCheckedChange = { action() },
            )
        }

        MaterialSettingsSectionHeader("Check & System Settings")
        var sent by remember { mutableStateOf(false) }
        MaterialSegmentedActionRow(
            title = "Send a test",
            body = if (sent) "Sent. If nothing showed up, check Android's settings below." else "Posts one notification now, so you see how they look.",
            action = if (sent) "Sent" else "Send",
            index = 0,
            count = 2,
            onClick = {
                Notifier.post(
                    context,
                    listOf(
                        Alert(
                            AlertChannel.Classes, "test:${System.currentTimeMillis()}",
                            "daVIT notifications work", "This is how a reminder looks.",
                        ),
                    ),
                )
                sent = true
            },
        )
        MaterialSegmentedActionRow(
            title = "Sounds and channels",
            body = "Android's own settings for each kind of notification.",
            action = "Open",
            index = 1,
            count = 2,
            onClick = openAndroid,
        )
    }
}
