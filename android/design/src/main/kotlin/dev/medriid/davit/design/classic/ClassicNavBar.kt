package dev.medriid.davit.design.classic

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.NavItem
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.design.Rule

@Composable
internal fun ClassicNavBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, inks: Inks) {
    val crowded = items.size > 4
    val pillWidth = if (crowded) 54.dp else 64.dp
    Column(modifier.fillMaxWidth().background(if (inks.soft) inks.surface else inks.paper)) {
        Rule()
        Row(
            Modifier.fillMaxWidth().padding(horizontal = if (crowded) 2.dp else 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEachIndexed { i, item ->
                val on = i == selected
                val tint by animateColorAsState(if (on && !inks.sticker) inks.paper else inks.text, label = "icon")
                Column(
                    Modifier
                        .weight(1f)
                        .height(72.dp)
                        .clickable(role = Role.Tab, onClick = { onSelect(i) })
                        .semantics { this.selected = on },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val pill = RoundedCornerShape(50)
                    Box(contentAlignment = Alignment.Center) {
                        if (on) {
                            Box(
                                Modifier.size(pillWidth, 34.dp).offset(3.dp, 3.dp).clip(pill).background(if (inks.soft) androidx.compose.ui.graphics.Color.Transparent else if (inks.glass) inks.glassShadow else if (inks.sticker) inks.text else inks.a),
                            )
                            Box(
                                Modifier.size(pillWidth, 34.dp).clip(pill).background(if (inks.soft) inks.text else if (inks.sticker || inks.glass) inks.a else inks.b)
                                    .then(if (inks.sticker) Modifier.border(1.5.dp, inks.text, pill) else Modifier),
                            )
                        } else {
                            Spacer(Modifier.size(pillWidth, 34.dp))
                        }
                        IconGlyph(item.icon, tint, Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    BasicText(
                        item.label,
                        maxLines = 1,
                        style = RisoType.body.copy(
                            fontSize = if (crowded) 11.sp else 12.5.sp,
                            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                            color = inks.text,
                            textAlign = TextAlign.Center,
                            letterSpacing = if (crowded) (-0.3).sp else (-0.2).sp,
                        ),
                    )
                }
            }
        }
    }
}
