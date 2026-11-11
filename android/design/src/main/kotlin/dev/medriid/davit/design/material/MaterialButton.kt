package dev.medriid.davit.design.material

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.InkPairs
import dev.medriid.davit.design.RisoTheme

@Composable
fun MaterialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    quiet: Boolean = false,
    fillWidth: Boolean = false,
    compact: Boolean = false,
) {
    val contentPadding = if (compact) {
        PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    } else {
        ButtonDefaults.ContentPadding
    }
    val minHeight = if (compact) 32.dp else if (fillWidth) 50.dp else 40.dp
    val mModifier = modifier
        .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
        .defaultMinSize(minHeight = minHeight)

    if (quiet) {
        OutlinedButton(
            onClick = onClick,
            modifier = mModifier,
            enabled = enabled,
            contentPadding = contentPadding,
        ) {
            Text(text)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = mModifier,
            enabled = enabled,
            contentPadding = contentPadding,
        ) {
            Text(text)
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MaterialButtonPreview() {
    val inks = InkPairs.Material3
    RisoTheme(inks = inks) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.background(color = MaterialTheme.colorScheme.surface).padding(16.dp)
        ) {
            // Standard Filled Button
            MaterialButton(
                text = "Filled Button",
                onClick = {},
                fillWidth = true
            )

            // Standard Filled Button (Disabled)
            MaterialButton(
                text = "Filled Disabled",
                onClick = {},
                enabled = false,
                fillWidth = true
            )

            // Quiet / Outlined Button
            MaterialButton(
                text = "Outlined Button",
                onClick = {},
                quiet = true,
                fillWidth = true
            )

            // Quiet / Outlined Button (Disabled)
            MaterialButton(
                text = "Outlined Disabled",
                onClick = {},
                enabled = false,
                quiet = true,
                fillWidth = true
            )

            // Compact Button
            MaterialButton(
                text = "Compact Button",
                onClick = {},
                compact = true
            )
        }
    }
}
