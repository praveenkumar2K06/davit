package dev.medriid.davit.design.soft

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.sp
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.RisoTheme
import dev.medriid.davit.design.RisoType

@Composable
internal fun SoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    quiet: Boolean = false,
    fillWidth: Boolean = false,
    inks: Inks,
) {
    val pill = RoundedCornerShape(50)
    Box(
        modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .clip(pill)
            .background(if (!enabled) inks.rule else if (quiet) Color.Transparent else inks.a)
            .then(if (quiet) Modifier.border(1.dp, inks.text.copy(alpha = 0.35f), pill) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 54.dp)
            .padding(horizontal = 24.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(text, style = RisoType.button.copy(color = if (quiet) inks.text else Color(0xFF1A1A1A), fontSize = 16.sp))
    }
}

@Preview(showBackground = true)
@Composable
private fun SoftButtonPreview() {
    val inks = InkPairs.Soft
    RisoTheme(inks = inks) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SoftButton("Soft Button", onClick = {}, inks = inks)
            SoftButton("Quiet Soft Button", onClick = {}, quiet = true, inks = inks)
            SoftButton("Disabled Soft Button", onClick = {}, enabled = false, inks = inks)
        }
    }
}

