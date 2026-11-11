package dev.medriid.davit.ui.material3

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.design.AppStyle
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.design.material.MaterialButton
import dev.medriid.davit.design.material.MaterialField
import dev.medriid.davit.sync.AutoUpdate
import dev.medriid.davit.sync.Notifier
import dev.medriid.davit.ui.LocalNav
import dev.medriid.davit.ui.Mascot
import dev.medriid.davit.ui.MaterialBanner
import dev.medriid.davit.ui.Route
import dev.medriid.davit.ui.RouteScreen
import dev.medriid.davit.ui.SettingsGroup
import dev.medriid.davit.ui.SettingsIndex
import dev.medriid.davit.ui.alertSummary
import dev.medriid.davit.ui.open
import dev.medriid.davit.ui.rememberUpdateAction

/**
 * Material 3 version of SettingsScreen: compact mascot banner, Material search field,
 * segmented list items for groups, Material buttons for updates and sign-out.
 */
@Composable
fun MaterialSettingsScreen(route: Route) {
    val repo = LocalRepo.current
    val nav = LocalNav.current
    val state by repo.state.collectAsState()
    val context = LocalContext.current
    val snapshot = state.snapshot
    val inks = LocalInks.current

    RouteScreen(route, "Settings") {
        MaterialBanner(
            state = state,
            mascot = Mascot.parse(state.mascot),
            photo = repo.bannerPhoto,
            onSync = repo::sync,
            compact = true,
        )

        Spacer(Modifier.height(14.dp))
        var query by remember { mutableStateOf("") }
        MaterialField(query, { query = it }, "Find a setting", modifier = Modifier.fillMaxWidth(), inks = inks)
        val q = query.trim().lowercase()
        if (q.isNotEmpty()) {
            val hits = SettingsIndex.filter { (name, words, _) -> (q in name.lowercase()) || (q in words) }
            Spacer(Modifier.height(10.dp))
            if (hits.isEmpty()) {
                Text(
                    "Nothing called that. Try another word.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            hits.forEachIndexed { idx, (name, _, group) ->
                SegmentedListItem(
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    shapes = ListItemDefaults.segmentedShapes(index = idx, count = hits.size),
                    leadingContent = { IconGlyph(group.icon, MaterialTheme.colorScheme.primary, Modifier.size(22.dp)) },
                    content = { Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)) },
                    supportingContent = { Text(group.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingContent = { IconGlyph(RisoIcon.Next, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), Modifier.size(16.dp)) },
                    onClick = { open(nav, group) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                )
            }
            return@RouteScreen
        }

        Spacer(Modifier.height(8.dp))
        val blocked = state.alerts && !Notifier.permitted(context)
        val saved by repo.files.saved.collectAsState()
        val currentStyleName = inks.style.title
        val isClassicStyle = inks.style == AppStyle.Classic
        SettingsGroup.entries.forEach { g ->
            val summary = when (g) {
                SettingsGroup.Look -> listOfNotNull(
                    currentStyleName,
                    listOf("Follows the phone", "Light", "Dark")[state.theme.coerceIn(0, 2)],
                    if (isClassicStyle) InkPairs.all[state.palette.base.coerceIn(0, InkPairs.all.lastIndex)].name.takeUnless { state.palette.custom } ?: "your own colours" else null,
                ).joinToString(" · ")
                SettingsGroup.Yours -> "Display card, mascot, course names" + if (state.aliases.isNotEmpty()) " · ${state.aliases.size} renamed" else ""
                SettingsGroup.Alerts -> when {
                    !state.alerts -> "Off"
                    blocked -> "Blocked in Android settings"
                    else -> alertSummary(state.alertPrefs)
                }
                SettingsGroup.Sync -> listOfNotNull(
                    if (!state.syncOnOpen) "Sync on open off" else null,
                    if (state.background) "Every 30 minutes" else "Background sync off",
                    snapshot?.semester?.name?.substringBefore(" - "),
                ).joinToString(" · ")
                SettingsGroup.Flags -> listOfNotNull(
                    if (state.followCalendar) "Follow calendar on" else "Follow calendar off",
                    if (state.calcBeforeExam) "Calculate before exam on" else null,
                ).joinToString(" · ")
                SettingsGroup.Phone -> listOfNotNull(
                    if (state.appLock) "App lock on" else null,
                    if (saved.isEmpty()) "no downloads" else "${saved.size} download${if (saved.size == 1) "" else "s"}",
                    "widgets, calendar",
                ).joinToString(" · ")
                SettingsGroup.About -> "daVIT ${remember { AutoUpdate.current(context) }}"
            }

            SegmentedListItem(
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                shapes = ListItemDefaults.segmentedShapes(index = g.ordinal, count = SettingsGroup.entries.size),
                leadingContent = { IconGlyph(g.icon, MaterialTheme.colorScheme.primary, Modifier.size(22.dp)) },
                content = { Text(g.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)) },
                supportingContent = { Text(summary.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingContent = { IconGlyph(RisoIcon.Next, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), Modifier.size(16.dp)) },
                onClick = { open(nav, g) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
                    .semantics { contentDescription = "${g.title}: $summary" },
            )
        }

        Spacer(Modifier.height(24.dp))
        val updates = rememberUpdateAction()
        MaterialButton(updates.button, updates.onTap, enabled = !updates.busy, quiet = !updates.ready, fillWidth = true, modifier = Modifier.fillMaxWidth())
        updates.message?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        var confirm by remember { mutableStateOf(false) }
        if (confirm) {
            Text(
                "Signs out of VTOP and erases your password, session and cached data from this phone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            MaterialButton("Yes, sign out", { nav.back(); repo.signOut() }, fillWidth = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            MaterialButton("Keep me signed in", { confirm = false }, quiet = true, fillWidth = true, modifier = Modifier.fillMaxWidth())
        } else {
            MaterialButton("Sign out", { confirm = true }, quiet = true, fillWidth = true, modifier = Modifier.fillMaxWidth())
        }
    }
}
