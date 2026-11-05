package dev.medriid.davit.data

import android.content.Context
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.Kind
import dev.medriid.davit.design.argb
import dev.medriid.davit.design.contrast
import dev.medriid.davit.design.liftedFor
import dev.medriid.davit.design.textFor
import dev.medriid.davit.design.toned
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Your colours: one of the ink pairs to start from ([base]) and whatever you changed on
 * top of it. A null colour means "as the pair has it" (or worked out, for text); colours
 * are stored as ARGB numbers.
 *
 * Primary and secondary are shared by day and night; at night secondary is lifted, if it
 * must be, so it still reads on the dark paper. Each paper has its own text.
 */
@Serializable
data class Palette(
    val base: Int = 0,
    val a: Long? = null,
    val b: Long? = null,
    val paper: Long? = null,
    val nightPaper: Long? = null,
    val text: Long? = null,
    val nightText: Long? = null,
    /** Kind name → colour, for the kinds you changed. */
    val kinds: Map<String, Long> = emptyMap(),
    /** Cards washed with their kind's colour, or plain paper. */
    val tinted: Boolean = true,
) {
    /** Anything changed from the pair? */
    val custom: Boolean
        get() = a != null || b != null || paper != null || nightPaper != null || text != null || nightText != null || kinds.isNotEmpty()

    fun kind(k: Kind): Color = kinds[k.name]?.let(::color) ?: k.default

    /** The inks to print with, by day or at night. */
    fun inks(dark: Boolean): Inks {
        val day = build(false)
        return if (dark) build(true).copy(dayTwin = day) else day
    }

    private fun build(dark: Boolean): Inks {
        val pair = InkPairs.of(base, dark)
        val paperOverride = (if (dark) nightPaper else paper)?.let(::color)
        val p = paperOverride ?: pair.paper
        // Your primary prints toned down at night like the pair's own.
        val inkA = a?.let(::color)?.let { if (dark) it.toned(pair.tone) else it } ?: pair.a
        // Your secondary by day; at night the pair's lifted one, or yours lifted to read.
        val inkB = b?.let(::color)?.let { if (dark || p.luminanceDark()) it.liftedFor(p, 3f) else it } ?: pair.b.liftedFor(p, 3f)
        val textOverride = (if (dark) nightText else text)?.let(::color)
        val t = textOverride ?: if (b == null && paperOverride == null) pair.text.takeIf { it.isReadableOn(p) } ?: textFor(p, inkB) else textFor(p, inkB)
        return Inks(
            name = if (custom) "Your own" else pair.name,
            paper = p,
            a = inkA,
            b = inkB,
            text = t,
            ruleAlpha = pair.ruleAlpha,
            dark = p.luminanceDark(),
            kinds = Kind.entries.map { k -> kinds[k.name]?.let(::color) },
            tinted = tinted,
            tone = pair.tone,
            sticker = pair.sticker && !p.luminanceDark(),
            glass = pair.glass,
            soft = pair.soft,
            editorial = pair.editorial,
            material = pair.material,
        )
    }

    fun withKind(k: Kind, c: Color?): Palette = copy(kinds = if (c == null) kinds - k.name else kinds + (k.name to c.argb()))

    fun toJson(): String = PaletteJson.encodeToString(serializer(), this)

    companion object {
        fun color(v: Long): Color = Color(v or 0xFF000000)

        fun fromJson(s: String?): Palette? = s?.let { runCatching { PaletteJson.decodeFromString(serializer(), it) }.getOrNull() }
    }
}

/**
 * Returns [this] Inks with system Material You dynamic wallpaper colors applied on Android 12+ (API 31+).
 */
fun Inks.withDynamicColor(context: Context): Inks {
    if (!material || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return this
    return runCatching {
        val scheme = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        val updatedDayTwin = dayTwin?.withDynamicColor(context)
        copy(
            paper = scheme.surface,
            a = scheme.primary,
            b = scheme.onSurface,
            text = scheme.onSurface,
            customOnA = scheme.onPrimary,
            dayTwin = updatedDayTwin,
        )
    }.getOrDefault(this)
}

private val PaletteJson = Json { ignoreUnknownKeys = true }

private fun Color.luminanceDark(): Boolean = luminance() <= 0.4f

private fun Color.isReadableOn(paper: Color): Boolean = contrast(paper) >= 4.5f
