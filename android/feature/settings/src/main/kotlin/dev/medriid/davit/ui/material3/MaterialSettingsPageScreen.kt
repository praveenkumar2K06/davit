package dev.medriid.davit.ui.material3

import androidx.compose.runtime.Composable
import dev.medriid.davit.ui.Route
import dev.medriid.davit.ui.RouteScreen
import dev.medriid.davit.ui.SettingsGroup

/** Material 3 version of SettingsPageScreen: routes each group to its Material 3 page implementation. */
@Composable
fun MaterialSettingsPageScreen(route: Route.SettingsPage) {
    val g = SettingsGroup.entries.firstOrNull { it.id == route.group } ?: SettingsGroup.Look
    RouteScreen(route, g.title) {
        when (g) {
            SettingsGroup.Look -> MaterialLookSettings()
            SettingsGroup.Yours -> MaterialYoursSettings()
            SettingsGroup.Alerts -> {}
            SettingsGroup.Sync -> MaterialSyncSettings()
            SettingsGroup.Flags -> MaterialFlagsSettings()
            SettingsGroup.Phone -> MaterialPhoneSettings()
            SettingsGroup.About -> MaterialAboutSettings()
        }
    }
}
