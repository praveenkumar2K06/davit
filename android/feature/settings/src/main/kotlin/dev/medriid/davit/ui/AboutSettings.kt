package dev.medriid.davit.ui

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.sync.AutoUpdate
import dev.medriid.davit.sync.BackgroundSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
internal fun AboutSettings() {
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    SettingsCard("Updates") {
        val version = remember { AutoUpdate.current(context) }
        val updates = rememberUpdateAction()
        SettingRow("daVIT $version", updates.message ?: "Check GitHub for a newer release.", updates.label, updates.onTap)
        if (AutoUpdate.supported(context)) {
            var auto by remember { mutableStateOf(AutoUpdate.enabled(context)) }
            ToggleRow(
                "Update by itself",
                "Every three hours, asks GitHub for a new release and downloads it. Installing is always your tap.",
                auto,
            ) {
                auto = !auto
                AutoUpdate.setEnabled(context, auto)
            }
        }
    }

    // The people whose work this stands on.
    SettingsCard("Credits") {
        val open = { url: String -> runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,
            url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }; Unit }
        SettingRow("Campus map by Jeman", "Shared under the MIT licence.", "Jeman") { open("https://www.linkedin.com/in/jemankalita/") }
        SettingRow("Faculty reviews by No8AM", "Student ratings and reviews, as of 1 November 2026.", "No8AM") { open("https://no8am.site/") }
        SettingRow("Map data", "© OpenStreetMap contributors, under the Open Database Licence.", "OSM") { open("https://www.openstreetmap.org/copyright") }
        InkText("Fonts: Bagel Fat One and IBM Plex Mono, under the SIL Open Font Licence.", style = RisoType.small, modifier = Modifier.padding(top = 6.dp))
    }

    // For fixing things.
    SettingsCard("Debugging") {
        var queued by remember { mutableStateOf(false) }
        // A new "Last" line means the queued run happened.
        LaunchedEffect(state.lastBackground) { queued = false }
        SettingRow(
            "Test background sync",
            if (queued) "Queued: lock the phone now, then check \"Last\" under Sync in the background in a minute."
            else "Runs one background sync in 30 seconds.",
            if (queued) "Queued" else "Run",
        ) {
            BackgroundSync.testSoon(context)
            queued = true
        }
        ToggleRow(
            "Save VTOP pages",
            "Keeps the last raw page of each kind on this phone so a broken parser can be fixed. Your username and tokens are scrubbed.",
            state.dumpPages,
        ) { repo.setDumpPages(!state.dumpPages) }
        var exported by remember { mutableStateOf(false) }
        SettingRow(
            "Export snapshot",
            if (exported) "Exported snapshot JSON file."
            else if (state.snapshot == null) "No snapshot cached on this phone yet."
            else "Exports the cached VTOP data structure as a JSON file.",
            if (exported) "Exported" else "Export",
        ) {
            val snapshot = state.snapshot
            if (snapshot != null) {
                scope.launch {
                    val file = withContext(Dispatchers.IO) {
                        File(context.cacheDir, "exports").apply { mkdirs() }.resolve("snapshot.json").apply {
                            writeText(snapshot.toJson())
                        }
                    }
                    repo.files.share(file)
                    exported = true
                }
            }
        }
    }
}
