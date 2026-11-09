package dev.medriid.davit.data

import dev.medriid.davit.vtop.Assignment
import dev.medriid.davit.vtop.AttendanceRow
import java.math.BigInteger
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.floor

fun titleCase(s: String) = s.split(' ').filter { it.isNotBlank() }.joinToString(" ") { w ->
    w.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
}

fun courseInitials(name: String): String {
    val words = name.split(' ', '-').filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words[0].take(3).uppercase()
        else -> words.mapNotNull { it.firstOrNull { c -> c.isLetterOrDigit() } }.joinToString("").take(4).uppercase()
    }
}

private val vtopDays = listOf(
    DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
    DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH),
)

/** VTOP's "15-Oct-2026" or "15-10-2026" (sometimes followed by a time); null for "-" or anything else. */
fun vtopDate(text: String): LocalDate? {
    val t = text.trim().substringBefore(' ')
    return vtopDays.firstNotNullOfOrNull { f -> runCatching { LocalDate.parse(t, f) }.getOrNull() }
}

fun Assignment.dueDay(): LocalDate? = vtopDate(due)

fun ago(at: Long): String {
    val mins = (System.currentTimeMillis() - at) / 60_000
    return when {
        mins < 1 -> "just now"
        mins < 60 -> "$mins min ago"
        mins < 1440 -> "${mins / 60} h ago"
        else -> "${mins / 1440} d ago"
    }
}

/** "8.5", "10", never "10.0". */
fun num(v: Double): String =
    if (v == floor(v)) v.toLong().toString() else "%.2f".format(Locale.ENGLISH, v).trimEnd('0').trimEnd('.')

fun fmtNum(v: Double): String = num(v)

private val FacultyTitle = Regex("""^(DR|PROF|MR|MRS|MS)\.?\s+""")
private val NotALetter = Regex("[^A-Z ]")
private val Spaces = Regex("\\s+")

/** "Dr. Asha  Raman" and "ASHA RAMAN" match: uppercase, titles and punctuation dropped. */
fun facultyKey(name: String): String =
    name.uppercase().replace(FacultyTitle, "").replace(NotALetter, " ").trim().replace(Spaces, " ")

fun isLabType(type: String) = "lab" in type.lowercase()

/** Identifies a course's attendance row across syncs: theory and lab of one course differ. */
val AttendanceRow.key: String get() = "$courseCode|$classType"

/** attended/total as a whole percentage, rounded up (ceil). */
val AttendanceRow.percent: Int
    get() = if (total == 0) 100 else (attended * 100 + total - 1) / total

/**
 * The mean of every course's exact attended/total, rounded up once at the end; each
 * course counts the same however many classes it has. Exact (a common denominator) so
 * the ceil can't slip; null when no course has held a class.
 */
fun overallPercent(rows: List<AttendanceRow>): Int? {
    val held = rows.filter { it.total > 0 }
    if (held.isEmpty()) return null
    var num = BigInteger.ZERO
    var den = BigInteger.ONE
    for ((_, _, _, attended, total) in held) {
        val t = total.toBigInteger()
        num = num * t + attended.toBigInteger() * den
        den *= t
    }
    val product = num * 100.toBigInteger()
    val divisor = den * held.size.toBigInteger()
    return ((product + divisor - 1.toBigInteger()) / divisor).toInt()
}

val AttendanceRow.atRisk: Boolean get() = percent < 75

/** Classes you can miss in a row from now and still sit at or above 75% (ceil). */
val AttendanceRow.canSkipNow: Int
    get() = if (atRisk) 0 else ((100 * attended - 74 * total - 1) / 74).coerceAtLeast(0)

/** Classes to attend in a row to climb back to 75% (ceil). */
val AttendanceRow.mustAttend: Int
    get() = if (!atRisk) 0 else ((74 * total - 100 * attended) / 26 + 1).coerceAtLeast(0)

/** The percentage (rounded up) after missing the next [n] classes. */
fun AttendanceRow.afterMissing(n: Int): Int =
    if (total + n == 0) 100 else (attended * 100 + total + n - 1) / (total + n)

fun AttendanceRow.hint(): String =
    if (atRisk) "attend the next $mustAttend to reach 75%"
    else "can skip $canSkipNow and stay at 75%"
