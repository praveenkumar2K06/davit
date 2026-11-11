package dev.medriid.davit.design.material

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.RisoTheme

val MaterialShape = RoundedCornerShape(16.dp)

@Composable
fun MaterialCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    shape: Shape = MaterialShape,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val fill = containerColor ?: MaterialTheme.colorScheme.surfaceVariant
    if (onClick != null) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = fill,
            ),
            shape = shape,
            onClick = onClick,
            modifier = modifier,
        ) {
            Column(Modifier.padding(contentPadding)) { content() }
        }
    } else {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = fill,
            ),
            shape = shape,
            modifier = modifier,
        ) {
            Column(Modifier.padding(contentPadding)) { content() }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MaterialCardPreview() {
    val inks = InkPairs.Material3
    RisoTheme(inks = inks) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.background(color = MaterialTheme.colorScheme.surface).padding(16.dp),
        ) {
            MaterialCard {
                Text(text = "Static Material Card")
            }
            MaterialCard(onClick = {}) {
                Text(text = "Clickable Material Card")
            }
        }
    }
}


