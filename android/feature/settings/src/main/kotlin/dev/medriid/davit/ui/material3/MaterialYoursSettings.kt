package dev.medriid.davit.ui.material3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.ui.LocalNav
import dev.medriid.davit.ui.Mascot
import dev.medriid.davit.ui.MascotEditor
import dev.medriid.davit.ui.Route

@Composable
fun MaterialYoursSettings() {
    val repo = LocalRepo.current
    val nav = LocalNav.current
    val state by repo.state.collectAsState()
    val inks = LocalInks.current

    MaterialSettingsSectionHeader("On Home", icon = RisoIcon.Home)
    MaterialSegmentedActionRow(
        title = "Display card",
        body = if (inks.editorial) "The picture at the top of Home: a print or your own photo."
        else if (inks.material) "The banner at the top of Home: a print or your own photo, with stats you pick."
        else "The top bar on a print or your own photo, with two stats you pick.",
        action = "Change",
        index = 0,
        count = 2,
        leadingContent = { IconGlyph(RisoIcon.Picture, MaterialTheme.colorScheme.primary, Modifier.size(20.dp)) },
        onClick = { nav.go(Route.DisplayCard) },
    )
    MaterialSegmentedActionRow(
        title = "Course names",
        body = if (state.aliases.isEmpty()) "Your own short names for courses, shown everywhere." else "${state.aliases.size} renamed, shown everywhere.",
        action = "Rename",
        index = 1,
        count = 2,
        leadingContent = { IconGlyph(RisoIcon.Tag, MaterialTheme.colorScheme.primary, Modifier.size(20.dp)) },
        onClick = { nav.go(Route.CourseNames) },
    )

    MaterialSettingsSectionHeader("Your mascot", icon = RisoIcon.Profile)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "The face in the top bar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            MascotEditor(Mascot.parse(state.mascot)) { repo.setMascot(it.encode()) }
        }
    }
}
