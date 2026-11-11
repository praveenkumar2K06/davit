package dev.medriid.davit.ui

import androidx.compose.runtime.toMutableStateList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteTest {
    @Test
    fun aPdfRouteSurvivesItsId() {
        val route = Route.PdfView("cert:/events/x|2026|ABC", "/data/user/0/app/cache/previews/a b/Notes (1).pdf", "Module 3 | Trees & graphs")
        assertEquals(route, Route.parse(route.id))
    }

    @Test
    fun allPlainRoutesParse() {
        val plainRoutes = listOf(
            Route.Search, Route.Campus, Route.Messages, Route.Spotlight, Route.Events,
            Route.People, Route.Money, Route.Gate, Route.Curriculum, Route.SemesterGrades,
            Route.Hostel, Route.Downloads, Route.Settings, Route.Notifications, Route.Bonafide,
            Route.CourseFeedback, Route.PaperSeeing, Route.Activities, Route.Allocation, Route.FfcsResult,
            Route.Colours, Route.LeavePlanner, Route.DisplayCard, Route.CourseNames, Route.Corrections,
            Route.ExamBoard, Route.Friends, Route.Planner, Route.MessMenu, Route.FacultyReviews,
            Route.Circulars, Route.Windows, Route.Projects, Route.Rules, Route.OtherExams,
            Route.Certificates, Route.Sports, Route.ClubDirectory, Route.SignIns,
        )
        for (r in plainRoutes) {
            assertEquals(r, Route.parse(r.id))
        }
    }

    @Test
    fun parameterizedRoutesParse() {
        val assignment = Route.Assignment("VL2026", "ETH")
        assertEquals(assignment, Route.parse(assignment.id))

        val course = Route.Course("VL1|ETH|CSE1001")
        assertEquals(course, Route.parse(course.id))

        val settingsPage = Route.SettingsPage("look")
        assertEquals(settingsPage, Route.parse(settingsPage.id))

        val facultyReview = Route.FacultyReview("V1001")
        assertEquals(facultyReview, Route.parse(facultyReview.id))
    }

    @Test
    fun malformedOrUnknownRoutesReturnNull() {
        assertNull(Route.parse("unknown_route_id"))
        assertNull(Route.parse("a|onlyone"))
        assertNull(Route.parse("pdf|a|b"))
        assertNull(Route.parse(""))
    }

    @Test
    fun navStackOperations() {
        var tabSelected = -1
        val stack = mutableListOf<Route>(Route.Search).toMutableStateList()
        val nav = Nav(stack) { tabSelected = it }

        assertEquals(Route.Search, nav.top)

        // Push new route
        nav.go(Route.Settings)
        assertEquals(Route.Settings, nav.top)
        assertEquals(2, stack.size)

        // Duplicate push top route should be ignored
        nav.go(Route.Settings)
        assertEquals(2, stack.size)

        // Back pops top
        assertTrue(nav.back())
        assertEquals(Route.Search, nav.top)

        // Back on root pops and returns true
        assertTrue(nav.back())
        assertNull(nav.top)

        // Back on empty returns false
        assertFalse(nav.back())

        // Tab clears stack and fires callback
        nav.go(Route.Campus)
        nav.tab(2)
        assertTrue(stack.isEmpty())
        assertEquals(2, tabSelected)
    }
}
