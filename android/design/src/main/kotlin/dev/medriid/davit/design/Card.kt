package dev.medriid.davit.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.classic.ClassicCard
import dev.medriid.davit.design.editorial.EditorialCard
import dev.medriid.davit.design.glass.GlassCard
import dev.medriid.davit.design.material.MaterialCard
import dev.medriid.davit.design.soft.SoftCard

val CardShape = RoundedCornerShape(10.dp)
val GlassShape = RoundedCornerShape(18.dp)
val SoftShape = RoundedCornerShape(24.dp)
val EditorialShape = RoundedCornerShape(3.dp)
val MaterialShape = RoundedCornerShape(16.dp)

/**
 * The workhorse surface: styled according to the active theme.
 */
@Composable
fun InkCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    kind: Kind? = null,
    fill: Color? = null,
    plate: Color? = null,
    plateOffset: Dp = 5.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val inks = LocalInks.current
    when (inks.style) {
        AppStyle.Material3 -> MaterialCard(modifier, onClick, contentPadding, content)
        AppStyle.Editorial -> EditorialCard(modifier, onClick, fill, kind, plate, contentPadding, inks, content)
        AppStyle.Soft -> SoftCard(modifier, onClick, fill, kind, plate, contentPadding, inks, content)
        AppStyle.Glass -> GlassCard(modifier, onClick, kind, fill, plate, plateOffset, contentPadding, inks, content)
        AppStyle.Classic -> ClassicCard(modifier, onClick, kind, fill, plate, plateOffset, contentPadding, inks, content)
    }
}

@Preview(showBackground = true)
@Composable
private fun InkCardPreview() {
    RisoTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InkCard {
                BasicText("Static InkCard")
            }
            InkCard(onClick = {}, kind = Kind.Classes) {
                BasicText("Clickable InkCard - Classes Kind")
            }
        }
    }
}

