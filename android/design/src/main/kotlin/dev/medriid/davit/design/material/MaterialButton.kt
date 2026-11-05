package dev.medriid.davit.design.material

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.RisoType

@Composable
internal fun MaterialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    quiet: Boolean = false,
    fillWidth: Boolean = false,
    inks: Inks,
) {
    val mModifier = modifier
        .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
        .defaultMinSize(minHeight = 54.dp)
    val textColor = if (!enabled) inks.text.copy(alpha = 0.38f) else if (quiet) inks.a else inks.onA
    if (quiet) {
        OutlinedButton(
            onClick = onClick,
            modifier = mModifier,
            enabled = enabled,
        ) {
            Text(text)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = mModifier,
            enabled = enabled,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = inks.a,
                contentColor = textColor,
                disabledContainerColor = inks.rule,
            ),
        ) {
            BasicText(
                text,
                style = RisoType.button.copy(
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                ),
            )
        }
    }
}
