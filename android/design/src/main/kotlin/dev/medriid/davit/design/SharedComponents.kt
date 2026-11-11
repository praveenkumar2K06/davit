package dev.medriid.davit.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Gutter = 18.dp

fun highlight(text: String, ranges: List<IntRange>, ink: Color): AnnotatedString = buildAnnotatedString {
    append(text)
    ranges.forEach { r ->
        if (r.first in text.indices && r.last in text.indices) {
            addStyle(SpanStyle(color = ink, fontWeight = FontWeight.Bold), r.first, r.last + 1)
        }
    }
}

@Composable
fun BackButton(onClick: () -> Unit) {
    val inks = LocalInks.current
    val shape = if (inks.editorial) EditorialShape else CircleShape
    Box(
        Modifier
            .size(44.dp)
            .border(
                if (inks.soft || inks.editorial) 1.dp else 2.dp,
                when {
                    inks.editorial -> inks.rule
                    inks.soft -> inks.text.copy(alpha = 0.25f)
                    else -> inks.text
                },
                shape,
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Back" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(18.dp)) {
            val w = 2.4.dp.toPx()
            drawLine(inks.text, Offset(size.width * 0.62f, size.height * 0.18f), Offset(size.width * 0.3f, size.height * 0.5f), w, StrokeCap.Round)
            drawLine(inks.text, Offset(size.width * 0.3f, size.height * 0.5f), Offset(size.width * 0.62f, size.height * 0.82f), w, StrokeCap.Round)
        }
    }
}

@Composable
fun RouteHead(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null) {
    val inks = LocalInks.current
    if (inks.editorial) {
        Column(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 14.dp)) {
            if (onBack != null) BackButton(onBack)
            Spacer(Modifier.height(18.dp))
            BasicText(title, style = RisoType.display(36.sp).copy(color = inks.text))
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                BasicText(subtitle, style = RisoType.small.copy(color = inks.muted, fontSize = 14.sp), maxLines = 3)
            }
            Spacer(Modifier.height(14.dp))
            Rule()
        }
        return
    }
    Row(Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            BackButton(onBack)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Heading(title, 24.sp)
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                InkText(subtitle, style = RisoType.small, maxLines = 2)
            }
        }
    }
}

@Composable
fun Loading(text: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        InkText(text, style = RisoType.small)
        Spacer(Modifier.height(12.dp))
        LoadingPlate(lines = 4)
    }
}

/** A failure under the data it couldn't refresh, with a retry. */
@Composable
fun ErrorNote(text: String, onRetry: (() -> Unit)? = null) {
    InkCard(Modifier.fillMaxWidth().padding(vertical = 6.dp), plateOffset = 3.dp) {
        InkText(text, style = RisoType.small)
        if (onRetry != null) {
            Spacer(Modifier.height(8.dp))
            InkButton("Retry", onRetry)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SharedComponentsPreview() {
    RisoTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RouteHead(title = "Dashboard", subtitle = "Overview of your day", onBack = {})
            BackButton(onClick = {})
            Loading(text = "Loading data...")
            ErrorNote(text = "Failed to load schedule", onRetry = {})
        }
    }
}

