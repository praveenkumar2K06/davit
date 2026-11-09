package dev.medriid.davit.ui

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.vtop.FacultyHit
import dev.medriid.davit.vtop.FacultyInfo
import dev.medriid.davit.data.showOnMap
import dev.medriid.davit.data.titleCase
import dev.medriid.davit.design.ErrorNote
import dev.medriid.davit.design.InkCard
import dev.medriid.davit.design.InkChip
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.Loading
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoType
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FacultyRatingLink(empId: String?, name: String, modifier: Modifier = Modifier) {
    if (empId == null) return
    val nav = LocalNav.current
    val inks = LocalInks.current
    InkText(
        "Reviews on No8AM",
        style = RisoType.small.copy(fontWeight = FontWeight.SemiBold, color = inks.a),
        modifier = modifier.clickable { nav.go(Route.FacultyReview(empId)) },
    )
}

@Composable
fun VtopFacultySearch(query: String) {
    val repo = LocalRepo.current
    val q = facultyQuery(query)
    if (q.count { it.isLetter() } < 3) return
    val results = remember { mutableStateMapOf<String, Result<List<FacultyHit>>>() }
    LaunchedEffect(q) {
        if (q in results) return@LaunchedEffect
        delay(650.milliseconds)
        results[q] = runCatching { repo.onVtop { c -> c.searchFaculty(q) } }
    }
    SectionTitle("Anyone on VTOP")
    val r = results[q]
    when {
        r == null -> Loading("Searching VTOP for \"$q\"")
        r.isFailure -> ErrorNote(repo.explain(r.exceptionOrNull()!!)) { results.remove(q) }
        r.getOrThrow().isEmpty() -> InkText("VTOP has nobody called \"$q\".", style = RisoType.small)
        else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            r.getOrThrow().take(30).forEach { hit -> FacultyHitCard(hit) }
            if (r.getOrThrow().size > 30) InkText("${r.getOrThrow().size - 30} more; type more of the name.", style = RisoType.small)
        }
    }
}

private val facultyCards = mutableStateMapOf<String, FacultyInfo?>()

@Composable
private fun FacultyHitCard(hit: FacultyHit) {
    val repo = LocalRepo.current
    val context = LocalContext.current
    val nav = LocalNav.current
    val open = hit.empId in facultyCards
    val card = facultyCards[hit.empId]
    var loading = false
    var error: String? = null
    LaunchedEffect(open) {
        if (open && hit.empId !in facultyCards) {
            loading = true
            runCatching { repo.onVtop { c -> c.faculty(hit.empId) } }
                .onSuccess { facultyCards[hit.empId] = it }
                .onFailure { error = repo.explain(it) }
            loading = false
        }
    }
    InkCard(Modifier.fillMaxWidth(), onClick = { if (open) facultyCards.remove(hit.empId) else facultyCards[hit.empId] = null }, plateOffset = 4.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                InkText(personName(hit.name), style = RisoType.body.copy(fontWeight = FontWeight.SemiBold), maxLines = 2)
                InkText(listOf(hit.designation, hit.school).filter { it.isNotBlank() }.joinToString(" · "), style = RisoType.small, maxLines = 2)
            }
            Spacer(Modifier.width(8.dp))
            InkChip(if (open) "Less" else "Cabin")
        }
        FacultyRatingLink(hit.empId, hit.name, Modifier.padding(top = 8.dp))
        if (!open) return@InkCard
        Spacer(Modifier.height(8.dp))
        when {
            loading -> InkText("Opening their card on VTOP", style = RisoType.small)
            error != null -> InkText(error!!, style = RisoType.small.copy(color = LocalInks.current.a))
            card == null && hit.empId in facultyCards -> InkText("VTOP didn't open this card.", style = RisoType.small)
            card != null -> {
                if (card.department.isNotBlank()) InkText(card.department, style = RisoType.small)
                Spacer(Modifier.height(6.dp))
                if (card.cabin.isBlank() && card.email.isBlank()) InkText("No cabin or email on VTOP.", style = RisoType.small)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (card.cabin.isNotBlank()) dev.medriid.davit.ui.map.MapChip(card.cabin, label = "Cabin ${card.cabin}") { nav.showOnMap(repo, card.cabin) }
                    if (card.email.isNotBlank()) {
                        Box(Modifier.clickable(role = Role.Button) {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_SENDTO, ("mailto:" + card.email).toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }) { InkChip(card.email, solid = true) }
                    }
                }
            }
        }
    }
}

private fun facultyQuery(s: String): String =
    s.trim().uppercase().replace(Regex("""^(DR|PROF|MR|MRS|MS)\.?\s+"""), "").replace(Regex("[^A-Z ]"), " ").trim()

private fun personName(s: String): String =
    titleCase(s.lowercase())
