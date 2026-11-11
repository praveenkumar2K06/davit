package dev.medriid.davit.widget

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.PreviewSizeMode
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.setWidgetPreviews
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.medriid.davit.DavitApp
import dev.medriid.davit.MainActivity
import dev.medriid.davit.data.UiState
import dev.medriid.davit.data.atRisk
import dev.medriid.davit.data.clock
import dev.medriid.davit.data.hint
import dev.medriid.davit.data.overallPercent
import dev.medriid.davit.data.percent
import dev.medriid.davit.data.planFor
import dev.medriid.davit.data.remainingFor
import dev.medriid.davit.data.vtopDate
import dev.medriid.davit.data.weekOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

/**
 * Home-screen widget: the class on now (or next), its attendance, and the nearest thing
 * due. Enhanced with Material You dynamic theming and Material 3 container hierarchy.
 */
class DavitWidget : GlanceAppWidget() {
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

    companion object {
        /** Redraws every placed widget, both kinds; safe to call from anywhere. */
        fun refresh(context: Context) {
            CoroutineScope(Dispatchers.Default).launch {
                runCatching { DavitWidget().updateAll(context.applicationContext) }
                runCatching { AttendanceWidget().updateAll(context.applicationContext) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    runCatching { GlanceAppWidgetManager(context.applicationContext).setWidgetPreviews<DavitWidgetReceiver>() }
                    runCatching { GlanceAppWidgetManager(context.applicationContext).setWidgetPreviews<AttendanceWidgetReceiver>() }
                }
            }
        }
    }
}

class DavitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DavitWidget()
}

@Composable
private fun Content(s: UiState, now: LocalDateTime) {
    DavitWidgetTheme(s) {
        val size = LocalSize.current
        val isCompact = size.height < 140.dp
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
                Text("daVIT", style = style(GlanceTheme.colors.primary, 14.sp, bold = true))
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
            when {
                !s.signedIn -> WidgetInfoCard(
                    title = "Sign in to daVIT",
                    subtitle = "Open the app to connect your account and view your timetable.",
                )
                snap == null -> WidgetInfoCard(
                    title = "Waiting for First Sync",
                    subtitle = "Syncing your timetable data...",
                )
                else -> {
                    val plan = planFor(weekOf(snap), now, snap.calendar, s.followCalendar)
                    val slot = plan.now ?: plan.next
                    if (slot == null) {
                        val semesterOver = s.followCalendar && snap.calendar?.isSemesterOver(now.toLocalDate()) == true
                        WidgetInfoCard(
                            title = if (semesterOver) "Semester Ended" else "No Classes Scheduled",
                            subtitle = if (semesterOver) "No more instructional days scheduled for this semester." else "No upcoming classes found in your timetable.",
                        )
                    } else {
                        val m = slot.meeting
                        val isNow = plan.now != null
                        val label = if (isNow) {
                            "NOW · until ${clock(m.end)}"
                        } else {
                            "NEXT · " + when (plan.nextInDays) {
                                0 -> clock(m.start)
                                1 -> "tomorrow ${clock(m.start)}"
                                else -> (plan.nextDate?.dayOfWeek ?: DayOfWeek.of(m.day)).getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH) + " " + clock(m.start)
                            }
                        }
                        Row(
                            GlanceModifier
                                .cornerRadius(8.dp)
                                .background(if (isNow) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                label,
                                style = style(
                                    if (isNow) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSecondaryContainer,
                                    11.sp,
                                    bold = true,
                                ),
                            )
                        }
                        Spacer(GlanceModifier.height(4.dp))
                        Text(slot.course.courseName, style = style(GlanceTheme.colors.onSurface, 16.sp, bold = true), maxLines = 1)
                        val venueType = listOf(slot.course.venue, if (slot.isLab) "Lab" else "Theory").filter { it.isNotBlank() }.joinToString(" · ")
                        if (venueType.isNotBlank()) {
                            Text(
                                venueType,
                                style = style(GlanceTheme.colors.onSurfaceVariant, 12.sp),
                                maxLines = 1,
                            )
                        }
                        slot.attendance?.let { row ->
                            Spacer(GlanceModifier.height(4.dp))
                            val atRisk = row.atRisk && !snap.attendanceExempt
                            Row(
                                GlanceModifier
                                    .cornerRadius(8.dp)
                                    .background(if (atRisk) GlanceTheme.colors.errorContainer else GlanceTheme.colors.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    "${row.percent}% · ${row.hint(snap.remainingFor(row, now))}",
                                    style = style(
                                        if (atRisk) GlanceTheme.colors.onErrorContainer else GlanceTheme.colors.onSurfaceVariant,
                                        11.sp,
                                        bold = true,
                                    ),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    if (!isCompact) {
                        Spacer(GlanceModifier.defaultWeight())
                        val foot = footer(s, now)
                        if (foot.isNotBlank()) {
                            Row(
                                GlanceModifier
                                    .fillMaxWidth()
                                    .cornerRadius(10.dp)
                                    .background(GlanceTheme.colors.surfaceVariant)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    foot,
                                    style = style(GlanceTheme.colors.onSurfaceVariant, 11.sp),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** The nearest unsubmitted assignment within two weeks, else overall attendance. */
private fun footer(s: UiState, now: LocalDateTime): String {
    val snap = s.snapshot ?: return ""
    val today = now.toLocalDate()
    val due = snap.assignments.asSequence().flatMap { it.items }
        .filter { it.updated == null }
        .mapNotNull { a -> vtopDate(a.due)?.let { a to it } }
        .filter { (_, d) -> !d.isBefore(today) && d.toEpochDay() - today.toEpochDay() <= 14 }
        .minByOrNull { it.second }
    if (due != null) {
        val whenText = when (val days = due.second.toEpochDay() - today.toEpochDay()) {
            0L -> "today"
            1L -> "tomorrow"
            else -> "in $days days"
        }
        return "Due $whenText · ${due.first.title}"
    }
    val att = snap.attendance
    val risky = if (snap.attendanceExempt) 0 else att.count { it.atRisk }
    return (overallPercent(att)?.let { "$it% overall · " } ?: "") + "$risky at risk"
}

internal fun ago(at: Long): String {
    val min = (System.currentTimeMillis() - at) / 60_000
    return when {
        min < 1 -> "just now"
        min < 60 -> "$min min ago"
        min < 48 * 60 -> "${min / 60} h ago"
        else -> "${min / (24 * 60)} d ago"
    }
}
