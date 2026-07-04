package com.iqra.chinese.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.view.*
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.iqra.chinese.R
import com.iqra.chinese.data.HskData
import com.iqra.chinese.alarm.AlarmPrefs
import com.iqra.chinese.alarm.AlarmScheduler
import com.iqra.chinese.alarm.alarmPhase
import com.iqra.chinese.firebase.FirebaseManager
import com.iqra.chinese.tts.DeviceInfo
import com.iqra.chinese.tts.TtsManager
import com.iqra.chinese.databinding.FragmentSettingsBinding

import kotlinx.coroutines.Job

class SettingsFragment : BaseFragment() {
    private var _b: FragmentSettingsBinding? = null
    private val b get() = _b!!
    private var countdownJob: Job? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSettingsBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        b.switchPinyin.isChecked  = vm.showPinyin
        b.switchMeaning.isChecked = vm.showMeaning
        b.switchPinyin.setOnCheckedChangeListener  { _, v -> vm.setShowPinyin(v) }
        b.switchMeaning.setOnCheckedChangeListener { _, v -> vm.setShowMeaning(v) }

        // Firebase account card
        refreshAccountCard()
        b.btnSignInOut.setOnClickListener {
            if (FirebaseManager.isLoggedIn) {
                FirebaseManager.signOut()
                refreshAccountCard()
                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.action_settings_to_auth)
            }
        }
        b.btnSyncNow.setOnClickListener {
            if (!FirebaseManager.isLoggedIn) {
                findNavController().navigate(R.id.action_settings_to_auth)
                return@setOnClickListener
            }
            b.btnSyncNow.isEnabled = false
            b.btnSyncNow.text = "Syncing..."
            viewLifecycleOwner.lifecycleScope.launch {
                vm.repo.prefs.lastBackupDate = ""   // clear guard so it runs
                val attempts = vm.repo.attemptDao.allOnce()
                val ok = FirebaseManager.backupStatsIfDue(vm.repo.prefs, attempts)
                b.btnSyncNow.isEnabled = true
                b.btnSyncNow.text = "Sync Now"
                if (ok) {
                    Toast.makeText(context, "✅ Sync complete!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "⚠️ Sync failed — check connection", Toast.LENGTH_SHORT).show()
                }
                refreshAccountCard()
            }
        }

        vm.xp.observe(viewLifecycleOwner)     { b.tvXP.text = "⚡ $it XP  ·  Level ${it/500+1}" }
        vm.streak.observe(viewLifecycleOwner) { b.tvStreak.text = "🔥 $it day streak" }
        vm.daily.observe(viewLifecycleOwner)  {
            b.tvDaily.text    = "${it/60}m ${(it%60).toString().padStart(2,'0')}s / 30m"
            b.pbDaily.progress = minOf(100, it * 100 / 1800)
        }
        b.tvBest.text     = "🏆 Best test: ${vm.bestTest}%"
        b.tvAppInfo.text  = "Iqra Chinese  ·  ${HskData.words.values.sumOf{it.size}} words  ·  6 HSK levels  ·  Offline"

        // Alarm phase status
        val alarmPrefs = AlarmPrefs(requireContext())
        refreshAlarmPhase(alarmPrefs)

        // Day-start NumberPickers
        b.npHour.minValue   = 0;  b.npHour.maxValue   = 23
        b.npMinute.minValue = 0;  b.npMinute.maxValue = 59
        b.npHour.value      = alarmPrefs.dayStartHour
        b.npMinute.value    = alarmPrefs.dayStartMinute
        b.npHour.setFormatter   { String.format("%02d", it) }
        b.npMinute.setFormatter { String.format("%02d", it) }

        b.btnSaveDayStart.setOnClickListener {
            alarmPrefs.dayStartHour   = b.npHour.value
            alarmPrefs.dayStartMinute = b.npMinute.value
            b.tvDayStartConfirm.text  =
                "✅ Day starts at %02d:%02d".format(b.npHour.value, b.npMinute.value)
            AlarmScheduler.schedule(requireContext())
            refreshAlarmPhase(alarmPrefs)
            Toast.makeText(context, "Day start saved", Toast.LENGTH_SHORT).show()
        }

        // TTS engine info
        val ttsStatus = TtsManager.getStatusSummary()
        val osDesc    = DeviceInfo.getOsDescription()
        b.tvTtsInfo.text = "🔊 TTS: $ttsStatus"
        b.tvDeviceOs.text = "📱 Device: $osDesc"

        // TTS test button
        // Retry TTS init (useful when engine was not ready on first launch)
        b.btnRetryTts.setOnClickListener {
            TtsManager.shutdown()
            TtsManager.initWithContext(requireContext())
            android.widget.Toast.makeText(context, "Retrying TTS detection…", android.widget.Toast.LENGTH_SHORT).show()
            // Refresh status after short delay
            viewLifecycleOwner.lifecycleScope.launch {
                kotlinx.coroutines.delay(2000)
                if (_b != null) {
                    b.tvTtsInfo.text = "🔊 TTS: ${TtsManager.getStatusSummary()}"
        // Show extra help for MIUI-blocked devices
        if (TtsManager.status == TtsManager.Status.ERROR ||
            TtsManager.status == TtsManager.Status.INTENT_FALLBACK) {
            b.tvTtsInfo.setTextColor(
                if (TtsManager.status == TtsManager.Status.ERROR)
                    resources.getColor(R.color.ruby, null)
                else resources.getColor(R.color.amber, null)
            )
        }
                }
            }
        }

        b.btnTestTts.setOnClickListener {
            when (TtsManager.status) {
                TtsManager.Status.READY -> {
                    TtsManager.speak("你好，欢迎学习中文！")
                    android.widget.Toast.makeText(context,
                        "Speaking: 你好，欢迎学习中文！", android.widget.Toast.LENGTH_SHORT).show()
                }
                TtsManager.Status.LANGUAGE_MISSING -> {
                    android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Chinese TTS Not Available")
                        .setMessage(getTtsInstallGuide())
                        .setPositiveButton("Open Settings") { _,_ ->
                            startActivity(android.content.Intent("com.android.settings.TTS_SETTINGS"))
                        }
                        .setNegativeButton("Later", null).show()
                }
                TtsManager.Status.INITIALIZING ->
                    android.widget.Toast.makeText(context, "TTS still loading…", android.widget.Toast.LENGTH_SHORT).show()
                else ->
                    android.widget.Toast.makeText(context, "TTS not available on this device", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        b.btnResetProgress.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Progress?")
                .setMessage("Clears all attempt history and XP. Cannot be undone.")
                .setPositiveButton("Reset") { _,_ -> vm.resetProgress(); Toast.makeText(context,"Reset done",Toast.LENGTH_SHORT).show() }
                .setNegativeButton("Cancel", null).show()
        }
        b.btnResetAll.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Everything?")
                .setMessage("ALL data including groups will be permanently deleted.")
                .setPositiveButton("Delete All") { _,_ -> vm.resetAll(); Toast.makeText(context,"All data cleared",Toast.LENGTH_SHORT).show() }
                .setNegativeButton("Cancel", null).show()
        }
    }

    private fun getTtsInstallGuide(): String {
        val os      = DeviceInfo.getOsType()
        val miuiVer = DeviceInfo.getMiuiVersion()
        return when (os) {
            DeviceInfo.OsType.HYPER_OS -> """
Xiaomi HyperOS — Install Chinese TTS:

▶ Best option (iFlytek):
  1. Open App Market (应用商店)
  2. Search: 讯飞语音引擎
  3. Install and open it once
  4. Reopen Iqra Chinese — TTS auto-detected

▶ Alternative (Google TTS):
  1. Install Google TTS from Play Store
  2. Settings → More Settings → Language & Input
     → Text-to-Speech → Google TTS → ⚙ → Install voice data → Chinese
            """.trimIndent()

            DeviceInfo.OsType.MIUI -> """
Xiaomi MIUI — Install Chinese TTS:

▶ Best option (iFlytek):
  1. Open App Market (应用商店)
  2. Search: 讯飞语音引擎  (iFlytek Speech Engine)
  3. Install, then reopen Iqra Chinese

▶ Alternative (Google TTS):
  1. Install Google Text-to-Speech from Play Store
  2. Settings → Additional Settings → Language & Input
     → Text-to-Speech output
  3. Select Google TTS → ⚙ Settings
     → Install voice data → 中文 (Chinese Simplified)
  4. Reopen Iqra Chinese

Note: MIUI ${if (miuiVer.isNotEmpty()) miuiVer else "V8"} detected.
If settings path differs, search "Text-to-Speech" in Settings search bar.
            """.trimIndent()

            DeviceInfo.OsType.SAMSUNG -> """
Samsung — Install Chinese TTS:

1. Settings → General Management → Language
   → Text-to-Speech
2. Select Samsung TTS or Google TTS
3. Tap ⚙ Settings → Install voice data
4. Select Chinese (Simplified) → Download
5. Reopen Iqra Chinese
            """.trimIndent()

            else -> """
Install Chinese TTS:

1. Settings → Language & Input
   → Text-to-Speech output
2. Select Google Text-to-Speech Engine
3. Tap ⚙ Settings → Install voice data
4. Choose Chinese (Simplified) → Download
5. Reopen Iqra Chinese
            """.trimIndent()
        }
    }

    private fun refreshAlarmPhase(alarmPrefs: AlarmPrefs) {
        if (_b == null) return
        val phase = alarmPhase(alarmPrefs)
        val iqraPrefs = vm.repo.prefs
        val dailySecs = iqraPrefs.dailySecs
        val goalMet   = dailySecs >= 1800
        val elapsedH  = ((System.currentTimeMillis() - alarmPrefs.todayStartMs) / 3_600_000).toInt()
        val startStr  = "%02d:%02d".format(alarmPrefs.dayStartHour, alarmPrefs.dayStartMinute)

        b.tvAlarmPhase.text = when {
            goalMet -> "✅ 30-min goal met — alarm silenced for today"
            phase == 0 -> "🔔 Phase 1 of 2: Notification mode (hour $elapsedH of 12)\nDay started at $startStr"
            else       -> "🚨 Phase 2 of 2: ALARM mode — goal not met after 12h\nDay started at $startStr"
        }
        b.tvAlarmPhase.setTextColor(resources.getColor(
            when { goalMet -> R.color.jade; phase == 0 -> R.color.amber; else -> R.color.ruby }, null
        ))
        b.tvDayStartConfirm.text = "Day starts at $startStr"
    }

    private fun refreshAccountCard() {
        if (_b == null) return
        if (FirebaseManager.isLoggedIn) {
            b.tvAccountStatus.text  = "✅ Signed in as ${FirebaseManager.userEmail}"
            b.tvBackupStatus.text   = "Last backup: ${vm.repo.prefs.lastBackupDate.ifEmpty { "Never" }}"
            b.btnSignInOut.text     = "Sign Out"
            b.btnSignInOut.setBackgroundColor(resources.getColor(R.color.bg3, null))
            b.btnSignInOut.setTextColor(resources.getColor(R.color.ruby, null))
            b.btnSyncNow.isEnabled  = true
        } else {
            b.tvAccountStatus.text  = "Not signed in — progress is stored locally only"
            b.tvBackupStatus.text   = "Sign in to enable cloud backup"
            b.btnSignInOut.text     = "Sign In / Register"
            b.btnSignInOut.setBackgroundColor(resources.getColor(R.color.gold, null))
            b.btnSignInOut.setTextColor(resources.getColor(R.color.black, null))
            b.btnSyncNow.isEnabled  = false
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                val ms = AlarmScheduler.msUntilNext(requireContext())
                if (_b != null) {
                    b.tvAlarmCountdown.text = if (ms <= 0L) {
                        "⏱ Next check: imminent"
                    } else {
                        val h = ms / 3_600_000
                        val m = (ms % 3_600_000) / 60_000
                        val s = (ms % 60_000) / 1_000
                        "⏱ Next check in: %02d:%02d:%02d".format(h, m, s)
                    }
                }
                delay(1_000)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAccountCard()
        refreshAlarmPhase(AlarmPrefs(requireContext()))
        startCountdown()
    }

    override fun onPause() {
        super.onPause()
        countdownJob?.cancel()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}