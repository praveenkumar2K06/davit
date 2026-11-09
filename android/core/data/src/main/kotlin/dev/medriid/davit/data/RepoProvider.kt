package dev.medriid.davit.data

import androidx.compose.runtime.staticCompositionLocalOf
import dev.medriid.davit.ui.Nav

interface RepoProvider {
    val repo: Repo
}

val LocalRepo = staticCompositionLocalOf<Repo> { error("no Repo") }

fun Nav.showOnMap(repo: Repo, venue: String) {
    repo.mapFocus.value = venue
    tab(4)
}
