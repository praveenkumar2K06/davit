package dev.medriid.davit.design

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.medriid.davit.design.classic.ClassicChip
import dev.medriid.davit.design.editorial.EditorialChip
import dev.medriid.davit.design.material.MaterialChip
import dev.medriid.davit.design.soft.SoftChip

/** Small bordered tag: room, slot, "6 days left". [solid] reverses it out of ink B. */
@Composable
fun InkChip(
    text: String,
    modifier: Modifier = Modifier,
    solid: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val inks = LocalInks.current
    when (inks.style) {
        AppStyle.Material3 -> MaterialChip(text, modifier, solid, onClick, inks)
        AppStyle.Editorial -> EditorialChip(text, modifier, solid, onClick, inks)
        AppStyle.Soft -> SoftChip(text, modifier, solid, onClick, inks)
        AppStyle.Glass, AppStyle.Classic -> ClassicChip(text, modifier, solid, onClick, inks)
    }
}

/** Chips where the chosen one is solid; wraps onto more lines when it runs out of room. */
@Composable
fun ChoiceRow(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val inks = LocalInks.current

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { i, text ->
            if (inks.material) {
                val isSelected = i == selected
                val labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(i) },
                    label = {
                        BasicText(
                            text,
                            maxLines = 1,
                            softWrap = false,
                            style = RisoType.label.copy(
                                color = labelColor,
                                fontSize = 12.sp,
                            ),
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = inks.a,
                        selectedLabelColor = labelColor,
                        containerColor = Color.Transparent,
                        labelColor = labelColor,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = inks.rule,
                        selectedBorderColor = inks.a,
                        borderWidth = 1.dp,
                    ),
                )
            } else {
                Box(
                    Modifier
                        .clickable(role = Role.RadioButton) { onSelect(i) }
                        .semantics { this.selected = i == selected }
                        .padding(vertical = 5.dp),
                ) { InkChip(text, solid = i == selected) }
            }
        }
    }
}

