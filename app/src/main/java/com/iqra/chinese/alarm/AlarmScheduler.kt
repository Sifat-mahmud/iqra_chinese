package com.iqra.chinese.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmScheduler {

    fun schedule(ctx: Context) = scheduleDelay(ctx, 3_600_000L)

    fun scheduleDelay(ctx: Context, delayMs: Long) {
        val am      = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val trigger = System.currentTimeMillis() + delayMs

        // Save for Settings countdown
        ctx.getSharedPreferences("iqra_alarm", Context.MODE_PRIVATE)
            .edit().putLong("next_trigger_ms", trigger).apply()

        val alarmPi = pendingIntent(ctx)

        // setAlarmClock — highest OS priority, same API used by clock apps.
        // Shows alarm icon in status bar, bypasses Doze/MIUI battery restrictions.
        // showIntent is what opens when user taps the status bar alarm icon.
        val showIntent = PendingIntent.getActivity(
            ctx, 99,
            Intent(ctx, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        am.setAlarmClock(
            AlarmManager.AlarmClockInfo(trigger, showIntent),
            alarmPi
        )
    }

    fun msUntilNext(ctx: Context): Long {
        val saved = ctx.getSharedPreferences("iqra_alarm", Context.MODE_PRIVATE)
            .getLong("next_trigger_ms", -1L)
        if (saved == -1L) return -1L
        return (saved - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun cancel(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(ctx))
    }

    private fun pendingIntent(ctx: Context) = PendingIntent.getBroadcast(
        ctx, 42,
        Intent(ctx, AlarmReceiver::class.java).apply { action = ACTION_ALARM },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
