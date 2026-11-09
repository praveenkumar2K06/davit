package dev.medriid.davit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.CardShape
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.InkCard
import dev.medriid.davit.design.InkChip
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.Inks
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.design.RisoType

/** A titled card of settings, with an optional glyph by the title. */
@Composable
fun SettingsCard(title: String, icon: RisoIcon? = null, content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.height(18.dp))
    InkCard(Modifier.fillMaxWidth(), plateOffset = 4.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                IconGlyph(icon, LocalInks.current.a, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            InkText(title, label = true)
        }
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
fun ToggleRow(title: String, body: String, on: Boolean, onToggle: () -> Unit) {
    val inks = LocalInks.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch, onClick = onToggle)
            .semantics { stateDescription = if (on) "On" else "Off" }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            InkText(title, style = RisoType.body.copy(fontWeight = FontWeight.SemiBold))
            InkText(body, style = RisoType.small)
        }
        Spacer(Modifier.width(12.dp))
        if (inks.material) {
            Switch(
                checked = on,
                onCheckedChange = { onToggle() },
            )
        } else {
            InkChip(if (on) "On" else "Off", solid = on)
        }
    }
}

@Composable
fun SettingRow(title: String, body: String, action: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            InkText(title, style = RisoType.body.copy(fontWeight = FontWeight.SemiBold))
            InkText(body, style = RisoType.small)
        }
        Spacer(Modifier.width(12.dp))
        InkChip(action, solid = true)
    }
}

@Composable
internal fun Radio(on: Boolean) {
    val inks = LocalInks.current
    Box(
        Modifier
            .size(18.dp)
            .border(2.dp, inks.text, CircleShape)
            .padding(4.dp)
            .then(if (on) Modifier.background(inks.a, CircleShape) else Modifier),
    )
}

/** VTOP's profile photo, decoded once, in a small ink-framed square. */
@Composable
internal fun VtopPhoto(dataUri: String) {
    val inks = LocalInks.current
    val bitmap = remember(dataUri) {
        runCatching {
            val bytes = android.util.Base64.decode(dataUri.substringAfter(','), android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    } ?: return
    Image(
        bitmap, contentDescription = "Your VTOP photo",
        contentScale = ContentScale.Crop,
        modifier = Modifier.padding(end = 14.dp).size(48.dp).clip(CardShape).border(2.dp, inks.text, CardShape),
    )
}

/** Two overlapping ink discs; the overlap is whatever multiply makes of them. */
@Composable
fun InkSwatch(pair: Inks, selected: Boolean, onClick: () -> Unit) {
    val current = LocalInks.current
    Box(
        Modifier
            .size(56.dp)
            .border(if (selected) 2.5.dp else 1.dp, if (selected) current.text else current.rule, CircleShape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = pair.name
                this.selected = selected
            }
            .padding(11.dp),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension * 0.34f
            drawCircle(pair.a, r, Offset(r, r), blendMode = current.blend)
            drawCircle(pair.b, r, Offset(size.width - r, size.height - r), blendMode = current.blend)
        }
    }
}
