package dev.medriid.davit.ui.material3

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import dev.medriid.davit.sync.AutoUpdate
import dev.medriid.davit.sync.BackgroundSync
import dev.medriid.davit.ui.rememberUpdateAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun MaterialAboutSettings() {
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    MaterialSettingsSectionHeader("Updates")
    val version = remember { AutoUpdate.current(context) }
    val updates = rememberUpdateAction()
    val autoSupported = remember(context) { AutoUpdate.supported(context) }
    val updatesCount = if (autoSupported) 2 else 1

    MaterialSegmentedActionRow(
        title = "daVIT $version",
        body = updates.message ?: "Check GitHub for a newer release.",
        action = updates.label,
        index = 0,
        count = updatesCount,
        onClick = updates.onTap,
    )

    if (autoSupported) {
        var auto by remember { mutableStateOf(AutoUpdate.enabled(context)) }
        MaterialSegmentedToggleRow(
            title = "Update by itself",
            body = "Every three hours, asks GitHub for a new release and downloads it. Installing is always your tap.",
            checked = auto,
            index = 1,
            count = 2,
            onCheckedChange = {
                auto = it
                AutoUpdate.setEnabled(context, auto)
            },
        )
    }

    MaterialSettingsSectionHeader("Credits")
    val open = { url: String ->
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        Unit
    }

    MaterialSegmentedActionRow(
        title = "Campus map by Jeman",
        body = "Shared under the MIT licence.",
        action = "Jeman",
        index = 0,
        count = 3,
        onClick = { open("https://www.linkedin.com/in/jemankalita/") },
    )
    MaterialSegmentedActionRow(
        title = "Faculty reviews by No8AM",
        body = "Student ratings and reviews, as of 1 November 2026.",
        action = "No8AM",
        index = 1,
        count = 3,
        onClick = { open("https://no8am.site/") },
    )
    MaterialSegmentedActionRow(
        title = "Map data",
        body = "© OpenStreetMap contributors, under the Open Database Licence.",
        action = "OSM",
        index = 2,
        count = 3,
        onClick = { open("https://www.openstreetmap.org/copyright") },
    )
    Text(
        "Fonts: Bagel Fat One and IBM Plex Mono, under the SIL Open Font Licence.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp),
    )

    MaterialSettingsSectionHeader("Debugging")
    var queued by remember { mutableStateOf(false) }
    LaunchedEffect(state.lastBackground) { queued = false }

    MaterialSegmentedActionRow(
        title = "Test background sync",
        body = if (queued) "Queued: lock the phone now, then check \"Last\" under Sync in the background in a minute."
        else "Runs one background sync in 30 seconds.",
        action = if (queued) "Queued" else "Run",
        index = 0,
        count = 3,
        onClick = {
            BackgroundSync.testSoon(context)
            queued = true
        },
    )

    MaterialSegmentedToggleRow(
        title = "Save VTOP pages",
        body = "Keeps the last raw page of each kind on this phone so a broken parser can be fixed. Your username and tokens are scrubbed.",
        checked = state.dumpPages,
        index = 1,
        count = 3,
        onCheckedChange = { repo.setDumpPages(it) },
    )

    var exported by remember { mutableStateOf(false) }
    MaterialSegmentedActionRow(
        title = "Export snapshot",
        body = if (exported) "Exported snapshot JSON file."
        else if (state.snapshot == null) "No snapshot cached on this phone yet."
        else "Exports the cached VTOP data structure as a JSON file.",
        action = if (exported) "Exported" else "Export",
        index = 2,
        count = 3,
        onClick = {
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
        },
    )
}
