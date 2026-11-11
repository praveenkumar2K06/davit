package dev.medriid.davit.ui.material3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.design.RisoIcon

@Composable
fun MaterialFlagsSettings() {
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()

    MaterialSettingsSectionHeader("Attendance & Schedule", icon = RisoIcon.Flag)
    MaterialSegmentedToggleRow(
        title = "Follow academic calendar",
        body = "Stop showing timetable classes and widget warnings after the semester's last instructional day.",
        checked = state.followCalendar,
        index = 0,
        count = 2,
        onCheckedChange = { repo.setFollowCalendar(it) },
    )
    MaterialSegmentedToggleRow(
        title = "Calculate before exam",
        body = "Calculate remaining classes until the start of the next exam period instead of the end of the semester.",
        checked = state.calcBeforeExam,
        index = 1,
        count = 2,
        onCheckedChange = { repo.setCalcBeforeExam(it) },
    )
}
