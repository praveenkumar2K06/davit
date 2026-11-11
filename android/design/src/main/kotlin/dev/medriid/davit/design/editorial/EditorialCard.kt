package dev.medriid.davit.design.editorial

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.Kind
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoMotion
import dev.medriid.davit.design.RisoTheme

internal val EditorialShape = RoundedCornerShape(3.dp)

@Composable
internal fun EditorialCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    fill: Color? = null,
    kind: Kind? = null,
    plate: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    inks: Inks,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink by animateFloatAsState(if (pressed) 0.975f else 1f, RisoMotion.quick(), label = "sink")
    Column(
        modifier
            .graphicsLayer { scaleX = sink; scaleY = sink }
            .clip(EditorialShape)
            .background(fill ?: inks.fill(kind))
            .border(1.dp, inks.rule, EditorialShape)
            .then(
                if (plate != null) Modifier.drawBehind { drawRect(inks.a, size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)) }
                else Modifier,
            )
            .then(if (onClick != null) Modifier.clickable(interaction, indication = null, role = Role.Button, onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun EditorialCardPreview() {
    val theme = InkPairs.Editorial
    RisoTheme(inks = theme) {
        val inks = LocalInks.current
        Column(
            modifier = Modifier.background(color = inks.surface).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EditorialCard(inks = inks) {
                BasicText("Static Editorial Card")
            }
            EditorialCard(inks = inks, fill = inks.a) {
                BasicText("Static Editorial Card")
            }
            EditorialCard(onClick = {}, kind = Kind.Classes, inks = inks) {
                BasicText("Clickable Editorial Card")
            }
        }
    }
}

