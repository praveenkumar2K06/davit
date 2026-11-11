package dev.medriid.davit.design.classic

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoTheme
import dev.medriid.davit.design.RisoType

@Composable
internal fun ClassicChip(
    text: String,
    modifier: Modifier = Modifier,
    solid: Boolean = false,
    onClick: (() -> Unit)? = null,
    inks: Inks,
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier
            .defaultMinSize(minHeight = 28.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .clip(shape)
            .background(if (solid) (if (inks.sticker) inks.a else inks.b) else Color.Transparent)
            .border(1.5.dp, if (solid && !inks.sticker) inks.b else inks.text, shape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text,
            maxLines = 1,
            softWrap = false,
            style = RisoType.label.copy(
                color = if (solid && !inks.sticker) inks.paper else inks.text,
                letterSpacing = RisoType.label.letterSpacing * 0.5f,
            ),
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ClassicChipPreview() {
    RisoTheme {
        val inks = LocalInks.current
        Column(
            modifier = Modifier.background(color = inks.surface).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ClassicChip(text = "Outline Classic Chip", inks = inks)
            ClassicChip(text = "Solid Classic Chip", solid = true, inks = inks)
        }
    }
}

