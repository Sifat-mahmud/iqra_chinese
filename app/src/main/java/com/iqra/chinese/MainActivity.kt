package com.iqra.chinese

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.iqra.chinese.alarm.AlarmScheduler
import com.iqra.chinese.firebase.BannerConfig
import com.iqra.chinese.firebase.BannerManager
import com.iqra.chinese.firebase.FirebaseManager
import com.iqra.chinese.tts.TtsManager
import com.iqra.chinese.tts.BundledTts
import com.iqra.chinese.data.*
import com.iqra.chinese.databinding.ActivityMainBinding
import com.iqra.chinese.ui.MainViewModel
import com.iqra.chinese.ui.VMFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var vm: MainViewModel
    private lateinit var nav: NavController

    private val notifLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { AlarmScheduler.schedule(this) }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db   = AppDatabase.get(this)
        val repo = Repository(db.attempts(), db.groups(), Prefs(this))
        vm = ViewModelProvider(this, VMFactory(repo))[MainViewModel::class.java]

        val host = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        nav = host.navController
        binding.bottomNav.setupWithNavController(nav)

        // Hide bottom nav inside deep screens
        nav.addOnDestinationChangedListener { _, dest, _ ->
            val hide = setOf(R.id.practiceFragment, R.id.sentenceFragment,
                R.id.groupPracticeFragment, R.id.testFragment, R.id.authFragment)
            binding.bottomNav.visibility = if (dest.id in hide) View.GONE else View.VISIBLE
        }

        vm.ach.observe(this) { a ->
            a ?: return@observe
            Snackbar.make(binding.root, "${a.emoji} ${a.title} unlocked! +${a.xp} XP", 3000)
                .setBackgroundTint(getColor(R.color.gold))
                .setTextColor(getColor(R.color.black)).show()
        }

        vm.startTimer()
        requestNotif()
        TtsManager.initWithContext(this)
        BundledTts.init(this)
        FirebaseManager.logAppOpen()
        showMiuiAlarmPromptIfNeeded()
        showBannerIfAvailable()
    }

    override fun onResume()  { super.onResume();  vm.startTimer() }
    override fun onPause()   { super.onPause();   vm.stopTimer()  }

    override fun onDestroy() {
        super.onDestroy()
        TtsManager.shutdown()
        releasePlayer()
        bannerListener?.let { BannerManager.removeLiveListener(it) }
    }

    private fun requestNotif() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED)
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        else AlarmScheduler.schedule(this)
    }

    private fun showMiuiAlarmPromptIfNeeded() {
        val isMiui = android.os.Build.MANUFACTURER.equals("xiaomi", ignoreCase = true)
        if (!isMiui) return

        val prefs = getSharedPreferences("iqra", MODE_PRIVATE)
        if (prefs.getBoolean("miui_alarm_shown", false)) return
        prefs.edit().putBoolean("miui_alarm_shown", true).apply()

        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Enable alarm for Iqra Chinese")
            .setMessage(
                "On Xiaomi devices, alarms are blocked when the app is killed.\n\n" +
                "Tap the button below to open Autostart settings and enable it for Iqra Chinese.\n\n" +
                "Also set Battery → No restrictions to ensure the alarm always fires."
            )
            .setPositiveButton("Open Autostart Settings") { _, _ ->
                openMiuiAutoStart()
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun openMiuiAutoStart() {
        val intents = listOf(
            // MIUI 12+ / HyperOS
            android.content.Intent().setComponent(
                android.content.ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            ),
            // Older MIUI
            android.content.Intent().setComponent(
                android.content.ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.securitycenter.MainActivity"
                )
            ),
            // Fallback: generic app settings
            android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.parse("package:$packageName")
            )
        )

        for (intent in intents) {
            try {
                startActivity(intent)
                return   // first one that works, stop
            } catch (e: Exception) {
                // try next
            }
        }
    }

    /**
     * Fetches a remote banner (if any) and shows it as a dismissible overlay.
     * Runs on every app open / resume from background. Cached locally so it
     * also works offline using the last-fetched banner.
     */
    private var bannerListener: com.google.firebase.database.ValueEventListener? = null
    private var lastShownBannerId: String? = null
    private var exoPlayer: ExoPlayer? = null

    private fun showBannerIfAvailable() {
        // Live listener fires immediately with current data on attach (handling
        // the initial launch case), AND delivers any future pushes/updates while
        // the app stays open — no restart needed for new banners to appear.
        bannerListener = BannerManager.startLiveListener(this) { banner ->
            runOnUiThread {
                if (banner == null) {
                    binding.bannerInclude.bannerOverlay.visibility = View.GONE
                    lastShownBannerId = null
                } else if (banner.id != lastShownBannerId) {
                    // Only (re)show if it's a different banner than what's
                    // currently displayed — avoids flicker on repeated syncs
                    displayBanner(banner)
                }
            }
        }
    }

    private fun displayBanner(banner: BannerConfig) {
        val b           = binding.bannerInclude
        val overlay     = b.bannerOverlay
        val ivImage     = b.ivBannerImage
        val playerView  = b.bannerPlayerView
        val tvTitle     = b.tvBannerTitle
        val tvMessage   = b.tvBannerMessage
        val btnYoutube  = b.btnBannerYoutube
        val btnAction   = b.btnBannerAction
        val btnClose    = b.btnBannerClose
        val layoutFb    = b.layoutFeedback
        val tvFbQ       = b.tvFeedbackQuestion
        val etFeedback  = b.etFeedback
        val btnSubmitFb = b.btnSubmitFeedback
        val tvFbThanks  = b.tvFeedbackThanks

        tvTitle.text   = banner.title
        tvMessage.text = banner.message

        // ── Media: video takes priority over image ─────────────────────────
        releasePlayer()
        when {
            !banner.videoUrl.isNullOrEmpty() -> {
                // Check if it's a YouTube URL
                val isYoutube = banner.videoUrl.contains("youtube.com") ||
                                banner.videoUrl.contains("youtu.be")
                if (isYoutube) {
                    // Option A: show YouTube thumbnail + open button
                    ivImage.isVisible     = true
                    playerView.isVisible  = false
                    btnYoutube.isVisible  = true
                    // Auto-load YouTube thumbnail
                    val videoId = extractYoutubeId(banner.videoUrl)
                    if (videoId != null) {
                        ivImage.load("https://img.youtube.com/vi/$videoId/hqdefault.jpg") {
                            crossfade(true)
                        }
                    } else if (!banner.imageUrl.isNullOrEmpty()) {
                        ivImage.load(banner.imageUrl) { crossfade(true) }
                    }
                    btnYoutube.setOnClickListener {
                        try {
                            startActivity(android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(banner.videoUrl)
                            ))
                        } catch (_: Exception) {}
                    }
                } else {
                    // Option B: inline ExoPlayer for direct video URL
                    ivImage.isVisible    = false
                    btnYoutube.isVisible = false
                    playerView.isVisible = true
                    exoPlayer = ExoPlayer.Builder(this).build().also { player ->
                        playerView.player = player
                        player.setMediaItem(MediaItem.fromUri(banner.videoUrl))
                        player.prepare()
                        player.playWhenReady = false
                    }
                }
            }
            !banner.imageUrl.isNullOrEmpty() -> {
                ivImage.isVisible    = true
                playerView.isVisible = false
                btnYoutube.isVisible = false
                ivImage.load(banner.imageUrl) { crossfade(true) }
            }
            else -> {
                ivImage.isVisible    = false
                playerView.isVisible = false
                btnYoutube.isVisible = false
            }
        }

        // ── Action button ──────────────────────────────────────────────────
        if (!banner.actionLabel.isNullOrEmpty() && !banner.actionUrl.isNullOrEmpty()) {
            btnAction.isVisible = true
            btnAction.text = banner.actionLabel
            btnAction.setOnClickListener {
                try {
                    startActivity(android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(banner.actionUrl)
                    ))
                } catch (_: Exception) {}
            }
        } else {
            btnAction.isVisible = false
        }

        // ── Feedback form ──────────────────────────────────────────────────
        if (banner.feedback) {
            layoutFb.isVisible   = true
            tvFbQ.isVisible      = !banner.feedbackQuestion.isNullOrEmpty()
            tvFbQ.text           = banner.feedbackQuestion ?: ""
            tvFbThanks.isVisible = false
            etFeedback.text?.clear()
            btnSubmitFb.isEnabled = true

            btnSubmitFb.setOnClickListener {
                val response = etFeedback.text?.toString()?.trim() ?: ""
                if (response.isEmpty()) {
                    etFeedback.error = "Please enter your feedback"
                    return@setOnClickListener
                }
                BannerManager.submitFeedback(
                    bannerId = banner.id,
                    response = response,
                    uid      = FirebaseManager.uid
                )
                btnSubmitFb.isEnabled = false
                tvFbThanks.isVisible  = true
                // Hide keyboard
                (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
                    ?.hideSoftInputFromWindow(etFeedback.windowToken, 0)
            }
        } else {
            layoutFb.isVisible = false
        }

        // ── Dismiss / close ────────────────────────────────────────────────
        fun closeBanner() {
            overlay.isVisible  = false
            lastShownBannerId  = null
            releasePlayer()
            BannerManager.dismiss(this, banner.id)
        }

        btnClose.setOnClickListener { closeBanner() }
        // Tapping the dark scrim outside the card closes without permanently dismissing
        overlay.setOnClickListener { overlay.isVisible = false; releasePlayer() }

        overlay.isVisible     = true
        lastShownBannerId     = banner.id
        FirebaseManager.logEvent("banner_shown", android.os.Bundle().apply {
            putString("banner_id", banner.id)
        })
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
        binding.bannerInclude.bannerPlayerView.player = null
    }

    private fun extractYoutubeId(url: String): String? {
        // Handles youtu.be/ID, youtube.com/watch?v=ID, youtube.com/embed/ID
        val patterns = listOf(
            Regex("youtu\\.be/([a-zA-Z0-9_-]{11})"),
            Regex("[?&]v=([a-zA-Z0-9_-]{11})"),
            Regex("embed/([a-zA-Z0-9_-]{11})")
        )
        return patterns.firstNotNullOfOrNull { it.find(url)?.groupValues?.getOrNull(1) }
    }
}
