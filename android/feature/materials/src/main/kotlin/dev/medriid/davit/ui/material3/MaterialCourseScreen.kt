package dev.medriid.davit.ui.material3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.CachedCourse
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.data.MaterialRow
import dev.medriid.davit.data.Section
import dev.medriid.davit.data.Snapshot
import dev.medriid.davit.data.courseName
import dev.medriid.davit.design.ErrorNote
import dev.medriid.davit.design.Loading
import dev.medriid.davit.design.SearchGlyph
import dev.medriid.davit.design.material.MaterialButton
import dev.medriid.davit.design.printIn
import dev.medriid.davit.design.rememberHaptics
import dev.medriid.davit.ui.FileRow
import dev.medriid.davit.ui.Freshness
import dev.medriid.davit.ui.LocalNav
import dev.medriid.davit.ui.Route
import dev.medriid.davit.ui.RouteLazyScreen
import dev.medriid.davit.ui.RouteScreen
import dev.medriid.davit.ui.materialLine
import dev.medriid.davit.ui.sharedCard
import dev.medriid.davit.ui.titleCase
import dev.medriid.davit.vtop.CourseOption

/**
 * Material 3 version of CourseScreen: displaying faculty uploads, modules, and course files
 * with Material 3 typography, cards, chips, and controls.
 */
