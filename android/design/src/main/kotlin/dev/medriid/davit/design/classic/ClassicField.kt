package dev.medriid.davit.design.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.medriid.davit.design.AppStyle
import dev.medriid.davit.design.CardShape
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.design.soft.SoftShape

@Composable
internal fun ClassicField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    secret: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    inks: Inks,
) {
    var focused by remember { mutableStateOf(false) }
    var reveal by remember { mutableStateOf(false) }
    val shape = if (inks.style == AppStyle.Soft) SoftShape else CardShape

    Column(modifier) {
        InkText(label, label = true)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .clip(shape)
                .background(inks.paper)
                .border(if (focused) 3.dp else 2.dp, if (focused) inks.a else inks.text, shape)
                .padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = RisoType.body.copy(fontSize = 18.sp, color = inks.text),
                cursorBrush = SolidColor(inks.a),
                visualTransformation = if (secret && !reveal) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp)
                    .onFocusChanged { focused = it.isFocused },
            )
            if (secret) {
                Box(
                    Modifier
                        .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)
                        .clickable(role = Role.Button) { reveal = !reveal },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        if (reveal) "HIDE" else "SHOW",
                        style = RisoType.label.copy(color = inks.text),
                    )
                }
            }
        }
    }
}
