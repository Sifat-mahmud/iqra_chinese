package com.iqra.chinese.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.iqra.chinese.R

/**
 * Foreground service that plays the looping alarm sound.
 * Started by AlarmActivity, stopped by Snooze/Stop actions.
 * Using a foreground service means the sound keeps playing even if
 * the user navigates away from AlarmActivity.
 */
class AlarmSoundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        const val ACTION_STOP  = "com.iqra.chinese.ALARM_STOP"
        const val CH_SVC       = "iqra_alarm_svc"

        fun start(ctx: Context) {
            val i = Intent(ctx, AlarmSoundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ctx.startForegroundService(i)
            else
                ctx.startService(i)
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, AlarmSoundService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(9002, buildNotification())
        playAlarm()
        startVibration()
        launchAlarmGui()
    }

    /**
     * Launches the full-screen AlarmActivity GUI so the user sees a clear
     * "study now" screen instead of just hearing sound in the background.
     *
     * Called from a running foreground service context (this), which Android
     * trusts to start activities — unlike a plain BroadcastReceiver, which
     * Android 10+ (and MIUI especially) silently blocks from doing so.
     */
    private fun launchAlarmGui() {
        try {
            val intent = Intent(this, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Some OEMs may still block this — full-screen notification below
            // is the fallback (user taps it to open AlarmActivity manually).
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) stopSelf()
        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun playAlarm() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                setDataSource(applicationContext, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            // If MediaPlayer fails, try Ringtone API
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone = RingtoneManager.getRingtone(applicationContext, uri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ringtone.isLooping = true
                ringtone.play()
            } catch (_: Exception) {}
        }
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 800, 400, 800, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val appIcon = android.graphics.BitmapFactory.decodeResource(
            resources, R.mipmap.ic_launcher
        )
        return NotificationCompat.Builder(this, CH_SVC)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(appIcon)   // shows the Iqra Chinese app icon prominently — makes the
                                     // sound's source unmistakable even before any screen opens
            .setContentTitle("⏰ Iqra Chinese — Study Alarm")
            .setContentText("快学习！ Tap to open and stop the alarm")
            .setSubText("Iqra Chinese")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("This alarm is from Iqra Chinese. You haven't completed today's " +
                        "study goal. Tap to open the app and stop the sound."))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(openPi)
            // Official Android mechanism for alarm-clock-style apps: shows the
            // activity full-screen even over the lock screen. This is the
            // documented, OEM-respected way to surface a GUI from a background
            // trigger — used as a robust companion to the direct startActivity()
            // call above (which some OEMs may still suppress).
            .setFullScreenIntent(openPi, true)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CH_SVC) != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        // IMPORTANCE_HIGH is required for setFullScreenIntent() to actually
        // trigger — IMPORTANCE_LOW (the old setting) silently suppresses it.
        val ch = NotificationChannel(CH_SVC, "Alarm Sound Service",
            NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Full-screen study alarm with sound"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 800, 400, 800, 400)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), attrs)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(ch)
    }
}
