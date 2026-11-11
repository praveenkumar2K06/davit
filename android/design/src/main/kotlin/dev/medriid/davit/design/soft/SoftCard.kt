package dev.medriid.davit.design.soft

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.Kind
import dev.medriid.davit.design.RisoTheme

internal val SoftShape = RoundedCornerShape(24.dp)

@Composable
internal fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    fill: Color? = null,
    kind: Kind? = null,
    plate: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    inks: Inks,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(SoftShape)
            .background(fill ?: inks.fill(kind))
            .then(if (plate != null) Modifier.border(2.dp, inks.a, SoftShape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(contentPadding)
            .padding(2.dp),
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
private fun SoftCardPreview() {
    val inks = InkPairs.Soft
    RisoTheme(inks = inks) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SoftCard(inks = inks) {
                BasicText("Static Soft Card")
            }
            SoftCard(onClick = {}, kind = Kind.Classes, inks = inks) {
                BasicText("Clickable Soft Card")
            }
        }
    }
}

