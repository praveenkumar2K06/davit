package dev.medriid.davit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.data.Repo
import dev.medriid.davit.data.sizeText
import dev.medriid.davit.design.HalftoneBar
import dev.medriid.davit.design.InkChip
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.design.rememberHaptics
import dev.medriid.davit.vtop.Download
import dev.medriid.davit.vtop.VtopClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream

fun File.isPdf(): Boolean = extension.equals("pdf", ignoreCase = true)

/**
 * Downloads [key] into app storage (in the app's scope, so leaving the screen doesn't stop
 * it), then calls [done] on the main thread.
 */
fun Repo.fetchFile(
    key: String,
    block: (VtopClient, OutputStream, (Long, Long?) -> Unit) -> Download,
    preview: Boolean = false,
    done: (Result<File>) -> Unit,
) {
    scope.launch {
        val r = runCatching { files.fetch(key, preview) { tmp, progress -> onVtop { c -> tmp.outputStream().buffered().use { block(c, it, progress) } } } }
        withContext(Dispatchers.Main) { done(r) }
    }
}

/**
 * A file VTOP serves. Tap to read it: PDFs open inside the app without landing in Downloads,
 * anything else goes to whichever app opens it. Save keeps a copy for offline; long-press
 * on a saved one to delete or share it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileRow(
    key: String,
    title: String,
    subtitle: String?,
    fetch: (VtopClient, OutputStream, (Long, Long?) -> Unit) -> Download,
    modifier: Modifier = Modifier,
    highlighted: @Composable (() -> Unit)? = null,
) {
    val repo = LocalRepo.current
    val inks = LocalInks.current
    val haptics = rememberHaptics()
    val saved by repo.files.saved.collectAsState()
    val progress by repo.files.progress.collectAsState()
    val file = saved[key]?.takeIf { it.exists() }
    val p = progress[key]
    var error by remember(key) { mutableStateOf<String?>(null) }
    var askDelete by remember(key) { mutableStateOf(false) }
    val nav = LocalNav.current
    val show: (File) -> Unit = { f ->
        if (f.isPdf()) {
            nav.go(Route.PdfView(key, f.path, title))
        } else {
            val kept = repo.files.keep(key)
            if (kept == null || !repo.files.open(kept)) error = "No app on this phone opens .${f.extension} files."
        }
    }
    val failed: (Throwable) -> Unit = {
        haptics.reject()
        error = repo.explain(it)
    }
    val open = {
        error = null
        val ready = file ?: repo.files.preview(key)
        if (ready != null) {
            show(ready)
        } else if (p == null) {
            repo.fetchFile(key, fetch, preview = true) { r -> r.onSuccess(show).onFailure(failed) }
        }
    }
    val save = {
        error = null
        if (repo.files.keep(key) != null) {
            haptics.confirm()
        } else if (p == null) {
            repo.fetchFile(key, fetch) { r -> r.onSuccess { haptics.confirm() }.onFailure(failed) }
        }
    }
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(role = Role.Button, onClick = open, onLongClick = { if (file != null) askDelete = !askDelete })
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                if (highlighted != null) highlighted() else InkText(title, style = RisoType.body.copy(fontWeight = FontWeight.SemiBold), maxLines = 3)
                val line = listOfNotNull(subtitle, file?.let { "saved · " + sizeText(it.length()) }).joinToString(" · ")
                if (line.isNotEmpty()) InkText(line, style = RisoType.small, maxLines = 2)
            }
            Spacer(Modifier.width(10.dp))
            when {
                p != null -> InkChip(p.fraction?.let { "${(it * 100).toInt()}%" } ?: sizeText(p.done), solid = true)
                file != null -> InkChip("Open", solid = true)
                else -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.clickable(role = Role.Button, onClick = save)) { InkChip("Save") }
                    InkChip("View", solid = true)
                }
            }
        }
        if (p != null) {
            HalftoneBar(p.fraction ?: 0.5f, height = 6.dp)
            Spacer(Modifier.height(6.dp))
        }
        AnimatedVisibility(askDelete && file != null) {
            Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.clickable(role = Role.Button) { repo.files.delete(key); askDelete = false; haptics.tick() }) { InkChip("Delete download", solid = true) }
                Box(Modifier.clickable(role = Role.Button) { file?.let(repo.files::share); askDelete = false }) { InkChip("Share") }
                Box(Modifier.clickable(role = Role.Button) { askDelete = false }) { InkChip("Keep") }
            }
        }
        error?.let { InkText(it, style = RisoType.small.copy(color = inks.a), modifier = Modifier.padding(bottom = 6.dp)) }
    }
}
