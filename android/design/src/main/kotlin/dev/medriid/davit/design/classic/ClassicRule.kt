package dev.medriid.davit.design.classic

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.Inks

@Composable
internal fun ClassicRule(modifier: Modifier = Modifier, inks: Inks) {
    val ink = inks.b
    val blend = inks.blend
    Spacer(
        modifier
            .fillMaxWidth()
            .height(6.dp)
            .drawBehind {
                val thick = 2.dp.toPx()
                val thin = 1.dp.toPx()
                drawRect(ink, size = Size(size.width, thick), blendMode = blend)
                drawRect(
                    ink,
                    topLeft = Offset(0f, size.height - thin),
                    size = Size(size.width, thin),
                    blendMode = blend,
                )
            },
    )
}
