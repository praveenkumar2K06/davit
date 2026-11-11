package dev.medriid.davit.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.Banner
import dev.medriid.davit.data.BannerStat
import dev.medriid.davit.data.UiState
import dev.medriid.davit.data.ago
import dev.medriid.davit.data.bannerText
import dev.medriid.davit.data.overallPercent
import dev.medriid.davit.data.titleCase
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.Kind
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.design.RisoType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

fun greeting(hour: Int): String = when (hour) {
    in 4..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..22 -> "Good evening"
    else -> "Late night"
}

@Composable
fun StatChip(text: String) {
    val inks = LocalInks.current
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .clip(shape)
            .background(inks.paper)
            .border(1.5.dp, inks.text, shape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) { BasicText(text, maxLines = 1, style = RisoType.small.copy(color = inks.text, fontWeight = FontWeight.SemiBold)) }
}

/** Your photo, cropped to the card at [Banner.focus], or the chosen built-in print. */
@Composable
fun BannerBackground(banner: Banner, photoAt: Long, photo: File, modifier: Modifier) {
    val image by produceState<ImageBitmap?>(null, banner.photo, photoAt) {
        value = if (banner.photo && photo.exists()) withContext(Dispatchers.IO) {
            runCatching { BitmapFactory.decodeFile(photo.path)?.asImageBitmap() }.getOrNull()
        } else null
    }
    val img = image
    if (img != null) {
        Image(img, contentDescription = null, contentScale = ContentScale.Crop, alignment = BiasAlignment(0f, banner.focus * 2 - 1), modifier = modifier)
    } else {
        BannerArt(banner.style, modifier)
    }
}

/** The built-in prints, drawn in your inks and card colours so they follow your theme. */
@Composable
fun BannerArt(style: Int, modifier: Modifier = Modifier) {
    val inks = LocalInks.current
    Canvas(modifier.background(inks.fill(Kind.Classes))) { drawArt(style, inks) }
}

private fun DrawScope.drawArt(style: Int, inks: Inks) {
    val ramp = listOf(inks.surface, inks.raised, inks.a.copy(alpha = 0.2f).compositeOver(inks.surface))
    val soft = if (inks.editorial) Kind.entries.indices.map { ramp[it % 3] }
    else Kind.entries.map { inks.fill(it).takeIf { f -> f != inks.paper } ?: inks.mark(it).copy(alpha = 0.35f) }
    val line = Stroke(2.dp.toPx())
    when (style) {
        0 -> {
            drawRect(soft[1])
            listOf(0.3f, 0.5f, 0.7f, 0.88f).forEachIndexed { i, base ->
                val p = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(0f, size.height * base)
                    var x = 0f
                    while (x <= size.width) {
                        lineTo(x, size.height * base + sin(x / size.width * 2 * PI * (1.3 + i * 0.4) + i).toFloat() * size.height * 0.06f)
                        x += 6f
                    }
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(p, listOf(soft[2], soft[0], soft[3], soft[5])[i])
                drawPath(p, inks.text, style = line)
            }
        }
        1 -> {
            drawRect(soft[6])
            val c = Offset(size.width * 0.72f, size.height * 0.62f)
            for (r in 1..6) drawCircle(inks.a.copy(alpha = 0.12f * (7 - r) / 6f), size.height * (0.32f + r * 0.12f), c)
            drawCircle(inks.a, size.height * 0.32f, c)
            drawCircle(inks.text, size.height * 0.32f, c, style = line)
            drawRect(soft[0], Offset(0f, size.height * 0.82f), Size(size.width, size.height * 0.18f))
            drawLine(inks.text, Offset(0f, size.height * 0.82f), Offset(size.width, size.height * 0.82f), 2.dp.toPx())
        }
        2 -> {
            drawRect(soft[4])
            val rnd = Random(11)
            repeat(26) {
                val o = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
                val r = (6 + rnd.nextInt(14)).dp.toPx() / 2
                val c = listOf(inks.a, inks.b, soft[3], soft[5], soft[0])[rnd.nextInt(5)]
                when (rnd.nextInt(3)) {
                    0 -> drawCircle(c, r, o)
                    1 -> drawCircle(inks.text, r, o, style = line)
                    else -> drawLine(c, o, o + Offset(r * 2, -r), 3.dp.toPx())
                }
            }
        }
        3 -> {
            val cols = listOf(0f, 0.28f, 0.55f, 0.8f, 1f)
            val rows = listOf(0f, 0.45f, 1f)
            var k = 0
            for (r in 0 until rows.size - 1) for (c in 0 until cols.size - 1) {
                val tl = Offset(size.width * cols[c], size.height * rows[r])
                val br = Offset(size.width * cols[c + 1], size.height * rows[r + 1])
                drawRect(if (k == 5) inks.a else soft[(k * 3) % soft.size], tl, Size(br.x - tl.x, br.y - tl.y))
                drawRect(inks.text, tl, Size(br.x - tl.x, br.y - tl.y), style = line)
                k++
            }
        }
        4 -> {
            drawRect(soft[2])
            val w = 22.dp.toPx()
            var x = -size.height
            var i = 0
            while (x < size.width) {
                if (i % 2 == 0) {
                    val p = Path().apply {
                        moveTo(x, size.height); lineTo(x + w, size.height); lineTo(x + w + size.height, 0f); lineTo(x + size.height, 0f); close()
                    }
                    drawPath(p, soft[7])
                }
                x += w
                i++
            }
        }
        else -> {
            drawRect(inks.paper)
            val step = 12.dp.toPx()
            var y = step / 2
            while (y < size.height) {
                var x = step / 2
                while (x < size.width) {
                    drawCircle(inks.a, step * 0.48f * (x / size.width), Offset(x, y))
                    x += step
                }
                y += step
            }
        }
    }
}

