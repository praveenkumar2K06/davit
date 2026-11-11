package dev.medriid.davit.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.LocalRepo

@Composable
internal fun FlagsSettings() {
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()
    SettingsCard("Attendance & Schedule") {
        ToggleRow(
            "Follow academic calendar",
            "Stop showing timetable classes and widget warnings after the semester's last instructional day.",
            state.followCalendar,
        ) { repo.setFollowCalendar(!state.followCalendar) }
        Spacer(Modifier.height(10.dp))
        ToggleRow(
            "Calculate before exam",
            "Calculate remaining classes until the start of the next exam period instead of the end of the semester.",
            state.calcBeforeExam,
        ) { repo.setCalcBeforeExam(!state.calcBeforeExam) }
    }
}
