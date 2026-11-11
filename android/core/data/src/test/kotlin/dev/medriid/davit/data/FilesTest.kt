package dev.medriid.davit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FilesTest {

    @Test
    fun sizeTextFormatsBytesKbMbCorrectly() {
        assertEquals("0 B", sizeText(0))
        assertEquals("500 B", sizeText(500))
        assertEquals("1023 B", sizeText(1023))
        assertEquals("1 KB", sizeText(1024))
        assertEquals("1.5 MB", sizeText(1572863))
        assertEquals("1.0 MB", sizeText(1048576))
        assertEquals("5.5 MB", sizeText(5767168))
    }

    @Test
    fun cleanNameSanitizesFilenamesAndAppendsExtensions() {
        assertEquals("notes.pdf", FileStore.cleanName("notes.pdf", "application/pdf"))
        assertEquals("notes.pdf", FileStore.cleanName("notes", "application/pdf"))
        assertEquals("file.pdf", FileStore.cleanName("", "application/pdf"))
        assertEquals("invalid_______file.txt", FileStore.cleanName("invalid:*?\"<>|file.txt", "text/plain"))
    }

    @Test
    fun isPdfChecksExtensionCaseInsensitively() {
        assertTrue(File("document.pdf").isPdf())
        assertTrue(File("DOCUMENT.PDF").isPdf())
        assertTrue(File("report.Pdf").isPdf())
        assertFalse(File("document.txt").isPdf())
        assertFalse(File("pdf").isPdf())
    }
}
