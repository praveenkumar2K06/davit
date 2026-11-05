package dev.medriid.davit.design.editorial

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.RisoMotion
import dev.medriid.davit.design.RisoType

@Composable
internal fun EditorialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    quiet: Boolean = false,
    fillWidth: Boolean = false,
    inks: Inks,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val nudge by animateDpAsState(if (pressed) 6.dp else 0.dp, RisoMotion.quick(), label = "nudge")
    val ink = if (quiet) inks.text else inks.paper
    Row(
        modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .clip(EditorialShape)
            .background(if (!enabled) inks.rule else if (quiet) Color.Transparent else inks.text)
            .then(if (quiet) Modifier.border(1.5.dp, inks.text, EditorialShape) else Modifier)
            .clickable(interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 54.dp)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(text.uppercase(), Modifier.weight(1f, fill = fillWidth), style = RisoType.button.copy(color = ink))
        Spacer(Modifier.width(14.dp))
        BasicText("→", Modifier.offset(x = nudge), style = RisoType.button.copy(color = ink, fontSize = 18.sp))
    }
}
