package dev.medriid.davit.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.PreviewSizeMode
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import dev.medriid.davit.DavitApp
import dev.medriid.davit.MainActivity
import dev.medriid.davit.data.UiState
import dev.medriid.davit.data.atRisk
import dev.medriid.davit.data.hint
import dev.medriid.davit.data.isLabType
import dev.medriid.davit.data.percent
import dev.medriid.davit.data.remainingFor
import dev.medriid.davit.data.watchList
import java.time.LocalDateTime

/**
 * Home-screen widget: the courses at or near the line (your own warning line, or a couple
 * of skips from it), worst first, each with what it takes. As many as fit.
 * Enhanced with Material You dynamic theming and Material 3 container hierarchy.
 */
class AttendanceWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(
        setOf(
            DpSize(250.dp, 110.dp),
            DpSize(320.dp, 160.dp),
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = (context.applicationContext as DavitApp).repo.state.value
        provideContent { Content(state, LocalDateTime.now()) }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val state = (context.applicationContext as? DavitApp)?.repo?.state?.value
        val previewState = if (state?.signedIn == true && state.snapshot != null) state else sampleState()
        provideContent { Content(previewState, LocalDateTime.now()) }
    }
}

class AttendanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AttendanceWidget()
}

@Composable
private fun Content(s: UiState, now: LocalDateTime) {
    DavitWidgetTheme(s) {
        Column(
            GlanceModifier
                .fillMaxSize()
                .cornerRadius(24.dp)
                .background(GlanceTheme.colors.widgetBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clickable(actionStartActivity<MainActivity>()),
        ) {
            Row(
                GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Attendance", style = style(GlanceTheme.colors.primary, 14.sp, bold = true))
                Spacer(GlanceModifier.defaultWeight())
                s.snapshot?.let { snap ->
                    Row(
                        GlanceModifier
                            .cornerRadius(12.dp)
                            .background(GlanceTheme.colors.tertiaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(ago(snap.syncedAt), style = style(GlanceTheme.colors.onTertiaryContainer, 11.sp))
                    }
                }
            }
            Spacer(GlanceModifier.height(8.dp))
            val snap = s.snapshot
            val semesterOver = s.followCalendar && snap?.calendar?.isSemesterOver(now.toLocalDate()) == true
            when {
                !s.signedIn -> WidgetInfoCard(
                    title = "Sign in to daVIT",
                    subtitle = "Open the app to connect your account and view your attendance.",
                )
                snap == null -> WidgetInfoCard(
                    title = "Waiting for First Sync",
                    subtitle = "Syncing your attendance data...",
                )
                snap.attendanceExempt -> WidgetInfoCard(
                    title = "Attendance Exempt (CGPA 9+)",
                    subtitle = "The 75% attendance requirement does not apply to you.",
                    containerColor = GlanceTheme.colors.secondaryContainer,
                    contentColor = GlanceTheme.colors.onSecondaryContainer,
                )
                semesterOver -> WidgetInfoCard(
                    title = "Semester Ended",
                    subtitle = "No more instructional days scheduled for this semester.",
                )
                else -> {
                    val watch = snap.watchList(s.alertPrefs)
                    if (watch.isEmpty()) {
                        val lowest = snap.attendance.minByOrNull { it.percent }
                        Column(
                            GlanceModifier
                                .fillMaxWidth()
                                .cornerRadius(16.dp)
                                .background(GlanceTheme.colors.primaryContainer)
                                .padding(12.dp),
                        ) {
                            Text("All clear of ${s.alertPrefs.warnBelow}%.", style = style(GlanceTheme.colors.onPrimaryContainer, 14.sp, bold = true))
                            lowest?.let {
                                Text("Lowest: ${it.courseName} at ${it.percent}%", style = style(GlanceTheme.colors.onPrimaryContainer, 12.sp), maxLines = 1)
                            }
                        }
                    } else {
                        val fits = ((LocalSize.current.height.value - 44f) / 44f).toInt().coerceAtLeast(1)
                        watch.take(fits).forEach { row ->
                            Row(
                                GlanceModifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .cornerRadius(12.dp)
                                    .background(GlanceTheme.colors.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    GlanceModifier
                                        .cornerRadius(8.dp)
                                        .background(if (row.atRisk) GlanceTheme.colors.errorContainer else GlanceTheme.colors.secondaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        "${row.percent}%",
                                        style = style(if (row.atRisk) GlanceTheme.colors.onErrorContainer else GlanceTheme.colors.onSecondaryContainer, 13.sp, bold = true),
                                    )
                                }
                                Spacer(GlanceModifier.width(8.dp))
                                Column(GlanceModifier.defaultWeight()) {
                                    Text(
                                        row.courseName + if (isLabType(row.classType)) " · Lab" else "",
                                        style = style(GlanceTheme.colors.onSurface, 12.sp, bold = true),
                                        maxLines = 1,
                                    )
                                    Text(row.hint(snap.remainingFor(row, now)), style = style(GlanceTheme.colors.onSurfaceVariant, 11.sp), maxLines = 1)
                                }
                            }
                        }
                        if (watch.size > fits) {
                            Text("+${watch.size - fits} more", style = style(GlanceTheme.colors.onSurfaceVariant, 11.sp))
                        }
                    }
                }
            }
        }
    }
}
