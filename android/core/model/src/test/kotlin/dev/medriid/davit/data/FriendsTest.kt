package dev.medriid.davit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FriendsTest {

    @Test
    fun blockOfExtractsLeadingLetters() {
        assertEquals("SJT", blockOf("SJT-401"))
        assertEquals("PRP", blockOf("PRP231"))
        assertEquals("MB", blockOf("MB-210"))
        assertEquals("TT", blockOf("TT G04"))
        assertEquals("", blockOf("401"))
    }

    @Test
    fun weekCodeEncodeAndDecodeRoundTrip() {
        val classes = listOf(SharedClass("A1+TA1", "SJT"), SharedClass("L31+L32", "PRP"))
        val encoded = WeekCode.encode("Alice", classes)
        assertTrue(encoded.startsWith("DAVIT1:Alice:"))

        val decoded = WeekCode.decode(encoded)
        assertNotNull(decoded)
        assertEquals("Alice", decoded!!.first)
        assertEquals(classes, decoded.second)
    }

    @Test
    fun weekCodeDecodeInvalidInputReturnsNull() {
        assertNull(WeekCode.decode("INVALID_PREFIX"))
        assertNull(WeekCode.decode("DAVIT1::"))
    }

    @Test
    fun friendsJsonSerializationRoundTrip() {
        val friendList = listOf(
            Friend(1L, "Bob", listOf(SharedClass("B1", "SJT"))),
            Friend(2L, "Charlie", listOf(SharedClass("C1", "MB"))),
        )

        val json = friendsToJson(friendList)
        val decoded = friendsFromJson(json)

        assertEquals(2, decoded.size)
        assertEquals(1L, decoded[0].id)
        assertEquals("Bob", decoded[0].name)
        assertEquals(listOf(SharedClass("B1", "SJT")), decoded[0].classes)
        assertEquals("Charlie", decoded[1].name)
    }
}
