package dev.medriid.davit.design.editorial

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.NavItem
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.design.RisoTheme
import dev.medriid.davit.design.RisoType

/**
 * Editorial tabs: a firm rule along the top with a red bar that slides to the open tab;
 * the open tab's name in bold, the rest quiet. No pills, nothing that isn't needed.
 */
@Composable
internal fun EditorialNavBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, inks: Inks) {
    val slide by animateFloatAsState(
        selected.toFloat(),
        spring(dampingRatio = 0.8f, stiffness = 420f),
        label = "tab",
    )
    Column(modifier.fillMaxWidth().background(inks.paper)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .drawBehind {
                    drawRect(inks.text, size = Size(size.width, 1.dp.toPx()))
                    val w = size.width / items.size
                    drawRect(inks.a, topLeft = Offset(w * slide + w * 0.22f, 0f), size = Size(w * 0.56f, size.height))
                },
        )
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            items.forEachIndexed { i, item ->
                val on = i == selected
                val tint by animateColorAsState(if (on) inks.text else inks.muted, label = "tabInk")
                Column(
                    Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clickable(role = Role.Tab, onClick = { onSelect(i) })
                        .semantics { this.selected = on },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    IconGlyph(item.icon, tint, Modifier.size(22.dp))
                    Spacer(Modifier.height(5.dp))
                    BasicText(
                        item.label.uppercase(),
                        maxLines = 1,
                        style = RisoType.label.copy(fontSize = 9.5.sp, letterSpacing = 0.08.em, color = tint, fontWeight = if (on) FontWeight.Black else FontWeight.Medium),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditorialNavBarPreview() {
    val inks = InkPairs.Editorial
    RisoTheme(inks = inks) {
        val items = listOf(
            NavItem("Home", RisoIcon.Home),
            NavItem("Today", RisoIcon.Today),
            NavItem("Academics", RisoIcon.Academics),
            NavItem("Profile", RisoIcon.Profile),
        )
        EditorialNavBar(items = items, selected = 0, onSelect = {}, inks = inks)
    }
}

