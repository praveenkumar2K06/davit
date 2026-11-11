package dev.medriid.davit.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsGroupTest {

    @Test
    fun settingsGroupsHaveUniqueIds() {
        val ids = SettingsGroup.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun settingsIndexContainsEntriesForEveryGroup() {
        val mappedGroups = SettingsIndex.map { it.third }.toSet()
        for (group in SettingsGroup.entries) {
            assertTrue("Expected SettingsIndex to contain entries for group ${group.name}", group in mappedGroups)
        }
    }

    @Test
    fun searchIndexMatchesKeywords() {
        val darkEntry = SettingsIndex.firstOrNull { it.second.contains("dark mode") }
        assertNotNull(darkEntry)
        assertEquals(SettingsGroup.Look, darkEntry!!.third)

        val widgetEntry = SettingsIndex.firstOrNull { it.second.contains("widget") }
        assertNotNull(widgetEntry)
        assertEquals(SettingsGroup.Phone, widgetEntry!!.third)
    }
}
