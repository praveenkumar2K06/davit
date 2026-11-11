package dev.medriid.davit.design.material

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.RisoTheme
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
    val containerColor = if (solid) inks.a else SuggestionChipDefaults.suggestionChipColors().containerColor
    val labelColor = if (solid) MaterialTheme.colorScheme.onPrimary else SuggestionChipDefaults.suggestionChipColors().labelColor
    val borderColor = if (solid) inks.a else SuggestionChipDefaults.suggestionChipColors().labelColor

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

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MaterialChipPreview() {
    val inks = InkPairs.Material3
    RisoTheme(inks = inks) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.background(color = MaterialTheme.colorScheme.surface).padding(16.dp),
        ) {
            MaterialChip(text = "Outline Chip", inks = inks)
            MaterialChip(text = "Solid Chip", solid = true, inks = inks)
            MaterialChip(text = "Clickable Chip", onClick = {}, inks = inks)
        }
    }
}

