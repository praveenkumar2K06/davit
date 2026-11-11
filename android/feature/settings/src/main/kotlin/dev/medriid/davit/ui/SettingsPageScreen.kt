package dev.medriid.davit.ui

import androidx.compose.runtime.Composable
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.ui.material3.MaterialSettingsPageScreen

/** One group's own page. */
@Composable
fun SettingsPageScreen(route: Route.SettingsPage) {
    if (LocalInks.current.material) {
        MaterialSettingsPageScreen(route)
        return
    }
    val g = SettingsGroup.entries.firstOrNull { it.id == route.group } ?: SettingsGroup.Look
    RouteScreen(route, g.title) {
        when (g) {
            SettingsGroup.Look -> LookSettings()
            SettingsGroup.Yours -> YoursSettings()
            SettingsGroup.Alerts -> {}
            SettingsGroup.Sync -> SyncSettings()
            SettingsGroup.Flags -> FlagsSettings()
            SettingsGroup.Phone -> PhoneSettings()
            SettingsGroup.About -> AboutSettings()
        }
    }
}
