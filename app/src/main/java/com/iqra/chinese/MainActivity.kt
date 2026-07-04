package com.iqra.chinese

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.iqra.chinese.alarm.AlarmScheduler
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

    override fun onDestroy() { super.onDestroy(); TtsManager.shutdown() }

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
    private fun showBannerIfAvailable() {
        lifecycleScope.launch {
            val banner = BannerManager.fetchBanner(this@MainActivity) ?: return@launch

            val bannerBinding = binding.bannerInclude
            val overlay     = bannerBinding.bannerOverlay
            val ivImage     = bannerBinding.ivBannerImage
            val tvTitle     = bannerBinding.tvBannerTitle
            val tvMessage   = bannerBinding.tvBannerMessage
            val btnAction   = bannerBinding.btnBannerAction
            val btnClose    = bannerBinding.btnBannerClose

            tvTitle.text   = banner.title
            tvMessage.text = banner.message

            if (!banner.imageUrl.isNullOrEmpty()) {
                ivImage.visibility = View.VISIBLE
                ivImage.load(banner.imageUrl) {
                    crossfade(true)
                }
            } else {
                ivImage.visibility = View.GONE
            }

            if (!banner.actionLabel.isNullOrEmpty() && !banner.actionUrl.isNullOrEmpty()) {
                btnAction.visibility = View.VISIBLE
                btnAction.text = banner.actionLabel
                btnAction.setOnClickListener {
                    try {
                        startActivity(android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(banner.actionUrl)
                        ))
                    } catch (_: Exception) {}
                    overlay.visibility = View.GONE
                    BannerManager.dismiss(this@MainActivity, banner.id)
                }
            } else {
                btnAction.visibility = View.GONE
            }

            btnClose.setOnClickListener {
                overlay.visibility = View.GONE
                BannerManager.dismiss(this@MainActivity, banner.id)
            }
            // Tapping outside the card also dismisses without marking as permanently seen
            // (re-shows on next launch in case user wants to see it again)
            overlay.setOnClickListener {
                overlay.visibility = View.GONE
            }

            overlay.visibility = View.VISIBLE
            FirebaseManager.logEvent("banner_shown", android.os.Bundle().apply {
                putString("banner_id", banner.id)
            })
        }
    }
}
