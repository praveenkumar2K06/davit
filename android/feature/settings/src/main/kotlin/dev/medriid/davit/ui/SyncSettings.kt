package dev.medriid.davit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.design.ChoiceRow
import dev.medriid.davit.design.InkButton
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.sync.BackgroundSync

@Composable
internal fun SyncSettings() {
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()
    val context = LocalContext.current
    val snapshot = state.snapshot
    SettingsCard("Sync") {
        InkButton(state.busy ?: "Sync now", repo::sync, enabled = state.busy == null, fillWidth = true, modifier = Modifier.fillMaxWidth().padding(bottom = 1.dp))
        if (state.timings.isNotEmpty()) {
            var open by rememberSaveable { mutableStateOf(false) }
            Spacer(Modifier.height(6.dp))
            SettingRow("Last sync", "${state.timings.sumOf { it.second } / 100 / 10.0} s over ${state.timings.size} stages", if (open) "Hide" else "Stages") { open = !open }
            AnimatedVisibility(open) {
                Column {
                    state.timings.forEach { (stage, ms) ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 1.dp)) {
                            InkText(stage, style = RisoType.small, modifier = Modifier.weight(1f))
                            InkText("$ms ms", style = RisoType.small.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
            }
        }
        ToggleRow(
            "Sync on app open",
            "Automatically refresh data when opening the app if older than 15 minutes.",
            state.syncOnOpen,
        ) { repo.setSyncOnOpen(!state.syncOnOpen) }
        ToggleRow(
            "Sync in the background",
            "At :15 and :45 while the app is closed, never between 1 and 6 am." + (state.lastBackground?.let { " Last: $it." } ?: ""),
            state.background,
        ) { repo.setBackground(!state.background) }
        if (state.background && !BackgroundSync.unrestricted(context)) {
            ToggleRow(
                "Let daVIT run in the background",
                "Your phone pauses apps when the screen is off, which stops background syncs and reminders. Tap to allow.",
                false,
            ) { BackgroundSync.askUnrestricted(context) }
        }
    }

    val semesters = snapshot?.semesters.orEmpty()
    val groups = snapshot?.feeds?.classGroups.orEmpty()
    if (semesters.size > 1 || groups.isNotEmpty()) {
        SettingsCard("Semester") {
            if (semesters.size > 1) {
                semesters.take(4).forEach { sem ->
                    val chosen = sem.id == snapshot?.semester?.id
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(enabled = !chosen && state.busy == null, role = Role.RadioButton, selected = chosen) { repo.chooseSemester(sem.id) }
                            .semantics { selected = chosen }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(chosen, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        InkText(sem.name)
                    }
                }
            }
            if (groups.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Hairline()
                val manual = repo.manualCalendarGroup()
                val cal = snapshot?.calendar
                Spacer(Modifier.height(8.dp))
                InkText("Calendar class group", style = RisoType.body.copy(fontWeight = FontWeight.SemiBold))
                InkText(
                    when {
                        manual != null -> "You chose ${groups.firstOrNull { it.id == manual }?.let { "${it.id} (${it.name})" } ?: manual}."
                        cal?.group?.isNotBlank() == true -> "Detected from your attendance: ${cal.group}."
                        else -> "Not detected yet; using General."
                    } + " Freshers' Saturdays and some exam days differ by group.",
                    style = RisoType.small,
                )
                Spacer(Modifier.height(8.dp))
                ChoiceRow(listOf("Auto") + groups.map { it.id }, if (manual == null) 0 else groups.indexOfFirst { it.id == manual } + 1) {
                    repo.setCalendarGroup(if (it == 0) null else groups[it - 1].id)
                }
            }
        }
    }
}
