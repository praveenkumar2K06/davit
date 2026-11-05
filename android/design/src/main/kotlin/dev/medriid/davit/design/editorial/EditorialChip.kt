package dev.medriid.davit.design.editorial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.RisoType

@Composable
internal fun EditorialChip(
    text: String,
    modifier: Modifier = Modifier,
    solid: Boolean = false,
    onClick: (() -> Unit)? = null,
    inks: Inks,
) {
    Box(
        modifier
            .defaultMinSize(minHeight = 28.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .clip(EditorialShape)
            .background(if (solid) inks.text else Color.Transparent)
            .border(1.dp, if (solid) inks.text else inks.text.copy(alpha = 0.3f), EditorialShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text,
            maxLines = 1,
            softWrap = false,
            style = RisoType.label.copy(
                color = inks.paper,
                letterSpacing = RisoType.label.letterSpacing * 0.5f,
            ),
        )
    }
}
