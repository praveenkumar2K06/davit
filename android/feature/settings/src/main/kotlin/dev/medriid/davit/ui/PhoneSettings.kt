package dev.medriid.davit.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
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
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate

import dev.medriid.davit.ui.material3.MaterialPhoneSettings

internal fun canLockApp(activity: ComponentActivity): Boolean {
    val bm = BiometricManager.from(activity)
    return bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
}

@Composable
internal fun PhoneSettings() {
    if (LocalInks.current.material) {
        MaterialPhoneSettings()
        return
    }
    val repo = LocalRepo.current
    val nav = LocalNav.current
    val state by repo.state.collectAsState()
    val context = LocalContext.current
    SettingsCard("Security") {
        val activity = context as? ComponentActivity
        if (activity != null && canLockApp(activity)) {
            ToggleRow(
                "App lock",
                "Fingerprint, face or screen lock to open daVIT after a minute away. The widget and notifications still show.",
                state.appLock,
            ) { repo.setAppLock(!state.appLock) }
        } else {
            InkText("This phone has no screen lock set, so daVIT can't ask for one.", style = RisoType.small)
        }
    }
    val widgets = AppWidgetManager.getInstance(context)
    if (widgets.isRequestPinAppWidgetSupported) SettingsCard("Widgets") {
        SettingRow("Home screen widget", "Your class now or next, its attendance, and what's due.", "Add") {
            widgets.requestPinAppWidget(ComponentName(context.packageName, "dev.medriid.davit.widget.DavitWidgetReceiver"), null, null)
        }
        SettingRow("Attendance widget", "Courses near or under the line, worst first, with what it takes.", "Add") {
            widgets.requestPinAppWidget(ComponentName(context.packageName, "dev.medriid.davit.widget.AttendanceWidgetReceiver"), null, null)
        }
    }
    SettingsCard("Calendar") {
        ClassesInCalendar(state)
    }
    SettingsCard("Stored here") {
        val saved by repo.files.saved.collectAsState()
        SettingRow(
            "Downloads",
            if (saved.isEmpty()) "Files you open from VTOP stay here for offline." else "${saved.size} file${if (saved.size == 1) "" else "s"} on this phone.",
            "Manage",
        ) { nav.go(Route.Downloads) }
        SettingRow("Campus map", "Jeman's map of the campus, kept on this phone and refreshed weekly.", "Refresh") { repo.maps.refresh() }
    }
}

/**
 * Your classes in the phone's calendar app (asks for the calendar permission when you
 * turn it on), kept up to date after each sync; or the semester as a file for other apps.
 */
@Composable
internal fun ClassesInCalendar(state: UiState) {
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
    when {
        snapshot?.calendar == null -> SettingRow("Classes in your calendar", "Needs the academic calendar, which comes with the next sync.", "Later") {}
        on -> SettingRow("Classes in your calendar", note ?: "On: \"daVIT classes\", updated after each sync. Holidays and swapped days included.", "Remove") {
            scope.launch {
                PhoneCalendar.turnOff(context)
                on = false
                note = "Removed from your calendar."
            }
        }
        else -> SettingRow("Classes in your calendar", note ?: "Every class until the semester ends, holidays and swapped days included, in your phone's calendar app.", "Add") {
            if (PhoneCalendar.permitted(context)) add()
            else ask.launch(arrayOf(android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR))
        }
    }
    if (snapshot?.calendar != null) {
        SettingRow("Classes as a file", "An .ics file of the semester, for calendar apps that import one.", "Share") {
            scope.launch {
                val file = withContext(Dispatchers.IO) {
                    File(context.cacheDir, "exports").apply { mkdirs() }.resolve("daVIT classes.ics").apply {
                        writeText(icsOf(snapshot.classEvents(LocalDate.now()), Instant.now()))
                    }
                }
                repo.files.share(file)
            }
        }
    }
}
