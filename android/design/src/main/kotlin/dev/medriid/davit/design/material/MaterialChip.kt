package dev.medriid.davit.design.material

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.RisoType

@Composable
internal fun MaterialChip(
    text: String,
    modifier: Modifier = Modifier,
    solid: Boolean = false,
    onClick: (() -> Unit)? = null,
    inks: Inks,
) {
    val shape = RoundedCornerShape(8.dp)
    val containerColor = if (solid) inks.a else Color.Transparent
    val labelColor = if (solid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (solid) inks.a else inks.rule

    if (onClick != null) {
        SuggestionChip(
            onClick = onClick,
            label = {
                Text(
                    text,
                    maxLines = 1,
                    softWrap = false,
                )
            },
            modifier = modifier.defaultMinSize(minHeight = 32.dp),
            shape = shape,
        )
    } else {
        Box(
            modifier
                .defaultMinSize(minHeight = 32.dp)
                .clip(shape)
                .background(containerColor)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text,
                maxLines = 1,
                softWrap = false,
                style = RisoType.label.copy(
                    color = labelColor,
                    letterSpacing = RisoType.label.letterSpacing * 0.5f,
                    fontSize = 12.sp,
                ),
            )
        }
    }
}
