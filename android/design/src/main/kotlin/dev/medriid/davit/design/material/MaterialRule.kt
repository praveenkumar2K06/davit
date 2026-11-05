package dev.medriid.davit.design.material

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.Inks

@Composable
internal fun MaterialRule(modifier: Modifier = Modifier, inks: Inks) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = inks.rule,
    )
}