/** Material 3 Hero Banner component used on Home, in Settings, and in Display Card preview. */
@Composable
fun MaterialBanner(
    state: UiState,
    mascot: Mascot,
    photo: File,
    modifier: Modifier = Modifier,
    onSync: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
    onMore: (() -> Unit)? = null,
    onOpenAttendance: (() -> Unit)? = null,
    compact: Boolean = false,
) {
    val snapshot = state.snapshot
    val now = remember { LocalDateTime.now() }
    val hasBg = !state.banner.hidden
    val profile = snapshot?.profile

    val headerTitle = if (state.banner.nameInBar) {
        profile?.name?.let(::titleCase)?.ifBlank { null } ?: state.regNo.orEmpty().ifBlank { "Student" }
    } else {
        "daVIT"
    }
    val headerSubtitle = if (state.banner.nameInBar) {
        greeting(now.hour)
    } else {
        greeting(now.hour) + (profile?.name?.let(::titleCase)?.ifBlank { null }?.let { " · $it" } ?: "")
    }

    val reminders = state.reminders
    val leftStat = remember(state.banner.left, snapshot, reminders, now) {
        snapshot?.let { s -> BannerStat.of(state.banner.left).takeIf { st -> st != BannerStat.Attendance }?.let { st -> s.bannerText(st, reminders, now) } }
    }
    val rightStat = remember(state.banner.right, snapshot, reminders, now) {
        snapshot?.let { s -> BannerStat.of(state.banner.right).takeIf { st -> st != BannerStat.Attendance }?.let { st -> s.bannerText(st, reminders, now) } }
    }

    val overall = snapshot?.attendance?.let { overallPercent(it) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Box(Modifier.fillMaxWidth()) {
            if (hasBg) {
                BannerBackground(state.banner, state.bannerPhotoAt, photo, Modifier.matchParentSize())
                Box(
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.48f),
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                )
            }

            Column(Modifier.padding(if (compact) 14.dp else 18.dp)) {
                // Top Header Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .then(if (onProfile != null) Modifier.clickable(role = Role.Button, onClick = onProfile) else Modifier)
                            .semantics { contentDescription = "Profile and settings" }
                    ) {
                        MascotAvatar(mascot, if (compact) 44.dp else 50.dp, ring = 2.5.dp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = headerSubtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hasBg) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (hasBg) Color.White else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                    if (onMore != null) {
                        Surface(
                            shape = CircleShape,
                            color = if (hasBg) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable(onClick = onMore)
                                .semantics { contentDescription = "VTOP Campus Hub" }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                IconGlyph(RisoIcon.More, if (hasBg) Color.White else MaterialTheme.colorScheme.onSurface, Modifier.size(20.dp))
                            }
                        }
                    }
                    if (onProfile != null) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (hasBg) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable(onClick = onProfile)
                                .semantics { contentDescription = "Settings" }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                IconGlyph(RisoIcon.Settings, if (hasBg) Color.White else MaterialTheme.colorScheme.onSurface, Modifier.size(20.dp))
                            }
                        }
                    }
                }

                if (!compact) {
                    Spacer(Modifier.height(12.dp))

                    // Dateline & Sync Details Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (hasBg) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (hasBg) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        val syncText = state.busy ?: ("Synced " + (snapshot?.syncedAt?.let { ago(it) } ?: "never"))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (hasBg) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier
                                .then(if (onSync != null && state.busy == null) Modifier.clickable(onClick = onSync) else Modifier)
                                .semantics { contentDescription = "Sync status: $syncText" }
                        ) {
                            Text(
                                text = syncText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (hasBg) Color.White else MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Display Card Custom Stat Chips Row
                    if (leftStat != null || rightStat != null) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            leftStat?.let { text ->
                                Surface(
                                    shape = CircleShape,
                                    color = if (hasBg) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "✦ $text",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (hasBg) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                            rightStat?.let { text ->
                                Surface(
                                    shape = CircleShape,
                                    color = if (hasBg) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = "✦ $text",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (hasBg) Color.White else MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (overall != null) {
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$overall%",
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (hasBg) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.padding(bottom = 6.dp)) {
                                    Text(
                                        text = "Overall Attendance",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = if (hasBg) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Enrolled Courses",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (hasBg) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                            if (onOpenAttendance != null) {
                                Text(
                                    text = "Details →",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (hasBg) Color.White else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .clickable { onOpenAttendance() }
                                        .padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Compact mode for Settings profile card
                    val roomStr = profile?.hostel?.room?.ifBlank { null }?.let { "Room $it" }
                    val semStr = snapshot?.semester?.name?.substringBefore(" - ")
                    val syncText = state.busy ?: ("Synced " + (snapshot?.syncedAt?.let { ago(it) } ?: "never"))
                    val detailsStr = listOfNotNull(semStr, roomStr, syncText).joinToString(" · ")
                    if (detailsStr.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = detailsStr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (hasBg) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.then(if (onSync != null && state.busy == null) Modifier.clickable(onClick = onSync) else Modifier)
                        )
                    }
                }
            }
        }
    }
}
