package dev.medriid.davit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.FileStore
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.data.Repo
import dev.medriid.davit.data.sizeText
import dev.medriid.davit.design.AppStyle
import dev.medriid.davit.design.HalftoneBar
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.InkChip
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.design.material.MaterialButton
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
@Composable
fun FileRow(
    key: String,
    title: String,
    subtitle: String?,
    fetch: (VtopClient, OutputStream, (Long, Long?) -> Unit) -> Download,
    modifier: Modifier = Modifier,
    highlighted: @Composable (() -> Unit)? = null,
    index: Int = 0,
    count: Int = 1,
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

    val line = remember(subtitle, file) {
        listOfNotNull(subtitle, file?.let { "saved · " + sizeText(it.length()) }).joinToString(" · ")
    }

    if (inks.style == AppStyle.Material3) {
        MaterialFileRow(
            title = title,
            line = line,
            file = file,
            p = p,
            askDelete = askDelete,
            error = error,
            highlighted = highlighted,
            open = open,
            save = save,
            onLongClick = { if (file != null) askDelete = !askDelete },
            onDelete = { repo.files.delete(key); askDelete = false; haptics.tick() },
            onShare = { file?.let(repo.files::share); askDelete = false },
            onKeep = { askDelete = false },
            index = index,
            count = count,
        )
        return
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialFileRow(
    title: String,
    line: String,
    file: File?,
    p: FileStore.Progress?,
    askDelete: Boolean,
    error: String?,
    highlighted: @Composable (() -> Unit)?,
    open: () -> Unit,
    save: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onKeep: () -> Unit,
    index: Int = 0,
    count: Int = 1,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.targetValue) {
        if (file != null && dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
            onDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    val itemShape = remember(index, count) {
        if (count > 1) {
            val top = if (index == 0) 16.dp else 4.dp
            val bottom = if (index == count - 1) 16.dp else 4.dp
            RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
        } else {
            RoundedCornerShape(16.dp)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = file != null,
        enableDismissFromEndToStart = file != null,
        backgroundContent = {
            if (file != null) {
                val direction = dismissState.dismissDirection
                val alignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 1.dp)
                        .clip(itemShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (direction == SwipeToDismissBoxValue.StartToEnd) {
                            IconGlyph(
                                RisoIcon.Download,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Delete download",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        } else {
                            Text(
                                "Delete download",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Spacer(Modifier.width(8.dp))
                            IconGlyph(
                                RisoIcon.Download,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        },
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp)
                .combinedClickable(role = Role.Button, onClick = open, onLongClick = onLongClick),
            shape = itemShape,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (file != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            IconGlyph(
                                if (file != null) RisoIcon.Download else RisoIcon.Materials,
                                color = if (file != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        if (highlighted != null) {
                            highlighted()
                        } else {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (line.isNotEmpty()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                line,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    when {
                        p != null -> InkChip(p.fraction?.let { "${(it * 100).toInt()}%" } ?: sizeText(p.done), solid = true)
                        file != null -> MaterialButton("Open", open, compact = true)
                        else -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            MaterialButton("Save", save, quiet = true, compact = true)
                            MaterialButton("View", open, compact = true)
                        }
                    }
                }
                if (p != null) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (p.fraction ?: 0.5f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                }
                AnimatedVisibility(askDelete && file != null) {
                    Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MaterialButton("Delete download", onDelete, quiet = false, compact = true)
                        MaterialButton("Share", onShare, quiet = true, compact = true)
                        MaterialButton("Keep", onKeep, quiet = true, compact = true)
                    }
                }
                error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
