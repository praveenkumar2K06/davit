package dev.medriid.davit.widget

import android.content.res.Configuration
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.medriid.davit.data.UiState
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.Kind

/**
 * Applies Material You dynamic theming to Glance widgets.
 * On Android 12+ (API 31+), if the user has selected Material 3 or standard dynamic colors,
 * GlanceTheme provides dynamic wallpaper colors.
 * If the user has selected a custom ink pair in daVIT settings, it maps those inks to a
 * matching Material 3 ColorScheme so the widget stays visually aligned with the app.
 */
@Composable
internal fun DavitWidgetTheme(
    state: UiState,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val dark = state.theme == 2 || (state.theme == 0 && systemNight)
    val inks = state.palette.inks(dark)

    if (inks.material) {
        GlanceTheme(content = content)
    } else {
        val lightInks = state.palette.inks(false)
        val darkInks = state.palette.inks(true)
        val colors = ColorProviders(
            light = lightColorSchemeFromInks(lightInks),
            dark = darkColorSchemeFromInks(darkInks),
        )
        GlanceTheme(colors = colors, content = content)
    }
}

@Composable
internal fun WidgetInfoCard(
    title: String,
    subtitle: String? = null,
    containerColor: ColorProvider = GlanceTheme.colors.primaryContainer,
    contentColor: ColorProvider = GlanceTheme.colors.onPrimaryContainer,
) {
    Column(
        GlanceModifier
            .fillMaxWidth()
            .cornerRadius(16.dp)
            .background(containerColor)
            .padding(12.dp),
    ) {
        Text(title, style = style(contentColor, 13.sp, bold = true))
        if (subtitle != null) {
            Spacer(GlanceModifier.height(2.dp))
            Text(subtitle, style = style(contentColor, 11.sp))
        }
    }
}

internal fun style(color: ColorProvider, size: TextUnit, bold: Boolean = false): TextStyle =
    TextStyle(color = color, fontSize = size, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)

private fun lightColorSchemeFromInks(inks: Inks): ColorScheme = lightColorScheme(
    primary = inks.a,
    onPrimary = inks.onA,
    primaryContainer = inks.fill(Kind.Classes),
    onPrimaryContainer = inks.text,
    secondary = inks.b,
    onSecondary = inks.paper,
    secondaryContainer = inks.fill(Kind.Attendance),
    onSecondaryContainer = inks.text,
    tertiary = inks.b,
    onTertiary = inks.paper,
    tertiaryContainer = inks.fill(Kind.Assignments),
    onTertiaryContainer = inks.text,
    surface = inks.paper,
    onSurface = inks.text,
    surfaceVariant = inks.raised,
    onSurfaceVariant = inks.muted,
    error = Kind.Vtop.default,
    onError = inks.paper,
    errorContainer = inks.fill(Kind.Vtop),
    onErrorContainer = inks.text,
    outline = inks.rule,
)

private fun darkColorSchemeFromInks(inks: Inks): ColorScheme = darkColorScheme(
    primary = inks.a,
    onPrimary = inks.onA,
    primaryContainer = inks.fill(Kind.Classes),
    onPrimaryContainer = inks.text,
    secondary = inks.b,
    onSecondary = inks.paper,
    secondaryContainer = inks.fill(Kind.Attendance),
    onSecondaryContainer = inks.text,
    tertiary = inks.b,
    onTertiary = inks.paper,
    tertiaryContainer = inks.fill(Kind.Assignments),
    onTertiaryContainer = inks.text,
    surface = inks.paper,
    onSurface = inks.text,
    surfaceVariant = inks.raised,
    onSurfaceVariant = inks.muted,
    error = Kind.Vtop.default,
    onError = inks.paper,
    errorContainer = inks.fill(Kind.Vtop),
    onErrorContainer = inks.text,
    outline = inks.rule,
)
