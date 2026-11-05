package dev.medriid.davit.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.medriid.davit.design.classic.ClassicButton
import dev.medriid.davit.design.editorial.EditorialButton
import dev.medriid.davit.design.glass.GlassButton
import dev.medriid.davit.design.material.MaterialButton
import dev.medriid.davit.design.soft.SoftButton

/**
 * Primary button: styled according to the active theme style.
 * At least 54 dp tall: buttons are for the actions that matter, so they're easy to hit.
 */
@Composable
fun InkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    quiet: Boolean = false,
    fillWidth: Boolean = false,
) {
    val inks = LocalInks.current
    when (inks.style) {
        AppStyle.Material3 -> MaterialButton(text, onClick, modifier, enabled, quiet, fillWidth, inks)
        AppStyle.Editorial -> EditorialButton(text, onClick, modifier, enabled, quiet, fillWidth, inks)
        AppStyle.Soft -> SoftButton(text, onClick, modifier, enabled, quiet, fillWidth, inks)
        AppStyle.Glass -> GlassButton(text, onClick, modifier, enabled, quiet, fillWidth, inks)
        AppStyle.Classic -> ClassicButton(text, onClick, modifier, enabled, quiet, fillWidth, inks)
    }
}
