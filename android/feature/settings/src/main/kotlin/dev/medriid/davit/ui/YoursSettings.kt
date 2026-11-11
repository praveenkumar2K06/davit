package dev.medriid.davit.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoType

import dev.medriid.davit.ui.material3.MaterialYoursSettings

@Composable
internal fun YoursSettings() {
    if (LocalInks.current.material) {
        MaterialYoursSettings()
        return
    }
    val repo = LocalRepo.current
    val nav = LocalNav.current
    val state by repo.state.collectAsState()
    val inks = LocalInks.current
    SettingsCard("On Home") {
        SettingRow(
            "Display card",
            if (inks.editorial) "The picture at the top of Home: a print or your own photo."
            else if (inks.material) "The banner at the top of Home: a print or your own photo, with stats you pick."
            else "The top bar on a print or your own photo, with two stats you pick.",
            "Change",
        ) { nav.go(Route.DisplayCard) }
        SettingRow(
            "Course names",
            if (state.aliases.isEmpty()) "Your own short names for courses, shown everywhere." else "${state.aliases.size} renamed, shown everywhere.",
            "Rename",
        ) { nav.go(Route.CourseNames) }
    }
    SettingsCard("Your mascot") {
        InkText("The face in the top bar.", style = RisoType.small)
        Spacer(Modifier.height(8.dp))
        MascotEditor(Mascot.parse(state.mascot)) { repo.setMascot(it.encode()) }
    }
}
