package dev.medriid.davit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BannerTest {

    @Test
    fun bannerJsonRoundTrip() {
        val banner = Banner(style = 2, photo = true, focus = 0.8f, nameInBar = true)
        val json = banner.toJson()
        val decoded = Banner.fromJson(json)

        assertEquals(banner.style, decoded.style)
        assertTrue(decoded.photo)
        assertEquals(0.8f, decoded.focus, 1e-4f)
        assertTrue(decoded.nameInBar)
        assertFalse(decoded.hidden)
    }

    @Test
    fun bannerFromJsonHandlesInvalidOrNull() {
        val defaultBanner = Banner()
        assertEquals(defaultBanner, Banner.fromJson(null))
        assertEquals(defaultBanner, Banner.fromJson("invalid json"))
    }

    @Test
    fun bannerStatLookup() {
        assertEquals(BannerStat.Attendance, BannerStat.of("attendance"))
        assertEquals(BannerStat.Exam, BannerStat.of("exam"))
        assertEquals(BannerStat.Cgpa, BannerStat.of("cgpa"))
        assertEquals(BannerStat.None, BannerStat.of("unknown"))
    }
}
