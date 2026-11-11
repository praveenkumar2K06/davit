package dev.medriid.davit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CampusTest {

    @Test
    fun defaultCampusModelIsEmpty() {
        val campus = Campus()
        assertEquals(0L, campus.peopleAt)
        assertTrue(campus.hodDean.isEmpty())
        assertTrue(campus.classMessages.isEmpty())
        assertTrue(campus.spotlight.isEmpty())
        assertTrue(campus.receipts.isEmpty())
        assertTrue(campus.curriculum.isEmpty())
        assertTrue(campus.semesterGrades.isEmpty())
    }

    @Test
    fun sectionFreshnessThresholds() {
        val now = System.currentTimeMillis()

        // Messages is fresh for 20 minutes (1_200_000 ms)
        assertTrue(Section.Messages.fresh(now - 10 * 60 * 1000L))
        assertFalse(Section.Messages.fresh(now - 25 * 60 * 1000L))

        // Spotlight is fresh for 6 hours
        assertTrue(Section.Spotlight.fresh(now - 2 * 3600 * 1000L))
        assertFalse(Section.Spotlight.fresh(now - 7 * 3600 * 1000L))

        // Money is fresh for 24 hours
        assertTrue(Section.Money.fresh(now - 12 * 3600 * 1000L))
        assertFalse(Section.Money.fresh(now - 25 * 3600 * 1000L))

        // People is fresh for 7 days
        assertTrue(Section.People.fresh(now - 3 * 24 * 3600 * 1000L))
        assertFalse(Section.People.fresh(now - 8 * 24 * 3600 * 1000L))
    }

    @Test
    fun libraryLineDataModel() {
        val line = LibraryLine("Overdue Fine", 50.0)
        assertEquals("Overdue Fine", line.label)
        assertEquals(50.0, line.amount!!, 0.0)
    }
}