@Composable
fun MaterialCourseScreen(route: Route.Course) {
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()
    val campus by repo.campus.state.collectAsState()
    val pages by repo.materials.pages.collectAsState()
    val loading by repo.materials.loading.collectAsState()
    val errors by repo.materials.errors.collectAsState()
    val saved by repo.files.saved.collectAsState()
    val cached = pages[route.key]
    val course = campus.courses.firstOrNull { it.key == route.key } ?: cached?.course
    LaunchedEffect(course) { course?.let { repo.materials.load(it) } }
    if (course == null) {
        RouteScreen(route, "Course page") {
            Text(
                "This course isn't on your course pages any more.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    val snapshot = state.snapshot
    val rows = remember(cached, snapshot) { cached?.let { repo.materials.rows(it, snapshot) }.orEmpty() }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var othersOpen by rememberSaveable { mutableStateOf(false) }
    val faculty = remember(course, snapshot) {
        snapshot?.timetable.orEmpty()
            .mapNotNull { if (it.courseCode == course.code && it.faculty.isNotBlank()) it.faculty else null }
            .distinct()
    }
    val (mine, others) = remember(rows, filter, saved) {
        val s = when (filter) {
            1 -> rows.filter { it.mine }
            2 -> rows.filter { it.key in saved }
            else -> rows
        }
        s.filter { it.mine } to s.filterNot { it.mine }
    }
    val mineCount = remember(rows) { rows.count { it.mine } }
    val savedCount = remember(rows, saved) { rows.count { it.key in saved } }
    val savedHere = remember(rows, saved) { rows.filter { it.key in saved } }
    val mineModuleGroups = remember(mine) {
        mine.groupBy { it.material.module }
            .entries
            .sortedWith(compareBy({ it.key.isEmpty() }, { it.key.toIntOrNull() ?: Int.MAX_VALUE }))
    }
    val othersModuleGroups = remember(others) {
        others.groupBy { it.material.module }
            .entries
            .sortedWith(compareBy({ it.key.isEmpty() }, { it.key.toIntOrNull() ?: Int.MAX_VALUE }))
    }

    RouteLazyScreen(
        route, course.code, "${state.aliases.courseName(course.code, course.name)} · ${course.typeName.ifBlank { course.type }}",
        refreshing = route.key in loading && cached != null,
        onRefresh = { repo.materials.load(course, force = true) },
    ) {
        item(key = "top", contentType = "top") {
            Column {
                cached?.let { Freshness(it.at) }
                errors[route.key]?.let { ErrorNote(it) { repo.materials.load(course, force = true) } }
                if (cached != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        FilterChip(
                            selected = filter == 0,
                            onClick = { filter = 0 },
                            label = { Text("All ${rows.size}") },
                        )
                        FilterChip(
                            selected = filter == 1,
                            onClick = { filter = 1 },
                            label = { Text("Your faculty $mineCount") },
                        )
                        FilterChip(
                            selected = filter == 2,
                            onClick = { filter = 2 },
                            label = { Text("Saved $savedCount") },
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
        if (cached == null) {
            item(key = "loading", contentType = "loading") { Loading(if (route.key in loading) "Reading the course page (big ones take a few seconds)" else "Not fetched yet. Pull down to fetch.") }
            return@RouteLazyScreen
        }
        if (rows.isEmpty()) {
            item(key = "none", contentType = "none") {
                Text(
                    "Nobody has uploaded anything here yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
        if (mine.isNotEmpty() || (filter != 2 && faculty.isNotEmpty())) {
            item(key = "mine-head", contentType = "header") {
                Text(
                    text = "Your faculty" + if (faculty.isNotEmpty()) " · " + faculty.joinToString(", ") { titleCase(it) } else "",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
                )
            }
            if (mine.isEmpty()) {
                item(key = "mine-none", contentType = "empty") {
                    Text(
                        "Nothing from your faculty on this page yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            materialByModule("mine", mineModuleGroups, cached.page.modules)
        }
        if (others.isNotEmpty()) {
            item(key = "others-head", contentType = "header") {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 6.dp)
                        .clickable(role = Role.Button) { othersOpen = !othersOpen },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (mine.isEmpty() && faculty.isEmpty()) "Everyone's uploads" else "Other faculty",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (!othersOpen && mine.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Text(
                                if (othersOpen || mine.isEmpty()) "${others.size}" else "Show ${others.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (!othersOpen && mine.isNotEmpty()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
            if (othersOpen || mine.isEmpty()) materialByModule("others", othersModuleGroups, cached.page.modules)
        }
        if (savedHere.isNotEmpty()) {
            item(key = "clear", contentType = "clear") {
                Column(Modifier.padding(top = 20.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(10.dp))
                    val haptics = rememberHaptics()
                    var sure by remember { mutableStateOf(false) }
                    MaterialButton(
                        if (sure) "Tap again to delete ${savedHere.size} saved file${if (savedHere.size > 1) "s" else ""}" else "Delete this course's downloads",
                        onClick = {
                            if (sure) {
                                savedHere.forEach { repo.files.delete(it.key) }
                                haptics.tick()
                            }
                            sure = !sure
                        },
                        quiet = !sure,
                        fillWidth = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Material 3 layout for Course Pages overview segment (Academics → Materials). */
@Composable
fun MaterialMaterialsSegment(snapshot: Snapshot) {
    val repo = LocalRepo.current
    val nav = LocalNav.current
    val campus by repo.campus.state.collectAsState()
    val loading by repo.campus.loading.collectAsState()
    val errors by repo.campus.errors.collectAsState()
    val pages by repo.materials.pages.collectAsState()
    val saved by repo.files.saved.collectAsState()
    val aliases = repo.state.collectAsState().value.aliases
    LaunchedEffect(Unit) { repo.campus.refresh(Section.Courses) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .sharedCard(Route.Search.sharedKey)
            .clickable(role = Role.Button) { nav.go(Route.Search) },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchGlyph()
            Spacer(Modifier.width(12.dp))
            Text(
                "Search every course's materials",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Spacer(Modifier.height(14.dp))
    val courses = campus.courses
    errors[Section.Courses]?.let { ErrorNote(it) { repo.campus.refresh(Section.Courses, force = true) } }
    if (courses.isEmpty()) {
        if (Section.Courses in loading) Loading("Reading your course pages from VTOP") else Text("No course pages on VTOP yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val order = remember(courses, snapshot.semester.id) {
        courses.groupBy { it.semesterId }.entries.sortedBy { if (it.key == snapshot.semester.id) 0 else 1 }
    }
    var i = 0
    order.forEach { (_, list) ->
        Text(
            list.first().semesterName.ifBlank { "Courses" },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            list.forEach { c ->
                val cached = pages[c.key]
                val rows = remember(cached, snapshot) { cached?.let { repo.materials.rows(it, snapshot) }.orEmpty() }
                val mineCount = remember(rows) { rows.count { it.mine } }
                val savedCount = remember(rows, saved) { rows.count { it.key in saved } }
                MaterialCourseCard(c, aliases.courseName(c.code, c.name), cached, mineCount, savedCount, Modifier.printIn(i++)) { nav.go(Route.Course(c.key)) }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        "Pages open offline once fetched. Downloads stay on this phone until you delete them.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun MaterialCourseCard(c: CourseOption, name: String, cached: CachedCourse?, mine: Int, saved: Int, modifier: Modifier, onOpen: () -> Unit) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .sharedCard(Route.Course(c.key).sharedKey),
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        name.ifBlank { c.code },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                    )
                    Text(
                        "${c.code} · ${c.typeName.ifBlank { c.type }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                if (cached != null) {
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                "${cached.page.materials.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                "files",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            )
                        }
                    }
                }
            }
            if (cached != null && (mine > 0 || saved > 0)) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (mine > 0) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                "$mine from your faculty",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                    if (saved > 0) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                            Text(
                                "$saved saved",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.materialByModule(
    prefix: String,
    groups: List<Map.Entry<String, List<MaterialRow>>>,
    modules: Map<String, String>,
) {
    groups.forEach { (module, list) ->
        item(key = "$prefix-m-$module", contentType = "module_header") {
            val name = modules[module]
            Text(
                if (module.isEmpty()) "Other" else "Module $module" + (name?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
                maxLines = 2,
            )
        }
        list.forEachIndexed { i, r ->
            item(key = "$prefix-f-${r.material.fileId}", contentType = "file_row") {
                Column(Modifier.printIn(i)) {
                    FileRow(
                        key = r.key,
                        title = r.material.title,
                        subtitle = materialLine(r),
                        fetch = { c, out, p -> c.downloadMaterial(r.material.fileId, out, p) },
                        index = i,
                        count = list.size,
                    )
                }
            }
        }
    }
}
