package dev.medriid.davit.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
