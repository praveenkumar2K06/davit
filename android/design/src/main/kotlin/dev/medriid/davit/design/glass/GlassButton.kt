package dev.medriid.davit.design.glass

import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.RisoMotion
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.design.rememberMotionEnabled

@Composable
internal fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    quiet: Boolean = false,
    fillWidth: Boolean = false,
    inks: Inks,
) {
    val motion = rememberMotionEnabled()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val plateTarget = if (pressed) DpOffset((-3).dp, 5.dp) else DpOffset(4.dp, 3.dp)
    val blockTarget = if (pressed) DpOffset(1.dp, (-1).dp) else DpOffset(0.dp, 0.dp)
    val plate by animateValueAsState(
        plateTarget, DpOffset.VectorConverter,
        if (motion) RisoMotion.quick() else snap(), label = "plate",
    )
    val block by animateValueAsState(
        blockTarget, DpOffset.VectorConverter,
        if (motion) RisoMotion.quick() else snap(), label = "block",
    )

    val blockColor = if (quiet) inks.glassFill else inks.a
    val textColor = inks.text
    val plateColor = inks.glassShadow
    val shape = GlassShape

    Box(
        modifier
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .offset { IntOffset(plate.x.roundToPx(), plate.y.roundToPx()) }
                .clip(shape)
                .background(if (enabled) plateColor else inks.rule),
        )
        Box(
            Modifier
                .offset { IntOffset(block.x.roundToPx(), block.y.roundToPx()) }
                .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .clip(shape)
                .background(if (enabled) blockColor else inks.paper)
                .then(if (quiet || !enabled) Modifier.border(1.dp, inks.glassEdge, shape) else Modifier)
                .defaultMinSize(minHeight = 54.dp)
                .padding(horizontal = 22.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text.uppercase(),
                style = RisoType.button.copy(color = if (enabled) textColor else inks.rule),
            )
        }
    }
}
