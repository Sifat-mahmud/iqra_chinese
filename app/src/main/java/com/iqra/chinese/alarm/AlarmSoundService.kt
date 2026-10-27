package com.iqra.chinese.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.iqra.chinese.R

/**
 * Foreground service that:
 * 1. Plays looping alarm sound
 * 2. Shows a compact overlay window over ANY app or lock screen
 *    (requires SYSTEM_ALERT_WINDOW permission — prompted in MainActivity on first run)
 * 3. Tries to launch the full AlarmActivity as a fallback
 */
class AlarmSoundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    companion object {
        const val ACTION_STOP = "com.iqra.chinese.ALARM_STOP"
        const val CH_SVC      = "iqra_alarm_svc"

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
        showOverlay()          // compact widget over any app / lock screen
        launchAlarmActivity()  // also try full-screen activity as secondary path
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) stopSelf()
        return START_STICKY
    }

    override fun onDestroy() {
        removeOverlay()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    // ── Overlay ────────────────────────────────────────────────────────────────

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            android.util.Log.w("AlarmSoundService", "Overlay skipped — SYSTEM_ALERT_WINDOW permission not granted")
            return
        }

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 80   // small gap from top so it doesn't hide status bar
        }

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.overlay_alarm, null)

        // Subtext: show goal info
        val iqraPrefs = getSharedPreferences("iqra", Context.MODE_PRIVATE)
        val goalMin = iqraPrefs.getInt("goal_min", 30).coerceIn(5, 60)
        val doneSecs = iqraPrefs.getInt("daily", 0)
        val doneMin = doneSecs / 60
        view.findViewById<TextView>(R.id.tvOverlaySubtext).text =
            "Studied $doneMin / $goalMin min today"

        // Snooze button
        val alarmPrefs = AlarmPrefs(this)
        val snoozeUsed = alarmPrefs.getSnoozeUsed("snooze_p1")
        val btnSnooze = view.findViewById<Button>(R.id.btnOverlaySnooze)
        if (snoozeUsed) {
            btnSnooze.isEnabled = false
            btnSnooze.alpha = 0.4f
            btnSnooze.text = "No snooze"
        } else {
            btnSnooze.setOnClickListener {
                alarmPrefs.setSnoozeUsed("snooze_p1", true)
                AlarmScheduler.scheduleDelay(this, 3_600_000L)
                stopSelf()
            }
        }

        // Stop button — opens app and stops alarm
        view.findViewById<Button>(R.id.btnOverlayStop).setOnClickListener {
            val appIntent = Intent(this, com.iqra.chinese.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(appIntent)
            stopSelf()
        }

        // Make overlay draggable so user can move it if it blocks their screen
        var startX = 0f; var startY = 0f
        var initialParamsX = 0; var initialParamsY = 0
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX; startY = event.rawY
                    initialParamsX = params.x; initialParamsY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = (initialParamsX + (event.rawX - startX)).toInt()
                    params.y = (initialParamsY + (event.rawY - startY)).toInt()
                    try { wm.updateViewLayout(view, params) } catch (_: Exception) {}
                    true
                }
                else -> false
            }
        }

        try {
            wm.addView(view, params)
            overlayView = view
            android.util.Log.i("AlarmSoundService", "Overlay added successfully")
        } catch (e: Exception) {
            android.util.Log.e("AlarmSoundService", "Failed to add overlay: ${e.message}", e)
        }
    }

    private fun removeOverlay() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
        } catch (_: Exception) {}
        overlayView = null
        windowManager = null
    }

    // ── Activity launch (secondary path) ──────────────────────────────────────

    private fun launchAlarmActivity() {
        try {
            startActivity(Intent(this, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        } catch (_: Exception) {}
    }

    // ── Audio ──────────────────────────────────────────────────────────────────

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
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone = RingtoneManager.getRingtone(applicationContext, uri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ringtone.isLooping = true
                ringtone.play()
            } catch (_: Exception) {}
        }
    }

    // ── Vibration ──────────────────────────────────────────────────────────────

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 800, 400, 800, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    // ── Notification ───────────────────────────────────────────────────────────

    private fun buildNotification(): Notification {
        val intent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val appIcon = android.graphics.BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        return NotificationCompat.Builder(this, CH_SVC)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(appIcon)
            .setContentTitle("⏰ Iqra Chinese — Study Alarm")
            .setContentText("快学习！ Tap to open and stop the alarm")
            .setSubText("Iqra Chinese")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("This alarm is from Iqra Chinese. You haven't completed today's study goal. Tap to stop."))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(openPi)
            .setFullScreenIntent(openPi, true)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CH_SVC) != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
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
