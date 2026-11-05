package dev.medriid.davit.design

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

enum class RisoIcon(@DrawableRes val resId: Int) {
    Today(R.drawable.ic_today),
    Attendance(R.drawable.ic_attendance),
    Academics(R.drawable.ic_academics),
    Week(R.drawable.ic_week),
    Profile(R.drawable.ic_profile),
    Map(R.drawable.ic_map),
    Marks(R.drawable.ic_marks),
    Assignments(R.drawable.ic_assignments),
    Exams(R.drawable.ic_exams),
    Grades(R.drawable.ic_grades),
    Materials(R.drawable.ic_materials),
    Faculty(R.drawable.ic_faculty),
    Bell(R.drawable.ic_bell),
    More(R.drawable.ic_more),
    Settings(R.drawable.ic_settings),
    Book(R.drawable.ic_book),
    Next(R.drawable.ic_next),
    Home(R.drawable.ic_home),
    Flag(R.drawable.ic_flag),
    Bubble(R.drawable.ic_bubble),
    Megaphone(R.drawable.ic_megaphone),
    People(R.drawable.ic_people),
    Certificate(R.drawable.ic_certificate),
    Pencil(R.drawable.ic_pencil),
    Eye(R.drawable.ic_eye),
    Receipt(R.drawable.ic_receipt),
    Gate(R.drawable.ic_gate),
    Trophy(R.drawable.ic_trophy),
    Tag(R.drawable.ic_tag),
    Download(R.drawable.ic_download),
    Picture(R.drawable.ic_picture);
}

@Composable
fun IconGlyph(icon: RisoIcon, color: Color = LocalContentColor.current, modifier: Modifier = Modifier.size(24.dp)) {
    Icon(
        painter = painterResource(icon.resId),
        contentDescription = null,
        modifier = modifier,
        tint = color,
    )
}

data class NavItem(val label: String, val icon: RisoIcon)
