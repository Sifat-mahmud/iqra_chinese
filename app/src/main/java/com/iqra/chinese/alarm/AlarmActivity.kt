package com.iqra.chinese.alarm

import android.os.*
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.iqra.chinese.R

/**
 * Full-screen activity that appears over the lock screen when the alarm fires.
 * Works like a clock alarm — screen turns on, activity shown over lock screen.
 * User must tap Snooze (once) or Study Now to dismiss.
 */
class AlarmActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn screen on and show over lock screen (API 27+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KEYGUARD_SERVICE) as android.app.KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        setContentView(R.layout.activity_alarm)

        val alarmPrefs = AlarmPrefs(this)
        val phase      = alarmPhase(alarmPrefs)
        val snoozeKey  = if (phase == 0) "snooze_p0" else "snooze_p1"
        val snoozeUsed = alarmPrefs.getSnoozeUsed(snoozeKey)

        val tvTitle   = findViewById<TextView>(R.id.tvAlarmTitle)
        val tvMsg     = findViewById<TextView>(R.id.tvAlarmMsg)
        val btnSnooze = findViewById<Button>(R.id.btnAlarmSnooze)
        val btnStop   = findViewById<Button>(R.id.btnAlarmStop)

        if (phase == 0) {
            tvTitle.text = "📚 Study Reminder"
            tvMsg.text   = "You haven't completed 30 minutes of practice today.\nStudy now to keep your streak alive! 🔥"
        } else {
            tvTitle.text = "⏰ STUDY NOW — 快学习！"
            tvMsg.text   = "Over 12 hours without 30 min of practice.\nComplete your goal to silence this alarm."
        }

        // Snooze — only available once per phase per day
        if (snoozeUsed) {
            btnSnooze.isEnabled = false
            btnSnooze.alpha     = 0.4f
            btnSnooze.text      = "Snooze used"
        } else {
            btnSnooze.setOnClickListener {
                alarmPrefs.setSnoozeUsed(snoozeKey, true)
                AlarmSoundService.stop(this)
                AlarmScheduler.scheduleDelay(this, 3_600_000L)
                finish()
            }
        }

        // Stop / Study now — opens the app
        btnStop.setOnClickListener {
            AlarmSoundService.stop(this)
            val intent = android.content.Intent(this,
                com.iqra.chinese.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    // Prevent back button from dismissing without snooze/stop
    override fun onBackPressed() { /* no-op */ }

    override fun onDestroy() {
        super.onDestroy()
    }
}
