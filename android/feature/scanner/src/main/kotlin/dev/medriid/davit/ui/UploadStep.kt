package dev.medriid.davit.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.medriid.davit.data.LocalRepo
import dev.medriid.davit.data.UploadState
import dev.medriid.davit.data.UploadTarget
import dev.medriid.davit.data.sizeText
import dev.medriid.davit.design.HalftoneBar
import dev.medriid.davit.design.Heading
import dev.medriid.davit.design.InkButton
import dev.medriid.davit.design.InkCard
import dev.medriid.davit.design.InkField
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.design.rememberHaptics
import dev.medriid.davit.design.rememberMotionEnabled
import dev.medriid.davit.vtop.UserConsent

/**
 * One step of [target]'s upload after a file is picked: the confirm (the only thing that
 * sends), progress, VTOP's emailed code for a replacement, done, or what went wrong. The
 * assignment screen and the scanner both show these. [onPick] and [onScan] offer another
 * file where that makes sense; [onClose] runs when you close it (it clears the step).
 */
@Composable
fun UploadStep(
    target: UploadTarget,
    st: UploadState,
    onPick: (() -> Unit)? = null,
    onScan: (() -> Unit)? = null,
    onClose: () -> Unit = {},
) {
    val repo = LocalRepo.current
    val haptics = rememberHaptics()
    val close = {
        repo.uploads.clear(target.key)
        onClose()
    }
    when (st) {
        is UploadState.Ready -> InkCard(Modifier.fillMaxWidth(), plateOffset = 4.dp) {
            InkText("Upload this?", label = true)
            Spacer(Modifier.height(8.dp))
            InkText(st.file.name, style = RisoType.title, maxLines = 3)
            InkText("${sizeText(st.file.size.toLong())} · as \"${target.title}\"", style = RisoType.small)
            st.file.note?.let { InkText(it, style = RisoType.small.copy(fontWeight = FontWeight.SemiBold)) }
            if (target.replacing) {
                Spacer(Modifier.height(8.dp))
                InkText("This replaces the file you uploaded${target.updated?.let { " on $it" } ?: ""}. VTOP will email you a code to confirm it.", style = RisoType.small)
            }
            Spacer(Modifier.height(14.dp))
            InkButton(
                if (target.replacing) "Yes, replace it" else "Yes, upload",
                {
                    haptics.heavy()
                    repo.uploads.send(target, UserConsent("upload ${target.code}"))
                },
                fillWidth = true, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (onPick != null) InkButton("Another file", onPick, quiet = true, modifier = Modifier.weight(1f), fillWidth = true)
                InkButton("Cancel", close, quiet = true, modifier = Modifier.weight(1f), fillWidth = true)
            }
        }
        is UploadState.Sending -> InkCard(Modifier.fillMaxWidth(), plateOffset = 4.dp) {
            InkText(st.stage, style = RisoType.body.copy(fontWeight = FontWeight.SemiBold), maxLines = 2)
            Spacer(Modifier.height(10.dp))
            HalftoneBar(st.fraction ?: 0.35f, height = 12.dp)
            Spacer(Modifier.height(8.dp))
            InkText(st.fraction?.let { "${(it * 100).toInt()}%" } ?: "Waiting for VTOP", style = RisoType.small)
        }
        is UploadState.Otp -> OtpCard(st, onSubmit = { code ->
            haptics.heavy()
            repo.uploads.submitOtp(target.key, UserConsent("upload otp ${target.code}"), target.classId, target.code, code)
        }, onCancel = {
            haptics.tick()
            repo.uploads.cancel(target.key, target.classId)
            onClose()
        })
        is UploadState.Done -> DoneCard(st, close)
        is UploadState.Failed -> InkCard(Modifier.fillMaxWidth(), plate = LocalInks.current.a, plateOffset = 4.dp) {
            InkText("Not uploaded", label = true)
            Spacer(Modifier.height(8.dp))
            InkText(st.message)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (onPick != null) InkButton("Choose", onPick, modifier = Modifier.weight(1f), fillWidth = true)
                if (onScan != null) InkButton("Scan", onScan, quiet = true, modifier = Modifier.weight(1f), fillWidth = true)
                InkButton("Close", close, quiet = true, modifier = Modifier.weight(1f), fillWidth = true)
            }
        }
    }
}

@Composable
private fun OtpCard(st: UploadState.Otp, onSubmit: (String) -> Unit, onCancel: () -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    InkCard(Modifier.fillMaxWidth(), kind = dev.medriid.davit.design.Kind.Assignments, plateOffset = 4.dp) {
        InkText("Check your email", label = true)
        Spacer(Modifier.height(8.dp))
        InkText(
            "VTOP emailed a 6-digit code to ${st.email.ifBlank { "your VIT email" }}. Enter it to replace your file with ${st.file}. Until then your old file stays.",
            style = RisoType.small,
        )
        Spacer(Modifier.height(12.dp))
        InkField(
            code, { code = it.filter(Char::isDigit).take(6) }, "6-digit code",
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )
        st.error?.let {
            Spacer(Modifier.height(8.dp))
            InkText(it, style = RisoType.small.copy(color = LocalInks.current.a))
        }
        Spacer(Modifier.height(14.dp))
        InkButton("Confirm code", { onSubmit(code) }, enabled = code.length == 6, fillWidth = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        InkButton("Cancel, keep the old file", onCancel, quiet = true, fillWidth = true, modifier = Modifier.fillMaxWidth())
    }
}

/** "Uploaded", stamped: it lands big and settles, like a rubber stamp. */
@Composable
private fun DoneCard(st: UploadState.Done, onClose: () -> Unit) {
    val inks = LocalInks.current
    val motion = rememberMotionEnabled()
    val stamp = remember { Animatable(if (motion) 1.6f else 1f) }
    LaunchedEffect(Unit) { if (motion) stamp.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 520f)) }
    InkCard(Modifier.fillMaxWidth(), plate = inks.b, plateOffset = 4.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                InkText(if (st.replaced) "Replaced" else "Uploaded", label = true)
                Spacer(Modifier.height(6.dp))
                InkText("VTOP said \"Uploaded successfully\".", style = RisoType.body.copy(fontWeight = FontWeight.SemiBold))
                st.updated?.let { InkText("Last updated $it", style = RisoType.small) }
            }
            Heading(
                "Done", 30.sp, color = inks.a,
                modifier = Modifier.graphicsLayer {
                    scaleX = stamp.value
                    scaleY = stamp.value
                    rotationZ = -6f
                    alpha = (2f - stamp.value).coerceIn(0f, 1f)
                },
            )
        }
        Spacer(Modifier.height(12.dp))
        InkButton("OK", onClose, quiet = true, fillWidth = true, modifier = Modifier.fillMaxWidth())
    }
}
