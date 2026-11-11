package dev.medriid.davit.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Section divider (`.rule`): a 2 dp line over a 1 dp line, 6 dp apart overall. */
@Composable
fun Rule(modifier: Modifier = Modifier) {
    val inks = LocalInks.current
    when (inks.style) {
        AppStyle.Editorial -> {
            // Editorial: one firm rule in the text colour, as a newspaper sets its sections.
            Spacer(modifier.fillMaxWidth().height(2.dp).background(inks.text))
        }
        AppStyle.Material3 -> {
            // Material 3: official HorizontalDivider component.
            HorizontalDivider()
        }
        AppStyle.Soft, AppStyle.Glass -> {
            // Soft / Glass: one hairline.
            Spacer(modifier.fillMaxWidth().height(1.dp).background(inks.rule))
        }
        AppStyle.Classic -> {
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
    }
}

@Preview(showBackground = true)
@Composable
private fun RulePreview() {
    RisoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            Rule()
        }
    }
}

