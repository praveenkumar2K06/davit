package dev.medriid.davit.design.soft

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.RisoTheme

@Composable
internal fun SoftRule(modifier: Modifier = Modifier, inks: Inks) {
    Spacer(modifier.fillMaxWidth().height(1.dp).background(inks.rule))
}

@Preview(showBackground = true)
@Composable
private fun SoftRulePreview() {
    val inks = InkPairs.Soft
    RisoTheme(inks = inks) {
        Box(modifier = Modifier.padding(16.dp)) {
            SoftRule(inks = inks)
        }
    }
}

