package dev.medriid.davit.design.editorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.Inks

@Composable
internal fun EditorialRule(modifier: Modifier = Modifier, inks: Inks) {
    Spacer(modifier.fillMaxWidth().height(2.dp).background(inks.text))
}
