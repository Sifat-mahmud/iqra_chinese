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
            Intent.ACTION_BOOT_COMPLETED -> AlarmScheduler.scheduleWeek(ctx)
            ACTION_ALARM                 -> handleAlarm(ctx, intent)
        }
    }

    private fun handleAlarm(ctx: Context, intent: Intent) {
        val iqra     = ctx.getSharedPreferences("iqra", Context.MODE_PRIVATE)
        val goalSecs = iqra.getInt("goal_min", 30).coerceIn(5, 60) * 60

        // ── Date-aware goal check ────────────────────────────────────────────
        // CRITICAL: "daily" seconds only resets to 0 when the app itself runs
        // touchStreak() (on app open). If the user never reopens the app, that
        // stale value — and whatever goal-met state it implies — would persist
        // forever, permanently silencing the alarm even on a brand new day
        // where zero studying has happened. Fix: compare the stored date the
        // "daily" counter applies to against TODAY's date, independent of
        // whether the app has run. If they don't match, today's effective
        // study time is 0 — regardless of what the stale counter says.
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        // Must match Repository.touchStreak()'s day boundary EXACTLY: the study
        // day rolls over at the user's configured day-start time, not midnight.
        val todayStr = fmt.format(java.util.Date(AlarmPrefs(ctx).todayStartMs))
        val storedDate     = iqra.getString("ldate", "") ?: ""
        val effectiveSecs  = if (storedDate == todayStr) iqra.getInt("daily", 0) else 0
        val goalMet        = effectiveSecs >= goalSecs

        createChannels(ctx)

        if (!goalMet) {
            val phase = alarmPhase(AlarmPrefs(ctx))
            if (phase == 0) showNotification(ctx)
            else            AlarmSoundService.start(ctx)
        }

        // Extend this exact hourly slot 7 days further out — keeps the
        // rolling window topped up forever, purely via system alarm
        // broadcasts, with no app process required.
        val code = intent.getIntExtra("slot_code", -1)
        if (code != -1) {
            AlarmScheduler.extendSlot(ctx, code, System.currentTimeMillis())
        }
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
