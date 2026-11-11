package dev.medriid.davit.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.data.Updates
import dev.medriid.davit.design.AppStyle
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.InkButton
import dev.medriid.davit.design.InkCard
import dev.medriid.davit.design.InkField
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.sync.AutoUpdate
import dev.medriid.davit.sync.Notifier
import dev.medriid.davit.ui.material3.MaterialSettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Settings, behind the mascot: you at the top, Find a setting, then six groups, each with
 * how it's set now, each opening its own short page. Signing out at the end.
 */
@Composable
fun SettingsScreen(route: Route) {
    if (LocalInks.current.material) {
        MaterialSettingsScreen(route)
        return
    }

    val repo = LocalRepo.current
    val nav = LocalNav.current
    val state by repo.state.collectAsState()
    val context = LocalContext.current
    val snapshot = state.snapshot

    RouteScreen(route, "Settings") {
        // You, in a line or two: the groups below should fit without scrolling.
        val profile = snapshot?.profile
        InkCard(Modifier.fillMaxWidth(), plateOffset = 4.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                profile?.photo?.let { VtopPhoto(it) }
                Column(Modifier.weight(1f)) {
                    InkText(profile?.name?.ifBlank { null } ?: state.regNo.orEmpty(), style = RisoType.title, maxLines = 1)
                    InkText(
                        listOfNotNull(
                            snapshot?.semester?.name?.substringBefore(" - "),
                            profile?.hostel?.room?.ifBlank { null }?.let { "Room $it" },
                        ).joinToString(" · "),
                        style = RisoType.small,
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        // Starts empty each time Settings opens.
        var query by remember { mutableStateOf("") }
        InkField(query, { query = it }, "Find a setting", modifier = Modifier.fillMaxWidth())
        val q = query.trim().lowercase()
        if (q.isNotEmpty()) {
            val hits = SettingsIndex.filter { (name, words, _) -> q in name.lowercase() || q in words }
            Spacer(Modifier.height(10.dp))
            if (hits.isEmpty()) InkText("Nothing called that. Try another word.", style = RisoType.small)
            hits.forEach { (name, _, group) ->
                SettingRow(name, group.title, "Open") { open(nav, group) }
            }
            return@RouteScreen
        }

        // The groups, each with how it's set now.
        Spacer(Modifier.height(8.dp))
        val blocked = state.alerts && !Notifier.permitted(context)
        val saved by repo.files.saved.collectAsState()
        val currentStyleName = LocalInks.current.style.title
        val isClassicStyle = LocalInks.current.style == AppStyle.Classic
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
            GroupRow(g, summary) { open(nav, g) }
        }

        Spacer(Modifier.height(24.dp))
        val updates = rememberUpdateAction()
        InkButton(updates.button, updates.onTap, enabled = !updates.busy, quiet = !updates.ready, fillWidth = true, modifier = Modifier.fillMaxWidth())
        updates.message?.let { InkText(it, style = RisoType.small, modifier = Modifier.padding(top = 6.dp)) }
        Spacer(Modifier.height(10.dp))
        var confirm by remember { mutableStateOf(false) }
        if (confirm) {
            InkText("Signs out of VTOP and erases your password, session and cached data from this phone.", style = RisoType.small)
            Spacer(Modifier.height(10.dp))
            InkButton("Yes, sign out", { nav.back(); repo.signOut() }, fillWidth = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            InkButton("Keep me signed in", { confirm = false }, quiet = true, fillWidth = true, modifier = Modifier.fillMaxWidth())
        } else {
            InkButton("Sign out", { confirm = true }, quiet = true, fillWidth = true, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** What tapping "update" does now, for the index's button and the Updates row. */
internal class UpdateAction(val label: String, val button: String, val message: String?, val busy: Boolean, val ready: Boolean, val onTap: () -> Unit)

/** Checks GitHub on a tap, then offers the install. Dev builds are a different app; they only point at the release page. */
@Composable
internal fun rememberUpdateAction(): UpdateAction {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val version = remember { AutoUpdate.current(context) }
    var update by remember { mutableStateOf<String?>(null) }
    var checking by remember { mutableStateOf(false) }
    if (AutoUpdate.supported(context)) {
        val ready by AutoUpdate.ready.collectAsState()
        val r = ready
        if (r != null) {
            return UpdateAction("Install", "Install ${r.version}", "${r.version} is downloaded. Install keeps your data and sign-in.",
                busy = false,
                ready = true
            ) { AutoUpdate.install(context) }
        }
        return UpdateAction(if (checking) "Checking" else "Check", if (checking) "Checking…" else "Check for updates", update, checking, false) {
            if (!checking) {
                checking = true
                update = "Checking GitHub…"
                scope.launch {
                    update = try {
                        if (AutoUpdate.check(context) == null) "You're on the latest version." else null
                    } catch (e: Exception) {
                        "Couldn't download the update. Try again later."
                    }
                    checking = false
                }
            }
        }
    }
    var release by remember { mutableStateOf<Updates.Release?>(null) }
    val rel = release
    if (rel != null) {
        return UpdateAction("Get", "Get ${rel.version}", update, busy = false, ready = true) {
            context.startActivity(Intent(Intent.ACTION_VIEW, rel.url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
    return UpdateAction(if (checking) "Checking" else "Check", if (checking) "Checking…" else "Check for updates", update, checking, false) {
        if (!checking) {
            checking = true
            update = "Checking…"
            scope.launch {
                update = try {
                    val latest = withContext(Dispatchers.IO) { Updates.latest() }
                    if (Updates.isNewer(latest.version, version)) {
                        release = latest
                        "${latest.version} is out. Tap Get to see it."
                    } else {
                        "You're on the latest version."
                    }
                } catch (e: Exception) {
                    "Couldn't reach GitHub. Try again later."
                }
                checking = false
            }
        }
    }
}

internal fun open(nav: Nav, g: SettingsGroup) = nav.go(if (g == SettingsGroup.Alerts) Route.Notifications else Route.SettingsPage(g.id))

/** One group on the index: its icon, its name, how it's set now. */
@Composable
private fun GroupRow(g: SettingsGroup, summary: String, onClick: () -> Unit) {
    val inks = LocalInks.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "${g.title}: $summary" }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconGlyph(g.icon, inks.text, Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            InkText(g.title, style = RisoType.body.copy(fontWeight = FontWeight.SemiBold))
            InkText(summary.replaceFirstChar { it.uppercase() }, style = RisoType.small, maxLines = 2)
        }
        Spacer(Modifier.width(8.dp))
        InkText("›", style = RisoType.title)
    }
    Hairline()
}

