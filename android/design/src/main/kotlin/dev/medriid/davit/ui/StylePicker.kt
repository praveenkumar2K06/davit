package dev.medriid.davit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.medriid.davit.design.AppStyle
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.Kind
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoType

/**
 * Grid picker for choosing theme styles (Classic, Soft, Editorial, Material 3, Glass, etc.).
 * Scales dynamically in a 2-column layout to support adding more themes easily.
 */
@Composable
fun StylePicker(
    style: AppStyle,
    modifier: Modifier = Modifier,
    styles: List<AppStyle> = AppStyle.entries,
    onPick: (AppStyle) -> Unit,
) {
    val dark = LocalInks.current.dark
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        styles.chunked(2).forEach { rowStyles ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowStyles.forEach { item ->
                    val isSelected = item == style
                    val lineText = if (item == AppStyle.Material3 && android.os.Build.VERSION.SDK_INT >= 31) {
                        "Material You wallpaper dynamic colors"
                    } else item.description

                    StyleOption(
                        name = item.title,
                        line = lineText,
                        selected = isSelected,
                        modifier = Modifier.weight(1f),
                        onClick = { onPick(item) },
                    ) {
                        when (item) {
                            AppStyle.Classic -> drawClassic(InkPairs.of(0, dark))
                            AppStyle.Soft -> drawSoft(InkPairs.of(InkPairs.softIndex, dark))
                            AppStyle.Editorial -> drawEditorial(InkPairs.of(InkPairs.editorialIndex, dark))
                            AppStyle.Material3 -> drawMaterial3(InkPairs.of(InkPairs.materialIndex, dark))
                            AppStyle.Glass -> drawGlass(InkPairs.of(InkPairs.glassIndex, dark))
                        }
                    }
                }
                if (rowStyles.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/** Legacy overload for backward compatibility with boolean editorial parameter. */
@Composable
fun StylePicker(editorial: Boolean, onPick: (Boolean) -> Unit) {
    val currentStyle = LocalInks.current.style
    StylePicker(style = currentStyle, onPick = { picked ->
        onPick(picked == AppStyle.Editorial)
    })
}

@Composable
private fun StyleOption(
    name: String,
    line: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    draw: DrawScope.() -> Unit,
) {
    val inks = LocalInks.current
    val shape = RoundedCornerShape(if (inks.editorial) 3.dp else if (inks.material) 16.dp else 12.dp)
    Column(
        modifier
            .clip(shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) inks.text else inks.rule, shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics(mergeDescendants = true) {
                this.selected = selected
                contentDescription = "$name style. $line."
            }
            .padding(10.dp),
    ) {
        Canvas(Modifier.fillMaxWidth().height(108.dp).clip(RoundedCornerShape(6.dp)), onDraw = draw)
        Spacer(Modifier.height(8.dp))
        BasicText(name, style = RisoType.title.copy(color = inks.text, fontSize = 15.sp, fontWeight = FontWeight.Bold))
        BasicText(line, style = RisoType.small.copy(color = inks.muted, fontSize = 11.sp, lineHeight = 14.sp), maxLines = 3)
    }
}

/** Classic Home in miniature: two pastel cards with black outlines and hard shadows. */
private fun DrawScope.drawClassic(p: Inks) {
    drawRect(p.paper)
    val x = size.width / 100f
    val y = size.height / 100f
    fun fill(k: Kind) = if (p.dark) k.default.copy(alpha = 0.22f).compositeOver(p.paper) else k.pastel
    fun card(top: Float, bottom: Float, k: Kind) {
        val at = Offset(8 * x, top * y)
        val s = Size(80 * x, (bottom - top) * y)
        val r = CornerRadius(4 * x)
        drawRoundRect(p.text, at + Offset(3 * x, 3 * x), s, r)
        drawRoundRect(fill(k), at, s, r)
        drawRoundRect(p.text, at, s, r, style = Stroke(1.4f * x))
    }
    card(8f, 56f, Kind.Classes)
    drawRoundRect(p.text, Offset(15 * x, 16 * y), Size(28 * x, 8 * y), CornerRadius(2 * x))
    drawRoundRect(p.text.copy(alpha = 0.5f), Offset(15 * x, 29 * y), Size(46 * x, 5 * y), CornerRadius(2 * x))
    drawRoundRect(p.a, Offset(15 * x, 41 * y), Size(56 * x, 7 * y), CornerRadius(3 * x))
    card(68f, 94f, Kind.Exams)
    drawRoundRect(p.text, Offset(15 * x, 76 * y), Size(22 * x, 7 * y), CornerRadius(2 * x))
}

/** Soft Home in miniature: rounded white sheets, pastel cards, pill button. */
private fun DrawScope.drawSoft(p: Inks) {
    drawRect(p.paper)
    val x = size.width / 100f
    val y = size.height / 100f

    // Greeting header
    drawRoundRect(p.text.copy(alpha = 0.8f), Offset(8 * x, 8 * y), Size(42 * x, 8 * y), CornerRadius(4 * x))

    // Main soft card (white rounded sheet)
    val rSheet = CornerRadius(8 * x)
    drawRoundRect(p.surface, Offset(8 * x, 22 * y), Size(84 * x, 38 * y), rSheet)
    drawRoundRect(Kind.Classes.pastel, Offset(14 * x, 28 * y), Size(72 * x, 14 * y), CornerRadius(5 * x))
    drawRoundRect(p.a, Offset(14 * x, 46 * y), Size(28 * x, 8 * y), CornerRadius(4 * x))

    // Secondary soft card
    drawRoundRect(p.surface, Offset(8 * x, 65 * y), Size(84 * x, 22 * y), rSheet)
    drawRoundRect(Kind.Exams.pastel, Offset(14 * x, 71 * y), Size(50 * x, 10 * y), CornerRadius(4 * x))

    // Bottom pill navigation
    drawRoundRect(p.surface, Offset(12 * x, 90 * y), Size(76 * x, 8 * y), CornerRadius(4 * x))
}

/** Editorial Home in miniature: a headline, a firm rule, two flat tiles with big figures and one red block. */
private fun DrawScope.drawEditorial(p: Inks) {
    drawRect(p.paper)
    val x = size.width / 100f
    val y = size.height / 100f
    drawRect(p.text, Offset(8 * x, 8 * y), Size(62 * x, 10 * y))
    drawRect(p.text, Offset(8 * x, 21 * y), Size(34 * x, 10 * y))
    drawRect(p.text, Offset(8 * x, 37 * y), Size(84 * x, 1.6f * y))
    for (col in 0 until 2) {
        val at = Offset((8 + col * 43) * x, 44 * y)
        val tile = Size(41 * x, 30 * y)
        drawRect(p.surface, at, tile)
        drawRect(p.rule, at, tile, style = Stroke(0.8f * x))
        drawRect(p.text, at + Offset(5 * x, 14 * y), Size(16 * x, 11 * y))
    }
    drawRect(p.a, Offset(8 * x, 80 * y), Size(84 * x, 13 * y))
}

/** Material 3 Home in miniature: rounded tonal container cards, pill button and bottom pill indicator. */
private fun DrawScope.drawMaterial3(p: Inks) {
    drawRect(p.paper)
    val x = size.width / 100f
    val y = size.height / 100f

    // Top header pill
    drawRoundRect(p.surface, Offset(8 * x, 6 * y), Size(42 * x, 10 * y), CornerRadius(5 * x))

    // Rounded tonal card 1
    val rCard = CornerRadius(6 * x)
    drawRoundRect(p.surface, Offset(8 * x, 20 * y), Size(84 * x, 36 * y), rCard)
    drawRoundRect(p.rule, Offset(8 * x, 20 * y), Size(84 * x, 36 * y), rCard, style = Stroke(0.8f * x))
    drawRoundRect(p.a.copy(alpha = 0.2f), Offset(14 * x, 26 * y), Size(38 * x, 7 * y), CornerRadius(3 * x))
    drawRoundRect(p.text.copy(alpha = 0.5f), Offset(14 * x, 37 * y), Size(52 * x, 5 * y), CornerRadius(2 * x))
    drawRoundRect(p.a, Offset(14 * x, 46 * y), Size(32 * x, 6 * y), CornerRadius(3 * x))

    // Rounded tonal card 2
    drawRoundRect(p.surface, Offset(8 * x, 60 * y), Size(84 * x, 24 * y), rCard)
    drawRoundRect(p.rule, Offset(8 * x, 60 * y), Size(84 * x, 24 * y), rCard, style = Stroke(0.8f * x))
    drawRoundRect(p.text.copy(alpha = 0.4f), Offset(14 * x, 68 * y), Size(24 * x, 6 * y), CornerRadius(2 * x))

    // Bottom Navigation bar with pill indicator
    drawRect(p.surface, Offset(0f, 88 * y), Size(100 * x, 12 * y))
    drawRoundRect(p.a.copy(alpha = 0.22f), Offset(12 * x, 90 * y), Size(22 * x, 8 * y), CornerRadius(4 * x))
    drawRoundRect(p.text.copy(alpha = 0.3f), Offset(64 * x, 92 * y), Size(18 * x, 4 * y), CornerRadius(2 * x))
}

/** Glass Home in miniature: frosted glass cards over a soft grey sheet. */
private fun DrawScope.drawGlass(p: Inks) {
    drawRect(p.paper)
    val x = size.width / 100f
    val y = size.height / 100f

    // Glass card 1
    val rCard = CornerRadius(6 * x)
    val at1 = Offset(8 * x, 12 * y)
    val s1 = Size(84 * x, 36 * y)
    drawRoundRect(p.glassShadow, at1 + Offset(0f, 3 * y), s1, rCard)
    drawRoundRect(p.glassFill, at1, s1, rCard)
    drawRoundRect(p.glassEdge, at1, s1, rCard, style = Stroke(1.2f * x))
    drawRoundRect(p.text, Offset(16 * x, 20 * y), Size(38 * x, 7 * y), CornerRadius(3 * x))
    drawRoundRect(p.text.copy(alpha = 0.5f), Offset(16 * x, 32 * y), Size(54 * x, 5 * y), CornerRadius(2 * x))

    // Glass card 2
    val at2 = Offset(8 * x, 54 * y)
    val s2 = Size(84 * x, 32 * y)
    drawRoundRect(p.glassShadow, at2 + Offset(0f, 3 * y), s2, rCard)
    drawRoundRect(p.glassFill, at2, s2, rCard)
    drawRoundRect(p.glassEdge, at2, s2, rCard, style = Stroke(1.2f * x))
    drawRoundRect(p.text, Offset(16 * x, 62 * y), Size(30 * x, 6 * y), CornerRadius(3 * x))
}
