package com.iqra.chinese.alarm

import android.app.*
import android.content.*
import android.media.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.iqra.chinese.MainActivity
import com.iqra.chinese.R

const val CH_NOTIF      = "iqra_notif"
const val CH_ALARM      = "iqra_alarm"
const val NID           = 9001
const val ACTION_ALARM  = "com.iqra.chinese.STUDY_ALARM"
const val ACTION_SNOOZE = "com.iqra.chinese.SNOOZE"

fun alarmPhase(prefs: AlarmPrefs): Int {
    val elapsedH = ((System.currentTimeMillis() - prefs.todayStartMs) / 3_600_000).toInt()
    return if (elapsedH < 12) 0 else 1
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> AlarmScheduler.schedule(ctx)
            ACTION_ALARM                 -> handleAlarm(ctx)
        }
    }

    private fun handleAlarm(ctx: Context) {
        val iqra      = ctx.getSharedPreferences("iqra", Context.MODE_PRIVATE)
        val goalSecs  = iqra.getInt("goal_min", 30).coerceIn(5, 60) * 60
        val goalMet   = iqra.getInt("daily", 0) >= goalSecs

        if (goalMet) {
            AlarmScheduler.schedule(ctx)
            return
        }

        val phase = alarmPhase(AlarmPrefs(ctx))
        createChannels(ctx)

        if (phase == 0) {
            // Phase 0 (0–12 h): standard heads-up notification with snooze
            showNotification(ctx)
        } else {
            // Phase 1 (12–24 h): full clock-style alarm.
            // IMPORTANT: only the foreground service starts here — the service
            // itself launches AlarmActivity from onCreate(), because Android 10+
            // (and especially MIUI) blocks startActivity() calls made directly
            // from a BroadcastReceiver. A running foreground service is trusted
            // to start an activity, a plain receiver is not.
            AlarmSoundService.start(ctx)
        }

        AlarmScheduler.schedule(ctx)
    }

    private fun showNotification(ctx: Context) {
        val sp        = AlarmPrefs(ctx)
        val snoozeUsed = sp.getSnoozeUsed("snooze_p0")

        val openPi = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozePi = PendingIntent.getBroadcast(
            ctx, "snooze_p0".hashCode(),
            Intent(ctx, SnoozeReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra("snooze_key", "snooze_p0")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appIcon = android.graphics.BitmapFactory.decodeResource(
            ctx.resources, R.mipmap.ic_launcher
        )
        val nb = NotificationCompat.Builder(ctx, CH_NOTIF)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(appIcon)
            .setContentTitle("📚 Iqra Chinese — Study Reminder")
            .setContentText("加油！ You haven't hit today's study goal yet.")
            .setSubText("Iqra Chinese")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("This reminder is from Iqra Chinese. Keep your streak alive — tap to open the app. 🀄"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(openPi)
            .apply {
                if (!snoozeUsed) addAction(0, "⏱ Snooze 1 hour", snoozePi)
                addAction(0, "Open App ✓", openPi)
            }

        (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NID, nb.build())
    }

    private fun createChannels(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (nm.getNotificationChannel(CH_NOTIF) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CH_NOTIF, "Study Reminder",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 400, 200, 400)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
            )
        }
        if (nm.getNotificationChannel(CH_ALARM) == null) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            nm.createNotificationChannel(
                NotificationChannel(CH_ALARM, "Study Alarm",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 800, 200, 800, 200, 800)
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), attrs)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
            )
        }
    }
}

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val key = intent.getStringExtra("snooze_key") ?: "snooze_p0"
        AlarmPrefs(ctx).setSnoozeUsed(key, true)
        AlarmSoundService.stop(ctx)
        (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NID)
        AlarmScheduler.scheduleDelay(ctx, 3_600_000L)
    }
}
