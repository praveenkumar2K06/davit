@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.medriid.davit.ui.material3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
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
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.design.material.MaterialButton
import dev.medriid.davit.ui.InkSwatch
import dev.medriid.davit.ui.LocalNav
import dev.medriid.davit.ui.Route
import dev.medriid.davit.ui.StylePicker

@Composable
fun MaterialLookSettings() {
    val repo = LocalRepo.current
    val nav = LocalNav.current
    val state by repo.state.collectAsState()
    val activeStyle = LocalInks.current.style
    val isClassic = activeStyle == AppStyle.Classic

    MaterialSettingsSectionHeader("Style", icon = RisoIcon.Eye)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            StylePicker(style = activeStyle) { repo.setStyle(it) }
        }
    }

    MaterialSettingsSectionHeader("Theme")
    val themes = listOf(
        "System" to "Follows phone settings",
        "Light" to "Bright and clear",
        "Dark" to "Easy on the eyes at night",
    )
    themes.forEachIndexed { index, (title, subtitle) ->
        val selected = state.theme == index
        MaterialSegmentedRow(
            title = title,
            body = subtitle,
            index = index,
            count = themes.size,
            leadingContent = {
                RadioButton(
                    selected = selected,
                    onClick = null,
                )
            },
            onClick = { repo.setTheme(index) },
        )
    }

    if (isClassic) {
        MaterialSettingsSectionHeader("Colours", icon = RisoIcon.Pencil)
        val palette = state.palette
        var replacing by remember { mutableStateOf<Int?>(null) }
        val currentPairName = InkPairs.all[palette.base.coerceIn(0, InkPairs.all.lastIndex)].name

        MaterialSegmentedActionRow(
            title = "Your own colours",
            body = "Any colour for every ink, and for each kind of card.",
            action = "Choose",
            index = 0,
            count = 2,
            onClick = { nav.go(Route.Colours) },
        )

        MaterialSegmentedRow(
            title = if (palette.custom) "Your own, from $currentPairName" else "Ink pair · $currentPairName",
            index = 1,
            count = 2,
        )

        Spacer(Modifier.height(8.dp))
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InkPairs.classic.forEach { i ->
                        InkSwatch(
                            pair = InkPairs.of(i, LocalInks.current.dark),
                            selected = !palette.custom && i == palette.base,
                        ) {
                            if (palette.custom) replacing = i else repo.setInk(i)
                        }
                    }
                }
                replacing?.let { i ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Start over from ${InkPairs.all[i].name}? Your own colours will be reset.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MaterialButton("Start over", onClick = { repo.setInk(i); replacing = null })
                        MaterialButton("Keep mine", onClick = { replacing = null }, quiet = true)
                    }
                }
            }
        }
    }
}
