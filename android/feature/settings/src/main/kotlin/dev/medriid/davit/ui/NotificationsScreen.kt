package dev.medriid.davit.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import dev.medriid.davit.design.ChoiceRow
import dev.medriid.davit.design.InkCard
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.sync.Notifier

import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.ui.material3.MaterialNotificationsScreen

/**
 * What to notify about: attendance lines, skips, muted courses, reminders before class,
 * and each other kind of alert on its own switch.
 */
@Composable
fun NotificationsScreen(route: Route) {
    if (LocalInks.current.material) {
        MaterialNotificationsScreen(route)
        return
    }
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()
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
        InkCard(Modifier.fillMaxWidth(), plateOffset = 4.dp) {
            ToggleRow(
                "Notifications",
                if (blocked) "Blocked in Android settings. Tap to open them." else "Checked after each background sync; reminders at the minute you set.",
                state.alerts,
            ) { if (blocked) openAndroid() else repo.setAlerts(!state.alerts) }
        }
        if (!state.alerts) return@RouteScreen

        val exempt = state.snapshot?.attendanceExempt == true
        SettingsCard("Attendance") {
            if (exempt) {
                InkText("Your CGPA is 9 or more, so attendance doesn't hold you back. These stay off until it does.", style = RisoType.small)
                Spacer(Modifier.height(8.dp))
            }
            ToggleRow("Attendance warnings", "When a course slips, once per change.", p.attendance) { set(p.copy(attendance = !p.attendance)) }
            if (p.attendance) {
                Spacer(Modifier.height(6.dp))
                InkText("Warn me below", style = RisoType.body.copy(fontWeight = FontWeight.SemiBold))
                InkText("75% is VTOP's line; a higher one warns while there's still room.", style = RisoType.small)
                Spacer(Modifier.height(8.dp))
                ChoiceRow(AlertPrefs.WarnLines.map { "$it%" }, AlertPrefs.WarnLines.indexOf(p.warnBelow).coerceAtLeast(0)) { set(p.copy(warnBelow = AlertPrefs.WarnLines[it])) }
                Spacer(Modifier.height(14.dp))
                InkText("When skips run low", style = RisoType.body.copy(fontWeight = FontWeight.SemiBold))
                InkText("Skips are classes you can miss in a row and stay at 75%.", style = RisoType.small)
                Spacer(Modifier.height(8.dp))
                ChoiceRow(listOf("Never", "None left", "1 left", "2 left", "3 left"), AlertPrefs.SkipOptions.indexOf(p.skipsLeft).coerceAtLeast(0)) {
                    set(p.copy(skipsLeft = AlertPrefs.SkipOptions[it]))
                }
            }
        }

        // Each course on its own switch, the closest to the line first.
        val rows = state.snapshot?.attendance.orEmpty().sortedBy { it.percent }
        if (p.attendance && rows.isNotEmpty()) {
            SettingsCard("Courses") {
                InkText("Mute a course you've made peace with; the rest still warn.", style = RisoType.small)
                Spacer(Modifier.height(4.dp))
                rows.forEach { r ->
                    val key = courseKey(r.courseCode, r.classType)
                    val on = key !in p.muted
                    ToggleRow(r.courseName, "${r.classType} · ${r.percent}% · ${r.attended}/${r.total}", on) {
                        set(p.copy(muted = if (on) p.muted + key else p.muted - key))
                    }
                }
            }
        }

        SettingsCard("Before class") {
            InkText("A reminder before class, with the room and what missing it would cost.", style = RisoType.small)
            Spacer(Modifier.height(8.dp))
            ChoiceRow(AlertPrefs.BeforeClassOptions.map { if (it == 0) "Off" else "$it min" }, AlertPrefs.BeforeClassOptions.indexOf(p.beforeClass).coerceAtLeast(0)) {
                set(p.copy(beforeClass = AlertPrefs.BeforeClassOptions[it]))
            }
            if (p.beforeClass > 0) {
                Spacer(Modifier.height(14.dp))
                InkText("For", style = RisoType.body.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(8.dp))
                ChoiceRow(listOf("Courses near the line", "Every class"), if (p.beforeClassNearLine) 0 else 1) { set(p.copy(beforeClassNearLine = it == 0)) }
                Spacer(Modifier.height(6.dp))
                InkText(
                    if (p.beforeClassNearLine) "Near the line: below ${p.warnBelow}%, or two skips or fewer from 75%."
                    else "Every class, with its room.",
                    style = RisoType.small,
                )
            }
        }

        SettingsCard("Meals") {
            val nav = LocalNav.current
            SettingRow("Meal reminders", if (p.meals.isEmpty()) "Off. Set them from the bell on the mess menu." else "${p.meals.size} of 4 meals, set from the bell on the mess menu.", "Open") { nav.go(Route.MessMenu) }
        }

        SettingsCard("Also tell me about") {
            ToggleRow("VTOP remarks", "A course marked Debarred or Revoked, or a change in which exams it covers.", p.remarks) { set(p.copy(remarks = !p.remarks)) }
            ToggleRow("New marks", "A quiz, CAT or assignment score as it appears.", p.marks) { set(p.copy(marks = !p.marks)) }
            ToggleRow("Deadlines", "Assignments due today or tomorrow, and your own reminders.", p.deadlines) { set(p.copy(deadlines = !p.deadlines)) }
            ToggleRow("Exams", "The day before, with the time, venue and seat.", p.exams) { set(p.copy(exams = !p.exams)) }
            ToggleRow("Registration", "A new registration window, and 30 minutes before it opens.", p.notices) { set(p.copy(notices = !p.notices)) }
        }

        SettingsCard("Check") {
            var sent by remember { mutableStateOf(false) }
            SettingRow("Send a test", if (sent) "Sent. If nothing showed up, check Android's settings below." else "Posts one notification now, so you see how they look.", "Send") {
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
            }
            SettingRow("Sounds and channels", "Android's own settings for each kind of notification.", "Open") { openAndroid() }
        }
    }
}

/** "Below 80% · 1 skip left · 10 min before class". */
internal fun alertSummary(p: AlertPrefs): String = listOfNotNull(
    if (!p.attendance) "No attendance warnings" else "Below ${p.warnBelow}%",
    when {
        !p.attendance || p.skipsLeft < 0 -> null
        p.skipsLeft == 0 -> "no skips left"
        else -> "${p.skipsLeft} skip${if (p.skipsLeft == 1) "" else "s"} left"
    },
    if (p.beforeClass > 0) "${p.beforeClass} min before class" else null,
).joinToString(" · ")
