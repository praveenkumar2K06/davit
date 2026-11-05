package dev.medriid.davit.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.medriid.davit.design.classic.ClassicNavBar
import dev.medriid.davit.design.editorial.EditorialNavBar
import dev.medriid.davit.design.material.MaterialNavBar

/**
 * Bottom tabs. Each tab is a full-height 72 dp target; the selected one sits in a solid
 * ink-B pill with the ink-A plate behind it, the same build as [InkButton].
 * Labels never truncate: they're short, and sized to fit five across at 360 dp.
 */
@Composable
fun RisoNavBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val inks = LocalInks.current
    when (inks.style) {
        AppStyle.Material3 -> MaterialNavBar(items, selected, onSelect, modifier)
        AppStyle.Editorial -> EditorialNavBar(items, selected, onSelect, modifier, inks)
        AppStyle.Soft, AppStyle.Glass, AppStyle.Classic -> ClassicNavBar(items, selected, onSelect, modifier, inks)
    }
}
