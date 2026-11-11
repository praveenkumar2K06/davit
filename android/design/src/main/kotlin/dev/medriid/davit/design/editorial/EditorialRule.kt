package dev.medriid.davit.design.editorial

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
internal fun EditorialRule(modifier: Modifier = Modifier, inks: Inks) {
    Spacer(modifier.fillMaxWidth().height(2.dp).background(inks.text))
}

@Preview(showBackground = true)
@Composable
private fun EditorialRulePreview() {
    val inks = InkPairs.Editorial
    RisoTheme(inks = inks) {
        Box(modifier = Modifier.padding(16.dp)) {
            EditorialRule(inks = inks)
        }
    }
}

