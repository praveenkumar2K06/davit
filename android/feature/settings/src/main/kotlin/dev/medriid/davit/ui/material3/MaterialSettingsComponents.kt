package dev.medriid.davit.ui.material3

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.design.IconGlyph
import dev.medriid.davit.design.RisoIcon
import dev.medriid.davit.design.material.MaterialButton

/** Header for a group of settings in Material 3 screens. */
@Composable
fun MaterialSettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: RisoIcon? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 18.dp, bottom = 8.dp),
    ) {
        if (icon != null) {
            IconGlyph(icon, MaterialTheme.colorScheme.primary, Modifier.size(20.dp))
            Spacer(Modifier.padding(end = 8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** Segmented list item for setting rows. */
@Composable
fun MaterialSegmentedRow(
    title: String,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    body: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    if (onClick != null) {
        SegmentedListItem(
            colors = ListItemDefaults.segmentedColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
            leadingContent = leadingContent,
            content = {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            },
            supportingContent = body?.let {
                {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            trailingContent = trailingContent,
            verticalAlignment = Alignment.CenterVertically,
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
        )
    } else {
        SegmentedListItem(
            colors = ListItemDefaults.segmentedColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
            leadingContent = leadingContent,
            content = {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            },
            supportingContent = body?.let {
                {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            verticalAlignment = Alignment.CenterVertically,
            trailingContent = trailingContent,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
        )
    }
}

/** Segmented list item with a trailing switch. */
@Composable
fun MaterialSegmentedToggleRow(
    title: String,
    checked: Boolean,
    index: Int,
    count: Int,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    MaterialSegmentedRow(
        title = title,
        body = body,
        index = index,
        count = count,
        leadingContent = leadingContent,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
    )
}

/** Segmented list item with a trailing action button. */
@Composable
fun MaterialSegmentedActionRow(
    title: String,
    action: String,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    MaterialSegmentedRow(
        title = title,
        body = body,
        index = index,
        count = count,
        leadingContent = leadingContent,
        trailingContent = {
            MaterialButton(
                text = action,
                onClick = onClick,
                quiet = true,
            )
        },
        onClick = onClick,
        modifier = modifier,
    )
}
