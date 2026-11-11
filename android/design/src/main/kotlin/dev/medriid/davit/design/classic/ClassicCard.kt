package dev.medriid.davit.design.classic

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.Kind
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoMotion
import dev.medriid.davit.design.RisoTheme
import dev.medriid.davit.design.rememberMotionEnabled

internal val ClassicCardShape = RoundedCornerShape(10.dp)

@Composable
internal fun ClassicCard(
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
    val plateColor = plate ?: inks.plate(kind)
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
                .clip(ClassicCardShape)
                .background(plateColor),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .offset { IntOffset(push.roundToPx(), push.roundToPx()) }
                .clip(ClassicCardShape)
                .background(fillColor)
                .border(2.dp, inks.text, ClassicCardShape)
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

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ClassicCardPreview() {
    RisoTheme {
        val inks = LocalInks.current
        Column(
            modifier = Modifier.background(color = inks.surface).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ClassicCard(inks = inks) {
                BasicText("Static Classic Card")
            }
            ClassicCard(onClick = {}, kind = Kind.Classes, inks = inks) {
                BasicText("Clickable Classic Card")
            }
        }
    }
}

