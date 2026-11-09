package dev.medriid.davit.sync

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import dev.medriid.davit.data.Alert
import dev.medriid.davit.data.AlertChannel

/**
 * Posts [Alert]s, each at most once: keys already sent are remembered (the last few
 * hundred). Three or more in one channel collapse into a single summary.
 */
object Notifier {
    private const val PREFS = "davit.alerts"
    private const val K_SENT = "sent"
    private const val KEEP = 400
    private const val SCARLET = 0xFFF65058.toInt()

    fun ensureChannels(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        AlertChannel.entries.forEach {
            nm.createNotificationChannel(
                NotificationChannelCompat.Builder(it.id, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                    .setName(it.title)
                    .build()
            )
        }
    }

    fun permitted(context: Context): Boolean =
        (Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
            NotificationManagerCompat.from(context).areNotificationsEnabled()

    @android.annotation.SuppressLint("MissingPermission")
    fun post(context: Context, alerts: List<Alert>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val sent = prefs.getString(K_SENT, "").orEmpty().lines().filter { it.isNotEmpty() }
        val fresh = alerts.filter { it.key !in sent }.distinctBy { it.key }
        if (fresh.isEmpty() || !permitted(context)) return
        val nm = NotificationManagerCompat.from(context)
        fresh.groupBy { it.channel }.forEach { (channel, list) ->
            try {
                if (list.size >= 3) {
                    val style = NotificationCompat.InboxStyle()
                    list.forEach { style.addLine("${it.title} · ${it.text}") }
                    nm.notify(channel.id.hashCode(), builder(context, channel, "${list.size} updates", list.first().title).setStyle(style).build())
                } else {
                    list.forEach {
                        nm.notify(it.key.hashCode(), builder(context, channel, it.title, it.text)
                            .setStyle(NotificationCompat.BigTextStyle().bigText(it.text)).build())
                    }
                }
            } catch (_: SecurityException) {
                // Permission revoked between the check and the post; nothing to do.
            }
        }
        prefs.edit {
            putString(
                K_SENT,
                (sent + fresh.map { it.key }).takeLast(KEEP).joinToString("\n"))}
    }

    /** Sign-out: forget what was sent, so a new account starts clean. */
    fun forget(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { clear() }
        NotificationManagerCompat.from(context).cancelAll()
    }

    private fun builder(context: Context, channel: AlertChannel, title: String, text: String): NotificationCompat.Builder {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(Intent.ACTION_MAIN).apply { setPackage(context.packageName) }
        val open = PendingIntent.getActivity(
            context, 0,
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val iconRes = context.resources.getIdentifier("ic_stat_davit", "drawable", context.packageName).takeIf { it != 0 }
            ?: context.applicationInfo.icon
        return NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(iconRes)
            .setColor(SCARLET)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
    }
}
