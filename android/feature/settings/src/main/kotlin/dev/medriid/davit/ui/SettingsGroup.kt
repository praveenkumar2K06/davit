package dev.medriid.davit.ui

import dev.medriid.davit.design.RisoIcon

/** The groups Settings opens onto, each its own short page. */
enum class SettingsGroup(val id: String, val title: String, val icon: RisoIcon) {
    Look("look", "Look", RisoIcon.Picture),
    Yours("yours", "Make it yours", RisoIcon.Profile),
    Alerts("alerts", "Notifications", RisoIcon.Bell),
    Sync("sync", "Sync and semester", RisoIcon.Today),
    Flags("flags", "Flags", RisoIcon.Flag),
    Phone("phone", "On this phone", RisoIcon.Download),
    About("about", "About and help", RisoIcon.Book),
}

/** Every setting by name, for Find a setting: what it's called, other words for it, and its group. */
internal val SettingsIndex: List<Triple<String, String, SettingsGroup>> = listOf(
    Triple("Theme style & look", "ui theme look layout style", SettingsGroup.Look),
    Triple("Light or dark", "dark mode night theme", SettingsGroup.Look),
    Triple("Colours", "ink pair palette colour color", SettingsGroup.Look),
    Triple("Your own colours", "custom palette color", SettingsGroup.Look),
    Triple("Display card", "banner photo background top bar picture", SettingsGroup.Yours),
    Triple("Your mascot", "avatar face profile picture", SettingsGroup.Yours),
    Triple("Course names", "alias rename nickname subjects", SettingsGroup.Yours),
    Triple("Notifications", "alerts", SettingsGroup.Alerts),
    Triple("Attendance warnings", "75 percent line alert", SettingsGroup.Alerts),
    Triple("Reminders before class", "before class reminder", SettingsGroup.Alerts),
    Triple("Meal reminders", "mess menu food breakfast lunch snacks dinner", SettingsGroup.Alerts),
    Triple("New marks, deadlines, exams, registration", "alerts marks deadline exam registration remarks", SettingsGroup.Alerts),
    Triple("Sync now", "refresh update data", SettingsGroup.Sync),
    Triple("Sync on app open", "open launch refresh auto sync", SettingsGroup.Sync),
    Triple("Sync in the background", "background refresh battery", SettingsGroup.Sync),
    Triple("Calculate before exam", "calculate attendance exam remaining classes flag flags", SettingsGroup.Flags),
    Triple("Let daVIT run in the background", "battery optimisation unrestricted", SettingsGroup.Sync),
    Triple("Semester", "term", SettingsGroup.Sync),
    Triple("Calendar class group", "freshers group saturday", SettingsGroup.Sync),
    Triple("App lock", "fingerprint face lock security", SettingsGroup.Phone),
    Triple("Home screen widget", "widget", SettingsGroup.Phone),
    Triple("Attendance widget", "widget", SettingsGroup.Phone),
    Triple("Classes in your calendar", "google calendar phone export", SettingsGroup.Phone),
    Triple("Classes as a file", "ics export share", SettingsGroup.Phone),
    Triple("Downloads", "files offline saved", SettingsGroup.Phone),
    Triple("Campus map", "map refresh navigation", SettingsGroup.Phone),
    Triple("Updates", "version new release install", SettingsGroup.About),
    Triple("Update by itself", "auto update", SettingsGroup.About),
    Triple("Credits", "thanks licence", SettingsGroup.About),
    Triple("Test background sync", "debug", SettingsGroup.About),
    Triple("Save VTOP pages", "debug parser", SettingsGroup.About),
)
