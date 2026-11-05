package dev.medriid.davit.design.material

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.NavItem

/** Material 3 tabs: official NavigationBar and NavigationBarItem. */
@Composable
internal fun MaterialNavBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
    ) {
        items.forEachIndexed { i, item ->
            val on = i == selected
            val selectedColor = if (on) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            val labelColor = if (on) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
            NavigationBarItem(
                selected = on,
                onClick = { onSelect(i) },
                icon = { IconGlyph(item.icon, color = selectedColor) },
                label = {
                    Text(
                        item.label,
                        maxLines = 1,
                        color = labelColor
                    )
                },
            )
        }
    }
}
