package dev.medriid.davit.ui

import androidx.compose.runtime.Composable

/** One group's own page. */
@Composable
fun SettingsPageScreen(route: Route.SettingsPage) {
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
