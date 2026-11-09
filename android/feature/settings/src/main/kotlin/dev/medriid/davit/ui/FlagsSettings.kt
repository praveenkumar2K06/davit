package dev.medriid.davit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.medriid.davit.data.LocalRepo

@Composable
internal fun FlagsSettings() {
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()
    SettingsCard("Attendance") {
        ToggleRow(
            "Calculate before exam",
            "Calculate remaining classes until the start of the next exam period instead of the end of the semester.",
            state.calcBeforeExam,
        ) { repo.setCalcBeforeExam(!state.calcBeforeExam) }
    }
}
