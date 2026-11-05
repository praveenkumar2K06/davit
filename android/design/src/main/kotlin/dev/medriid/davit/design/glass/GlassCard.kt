package dev.medriid.davit.design.glass

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.Kind
import dev.medriid.davit.design.RisoMotion
import dev.medriid.davit.design.rememberMotionEnabled

internal val GlassShape = RoundedCornerShape(18.dp)

@Composable
internal fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    kind: Kind? = null,
    fill: Color? = null,
    plate: Color? = null,
    plateOffset: Dp = 5.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    inks: Inks,
    content: @Composable ColumnScope.() -> Unit,
) {
    val plateColor = inks.glassShadow
    val fillColor = fill ?: inks.fill(kind)
    val motion = rememberMotionEnabled()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val push by animateDpAsState(
        if (pressed) plateOffset * 0.7f else 0.dp,
        if (motion) RisoMotion.quick() else snap(),
        label = "push",
    )

    Box(modifier.padding(end = plateOffset, bottom = plateOffset), propagateMinConstraints = true) {
        Box(
            Modifier
                .matchParentSize()
                .offset(plateOffset, plateOffset)
                .clip(GlassShape)
                .background(plateColor),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .offset { IntOffset(push.roundToPx(), push.roundToPx()) }
                .clip(GlassShape)
                .background(fillColor)
                .border(
                    if (plate == null) 1.dp else 2.dp,
                    if (plate != null) inks.a else inks.glassEdge,
                    GlassShape,
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable(interaction, indication = null, role = Role.Button, onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(contentPadding),
            content = content,
        )
    }
}
