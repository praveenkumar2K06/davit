package dev.medriid.davit.ui.material3

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.data.PhoneCalendar
import dev.medriid.davit.data.UiState
import dev.medriid.davit.data.classEvents
import dev.medriid.davit.data.icsOf
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.ui.LocalNav
import dev.medriid.davit.ui.Route
import dev.medriid.davit.ui.canLockApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate

@Composable
fun MaterialPhoneSettings() {
    val repo = LocalRepo.current
    val nav = LocalNav.current
    val state by repo.state.collectAsState()
    val context = LocalContext.current

    MaterialSettingsSectionHeader("Security", icon = RisoIcon.Gate)
    val activity = context as? ComponentActivity
    if (activity != null && canLockApp(activity)) {
        MaterialSegmentedToggleRow(
            title = "App lock",
            body = "Fingerprint, face or screen lock to open daVIT after a minute away. The widget and notifications still show.",
            checked = state.appLock,
            index = 0,
            count = 1,
            onCheckedChange = { repo.setAppLock(it) },
        )
    } else {
        MaterialSegmentedRow(
            title = "App lock",
            body = "This phone has no screen lock set, so daVIT can't ask for one.",
            index = 0,
            count = 1,
        )
    }

    val widgets = remember(context) { AppWidgetManager.getInstance(context) }
    if (widgets.isRequestPinAppWidgetSupported) {
        MaterialSettingsSectionHeader("Widgets")
        MaterialSegmentedActionRow(
            title = "Home screen widget",
            body = "Your class now or next, its attendance, and what's due.",
            action = "Add",
            index = 0,
            count = 2,
            onClick = {
                widgets.requestPinAppWidget(ComponentName(context.packageName, "dev.medriid.davit.widget.DavitWidgetReceiver"), null, null)
            },
        )
        MaterialSegmentedActionRow(
            title = "Attendance widget",
            body = "Courses near or under the line, worst first, with what it takes.",
            action = "Add",
            index = 1,
            count = 2,
            onClick = {
                widgets.requestPinAppWidget(ComponentName(context.packageName, "dev.medriid.davit.widget.AttendanceWidgetReceiver"), null, null)
            },
        )
    }

    MaterialSettingsSectionHeader("Calendar")
    MaterialClassesInCalendar(state)

    MaterialSettingsSectionHeader("Stored here")
    val saved by repo.files.saved.collectAsState()
    MaterialSegmentedActionRow(
        title = "Downloads",
        body = if (saved.isEmpty()) "Files you open from VTOP stay here for offline." else "${saved.size} file${if (saved.size == 1) "" else "s"} on this phone.",
        action = "Manage",
        index = 0,
        count = 2,
        onClick = { nav.go(Route.Downloads) },
    )
    MaterialSegmentedActionRow(
        title = "Campus map",
        body = "Jeman's map of the campus, kept on this phone and refreshed weekly.",
        action = "Refresh",
        index = 1,
        count = 2,
        onClick = { repo.maps.refresh() },
    )
}

@Composable
private fun MaterialClassesInCalendar(state: UiState) {
    val repo = LocalRepo.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snapshot = state.snapshot
    var on by remember { mutableStateOf(PhoneCalendar.enabled(context)) }
    var note by remember { mutableStateOf<String?>(null) }
    val color = LocalInks.current.a.toArgb()
    fun add() = scope.launch {
        note = "Adding your classes…"
        note = runCatching { PhoneCalendar.turnOn(context, snapshot!!.classEvents(LocalDate.now()), color) }
            .fold({ n -> on = true; "$n classes added to \"daVIT classes\" in your calendar." }, { "Couldn't write to the phone's calendar." })
    }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.all { it }) add() else note = "daVIT needs the calendar permission for this."
    }
    val calCount = if (snapshot?.calendar != null) 2 else 1
    when {
        snapshot?.calendar == null -> {
            MaterialSegmentedActionRow(
                title = "Classes in your calendar",
                body = "Needs the academic calendar, which comes with the next sync.",
                action = "Later",
                index = 0,
                count = calCount,
                onClick = {},
            )
        }
        on -> {
            MaterialSegmentedActionRow(
                title = "Classes in your calendar",
                body = note ?: "On: \"daVIT classes\", updated after each sync. Holidays and swapped days included.",
                action = "Remove",
                index = 0,
                count = calCount,
                onClick = {
                    scope.launch {
                        PhoneCalendar.turnOff(context)
                        on = false
                        note = "Removed from your calendar."
                    }
                },
            )
        }
        else -> {
            MaterialSegmentedActionRow(
                title = "Classes in your calendar",
                body = note ?: "Every class until the semester ends, holidays and swapped days included, in your phone's calendar app.",
                action = "Add",
                index = 0,
                count = calCount,
                onClick = {
                    if (PhoneCalendar.permitted(context)) add()
                    else ask.launch(arrayOf(android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR))
                },
            )
        }
    }
    if (snapshot?.calendar != null) {
        MaterialSegmentedActionRow(
            title = "Classes as a file",
            body = "An .ics file of the semester, for calendar apps that import one.",
            action = "Share",
            index = 1,
            count = calCount,
            onClick = {
                scope.launch {
                    val file = withContext(Dispatchers.IO) {
                        File(context.cacheDir, "exports").apply { mkdirs() }.resolve("daVIT classes.ics").apply {
                            writeText(icsOf(snapshot.classEvents(LocalDate.now()), Instant.now()))
                        }
                    }
                    repo.files.share(file)
                }
            },
        )
    }
}
