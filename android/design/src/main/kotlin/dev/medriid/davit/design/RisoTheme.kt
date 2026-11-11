package dev.medriid.davit.design

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Provides the ink pair to everything below. Wrap the root in [Grain] for paper + grain. */
@Composable
fun RisoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    inks: Inks = if (darkTheme) InkPairs.night.first() else InkPairs.all.first(),
    content: @Composable () -> Unit,
) {
    val activeInks = inks

    // The type follows the look; set before anything below composes.
    RisoType.soft = activeInks.soft
    RisoType.editorial = activeInks.editorial
    RisoType.material = activeInks.material

    val context = LocalContext.current
    val dynamicColor = activeInks.material && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicScheme = if (dynamicColor) {
        runCatching {
            if (activeInks.dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }.getOrNull()
    } else null

    val colorScheme = dynamicScheme ?: if (activeInks.dark) {
        darkColorScheme(
            primary = activeInks.a,
            onPrimary = activeInks.onA,
            surface = activeInks.paper,
            onSurface = activeInks.text,
            surfaceVariant = activeInks.surface,
            onSurfaceVariant = activeInks.text,
            outline = activeInks.rule,
        )
    } else {
        lightColorScheme(
            primary = activeInks.a,
            onPrimary = activeInks.onA,
            surface = activeInks.paper,
            onSurface = activeInks.text,
            surfaceVariant = activeInks.surface,
            onSurfaceVariant = activeInks.text,
            outline = activeInks.rule,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = {
            CompositionLocalProvider(LocalInks provides activeInks, content = content)
        },
    )
}

/**
 * Plain text in the current ink: the text pull on paper; on a [TiltedCard], ink B
 * multiplied into the ink-A block. [label] uppercases the string and defaults to the
 * spaced label style.
 */
@Composable
fun InkText(
    text: String,
    modifier: Modifier = Modifier,
    label: Boolean = false,
    style: TextStyle = if (label) RisoType.label else RisoType.body,
    maxLines: Int = Int.MAX_VALUE,
) {
    val inks = LocalInks.current
    val onCard = LocalOnCard.current

    if (inks.material) {
        Text(
            if (label) text.uppercase() else text,
            modifier = modifier,
            // A colour the caller set (an accent, a muted caption) wins; otherwise the text ink.
            style = style.copy(color = if (onCard) MaterialTheme.colorScheme.onPrimary else if (style.color != Color.Unspecified) style.color else inks.text),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
        return
    }

    BasicText(
        if (label) text.uppercase() else text,
        modifier = if (onCard) modifier.multiply() else modifier,
        // A colour the caller set (an accent, a muted caption) wins; otherwise the text ink.
        style = style.copy(color = if (onCard) inks.b else if (style.color != Color.Unspecified) style.color else inks.text),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Static display heading in one ink, multiplied (`.sec-head h2`). Use [Overprint] for the
 * screen's single animated headline, and this for everything else.
 */
@Composable
fun Heading(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = LocalInks.current.b,
    uppercase: Boolean = true,
    onCard: Boolean = false,
) {
    // Soft: plain sans in sentence case, in the text colour.
    val soft = LocalInks.current.soft || LocalInks.current.editorial || LocalInks.current.material

    val isMaterial = LocalInks.current.material
    BasicText(
        if (uppercase && !soft) text.uppercase() else text,
        modifier = if (soft) modifier else modifier.trimToCaps(fontSize).multiply(),
        style = RisoType.display(fontSize).copy(color = if(isMaterial && onCard) MaterialTheme.colorScheme.onPrimary else if (soft) LocalInks.current.text else color),
        overflow = TextOverflow.Visible,
    )
}

@Preview(showBackground = true)
@Composable
private fun RisoThemeTextPreview() {
    RisoTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Heading("Main Heading", fontSize = 28.sp)
            InkText("Standard body ink text content goes here.")
            InkText("Label Ink Text", label = true)
        }
    }
}

