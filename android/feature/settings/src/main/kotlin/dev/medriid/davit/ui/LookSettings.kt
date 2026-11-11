@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.medriid.davit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.design.AppStyle
import dev.medriid.davit.design.ChoiceRow
import dev.medriid.davit.design.InkChip
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoType

import dev.medriid.davit.ui.material3.MaterialLookSettings

@Composable
internal fun LookSettings() {
    if (LocalInks.current.material) {
        MaterialLookSettings()
        return
    }
    val repo = LocalRepo.current
    val nav = LocalNav.current
    val state by repo.state.collectAsState()
    val activeStyle = LocalInks.current.style
    val isClassic = activeStyle == AppStyle.Classic
    SettingsCard("Style") {
        StylePicker(style = activeStyle) { repo.setStyle(it) }
    }
    SettingsCard("Light or dark") {
        ChoiceRow(listOf("System", "Light", "Dark"), state.theme) { repo.setTheme(it) }
    }
    // Colours belong to Classic; Editorial and Material 3 keep their own palettes.
    if (isClassic) SettingsCard("Colours") {
        val palette = state.palette
        var replacing by remember { mutableStateOf<Int?>(null) }
        InkText(
            if (palette.custom) "Your own, from " + InkPairs.all[palette.base.coerceIn(0, InkPairs.all.lastIndex)].name
            else "Ink pair · " + InkPairs.all[palette.base.coerceIn(0, InkPairs.all.lastIndex)].name,
            style = RisoType.body.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InkPairs.classic.forEach { i -> InkSwatch(InkPairs.of(i, LocalInks.current.dark), selected = !palette.custom && i == palette.base) { if (palette.custom) replacing = i else repo.setInk(i) } }
        }
        replacing?.let { i ->
            Spacer(Modifier.height(8.dp))
            InkText("Start over from " + InkPairs.all[i].name + "? Your own colours go.", style = RisoType.small)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.clickable(role = Role.Button) { repo.setInk(i); replacing = null }) { InkChip("Start over", solid = true) }
                Box(Modifier.clickable(role = Role.Button) { replacing = null }) { InkChip("Keep mine") }
            }
        }
        Spacer(Modifier.height(6.dp))
        SettingRow("Your own colours", "Any colour for every ink, and for each kind of card.", "Choose") { nav.go(Route.Colours) }
    }
}
