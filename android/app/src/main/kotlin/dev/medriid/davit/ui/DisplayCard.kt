package dev.medriid.davit.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.Banner
import dev.medriid.davit.data.BannerStat
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.design.ChoiceRow
import dev.medriid.davit.design.EditorialShape
import dev.medriid.davit.design.InkButton
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoType
import kotlinx.coroutines.launch

/** Make the top bar yours: a built-in print or your own photo behind it, the two stats it shows, and your name. */
@Composable
internal fun DisplayCardScreen(route: Route) {
    val repo = LocalRepo.current
    val state by repo.state.collectAsState()
    val scope = rememberCoroutineScope()
    val inks = LocalInks.current
    val b = state.banner
    var note by remember { mutableStateOf<String?>(null) }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch { note = if (repo.setBannerPhoto(uri)) null else "Couldn't read that image." }
    }
    val editorial = inks.editorial
    val material = inks.material
    val tile = if (editorial) EditorialShape else if (material) RoundedCornerShape(16.dp) else RoundedCornerShape(10.dp)
    RouteScreen(
        route,
        "Display card",
        if (editorial) "The picture at the top of Home: a print or your own photo. Nothing leaves this phone."
        else if (material) "The banner at the top of Home: a print or your own photo. Nothing leaves this phone."
        else "The top bar on Home, printed on a background of your choice. Nothing leaves this phone.",
    ) {
        // As it looks on Home, changing as you pick.
        if (editorial) EditorialPicture(state.snapshot, onClick = {}, onChange = null)
        else if (material) MaterialBanner(state, Mascot.parse(state.mascot), repo.bannerPhoto, onSync = repo::sync)
        else TopBar(state, Mascot.parse(state.mascot), repo.bannerPhoto, card = true, onSync = {}, onProfile = {}, onMore = {}, gutter = 0.dp)

        GroupLabel("Background", Modifier.padding(top = 18.dp, bottom = 8.dp))
        Banner.Styles.chunked(3).forEachIndexed { row, names ->
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                names.forEachIndexed { i, label ->
                    val index = row * 3 + i
                    val on = !b.photo && b.style == index
                    Column(
                        Modifier
                            .weight(1f)
                            .clickable(role = Role.RadioButton) { repo.setBanner(b.copy(style = index, photo = false)) }
                            .semantics { selected = on },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BannerArt(
                            index,
                            Modifier.fillMaxWidth().height(56.dp).clip(tile)
                                .border(
                                    if (on) (if (editorial) 2.dp else 3.dp) else (if (editorial) 1.dp else 1.5.dp),
                                    if (on || !editorial) inks.text else inks.rule,
                                    tile,
                                ),
                        )
                        InkText(label, style = RisoType.small.copy(fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal))
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InkButton(if (b.photo) "Another photo" else "Your own photo", {
                runCatching { pick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            }, modifier = Modifier.weight(1f), fillWidth = true)
            if (b.photo) InkButton("Remove photo", repo::removeBannerPhoto, quiet = true, modifier = Modifier.weight(1f), fillWidth = true)
        }
        note?.let { InkText(it, style = RisoType.small, modifier = Modifier.padding(top = 6.dp)) }
        if (b.photo) {
            GroupLabel("Which part of the photo", Modifier.padding(top = 16.dp, bottom = 8.dp))
            val stops = listOf(0f, 0.5f, 1f)
            ChoiceRow(listOf("Top", "Middle", "Bottom"), stops.indexOfFirst { kotlin.math.abs(it - b.focus) < 0.2f }) {
                repo.setBanner(b.copy(focus = stops[it]))
            }
        }

        // Editorial's picture carries the attendance figure, not these.
        if (editorial) return@RouteScreen
        // Attendance isn't offered: Home already says it big.
        val stats = BannerStat.entries - BannerStat.Attendance
        GroupLabel("First stat", Modifier.padding(top = 18.dp, bottom = 8.dp))
        ChoiceRow(stats.map { it.title }, stats.indexOf(BannerStat.of(b.left)).coerceAtLeast(0)) { repo.setBanner(b.copy(left = stats[it].id)) }
        GroupLabel("Second stat", Modifier.padding(top = 16.dp, bottom = 8.dp))
        ChoiceRow(stats.map { it.title }, stats.indexOf(BannerStat.of(b.right)).coerceAtLeast(0)) { repo.setBanner(b.copy(right = stats[it].id)) }

        GroupLabel("In the bar", Modifier.padding(top = 18.dp, bottom = 4.dp))
        ToggleRow("Your name instead of daVIT", "As VTOP has it. Only on your screen.", b.nameInBar) { repo.setBanner(b.copy(nameInBar = !b.nameInBar)) }
        ToggleRow(if (material) "Print the banner on a card" else "Print the bar on a card", if (material) "Off for a plain top bar." else "Off for a plain top bar.", !b.hidden) { repo.setBanner(b.copy(hidden = !b.hidden)) }
        Spacer(Modifier.height(10.dp))
        InkText(if (material) "Your mascot, in the banner's corner, is changed in Settings → Look." else "Your mascot, in the bar's corner, is changed in Settings → Look.", style = RisoType.small)
    }
}
