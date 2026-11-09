package dev.medriid.davit.design

import android.os.Build
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
import androidx.compose.ui.unit.TextUnit

/** Provides the ink pair to everything below. Wrap the root in [Grain] for paper + grain. */
@Composable
fun RisoTheme(inks: Inks = InkPairs.all.first(), content: @Composable () -> Unit) {
    // The type follows the look; set before anything below composes.
    RisoType.soft = inks.soft
    RisoType.editorial = inks.editorial
    RisoType.material = inks.material

    val context = LocalContext.current
    val dynamicColor = inks.material && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicScheme = if (dynamicColor) {
        runCatching {
            if (inks.dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }.getOrNull()
    } else null

    val colorScheme = dynamicScheme ?: if (inks.dark) {
        darkColorScheme(
            primary = inks.a,
            onPrimary = inks.onA,
            surface = inks.paper,
            onSurface = inks.text,
            surfaceVariant = inks.surface,
            onSurfaceVariant = inks.text,
            outline = inks.rule,
        )
    } else {
        lightColorScheme(
            primary = inks.a,
            onPrimary = inks.onA,
            surface = inks.paper,
            onSurface = inks.text,
            surfaceVariant = inks.surface,
            onSurfaceVariant = inks.text,
            outline = inks.rule,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = {
            CompositionLocalProvider(LocalInks provides inks, content = content)
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
