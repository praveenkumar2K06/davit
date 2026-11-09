package dev.medriid.davit.data

data class FacultyReply(val text: String, val date: String)

data class FacultyReview(
    val rating: Int = 0,
    val text: String = "",
    val course: String = "",
    val courseName: String = "",
    val date: String = "",
    /** "site" for written on No8AM, "reddit" or "compiled" for gathered from elsewhere. */
    val source: String = "site",
    val paraphrased: Boolean = false,
    /** Grade to the mark it took, "s" to "f", where the reviewer gave them. */
    val cutoffs: Map<String, String> = emptyMap(),
    val classStrength: String? = null,
    val classMean: String? = null,
    val classSd: String? = null,
    val replies: List<FacultyReply> = emptyList(),
)

data class ReviewedFaculty(
    val id: String,
    val empId: String = "",
    val name: String = "",
    val department: String = "",
    val designation: String = "",
    val campus: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val reviews: List<FacultyReview> = emptyList(),
)
