@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.medriid.davit.ui.material3

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.design.material.MaterialButton
import dev.medriid.davit.sync.BackgroundSync

@Composable
fun MaterialSyncSettings() {
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()
    val context = LocalContext.current
    val snapshot = state.snapshot

    MaterialSettingsSectionHeader("Sync", icon = RisoIcon.Today)
    MaterialButton(
        text = state.busy ?: "Sync now",
        onClick = repo::sync,
        enabled = state.busy == null,
        fillWidth = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    )

    var timingsOpen by rememberSaveable { mutableStateOf(false) }
    val hasTimings = state.timings.isNotEmpty()
    val needsUnrestricted = state.background && !BackgroundSync.unrestricted(context)

    var count = 2
    if (hasTimings) count++
    if (needsUnrestricted) count++

    var index = 0
    if (hasTimings) {
        val currIdx = index++
        MaterialSegmentedActionRow(
            title = "Last sync",
            body = "${state.timings.sumOf { it.second } / 100 / 10.0} s over ${state.timings.size} stages",
            action = if (timingsOpen) "Hide" else "Stages",
            index = currIdx,
            count = count,
            onClick = { timingsOpen = !timingsOpen },
        )
        AnimatedVisibility(timingsOpen) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        "Stage breakdown",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    state.timings.forEachIndexed { i, (stage, ms) ->
                        if (i > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ) {
                                Text(
                                    "$ms ms",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    MaterialSegmentedToggleRow(
        title = "Sync on app open",
        body = "Automatically refresh data when opening the app if older than 15 minutes.",
        checked = state.syncOnOpen,
        index = index++,
        count = count,
        onCheckedChange = { repo.setSyncOnOpen(it) },
    )

    MaterialSegmentedToggleRow(
        title = "Sync in the background",
        body = "At :15 and :45 while the app is closed, never between 1 and 6 am." + (state.lastBackground?.let { " Last: $it." } ?: ""),
        checked = state.background,
        index = index++,
        count = count,
        onCheckedChange = { repo.setBackground(it) },
    )

    if (needsUnrestricted) {
        MaterialSegmentedToggleRow(
            title = "Let daVIT run in the background",
            body = "Your phone pauses apps when the screen is off, which stops background syncs and reminders. Tap to allow.",
            checked = false,
            index = index,
            count = count,
            onCheckedChange = { BackgroundSync.askUnrestricted(context) },
        )
    }

    val semesters = snapshot?.semesters.orEmpty()
    val groups = snapshot?.feeds?.classGroups.orEmpty()

    if (semesters.size > 1) {
        MaterialSettingsSectionHeader("Semester")
        val activeSemesters = semesters.take(4)
        activeSemesters.forEachIndexed { i, sem ->
            val chosen = sem.id == snapshot?.semester?.id
            MaterialSegmentedRow(
                title = sem.name,
                index = i,
                count = activeSemesters.size,
                leadingContent = {
                    RadioButton(
                        selected = chosen,
                        onClick = null,
                    )
                },
                onClick = { if (!chosen && state.busy == null) repo.chooseSemester(sem.id) },
            )
        }
    }

    if (groups.isNotEmpty()) {
        MaterialSettingsSectionHeader("Calendar class group")
        val manual = repo.manualCalendarGroup()
        val cal = snapshot?.calendar

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    when {
                        manual != null -> "You chose ${groups.firstOrNull { it.id == manual }?.let { "${it.id} (${it.name})" } ?: manual}."
                        cal?.group?.isNotBlank() == true -> "Detected from your attendance: ${cal.group}."
                        else -> "Not detected yet; using General."
                    } + " Freshers' Saturdays and some exam days differ by group.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                val options = listOf("Auto") + groups.map { it.id }
                val selectedIdx = if (manual == null) 0 else groups.indexOfFirst { it.id == manual } + 1
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    options.forEachIndexed { i, opt ->
                        FilterChip(
                            selected = i == selectedIdx,
                            onClick = { repo.setCalendarGroup(if (i == 0) null else groups[i - 1].id) },
                            label = { Text(opt) },
                        )
                    }
                }
            }
        }
    }
}
