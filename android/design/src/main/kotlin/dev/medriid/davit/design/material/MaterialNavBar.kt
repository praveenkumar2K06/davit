package dev.medriid.davit.design.material

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.NavItem
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.design.RisoTheme

/** Material 3 tabs: official NavigationBar and NavigationBarItem. */
@Composable
internal fun MaterialNavBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
    ) {
        items.forEachIndexed { i, item ->
            val on = i == selected
            NavigationBarItem(
                selected = on,
                onClick = { onSelect(i) },
                icon = { IconGlyph(item.icon) },
                label = {
                    Text(
                        item.label,
                        maxLines = 1
                    )
                },

            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MaterialNavBarPreview() {
    val inks = InkPairs.Material3
    RisoTheme(inks = inks) {
        val items = listOf(
            NavItem("Home", RisoIcon.Home),
            NavItem("Today", RisoIcon.Today),
            NavItem("Academics", RisoIcon.Academics),
            NavItem("Profile", RisoIcon.Profile),
        )
        MaterialNavBar(items = items, selected = 0, onSelect = {}, modifier = Modifier)
    }
}

