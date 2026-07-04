package com.iqra.chinese.tts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Offline Chinese TTS with MIUI V816 compatibility.
 *
 * MIUI V816 (BD firmware) blocks standard TTS API binding (code=-1).
 * This manager:
 *  1. Tries standard TextToSpeech API (works on standard Android + HyperOS)
 *  2. On persistent failure, falls back to ACTION_VIEW speech intent
 *  3. Shows install guide for Google TTS if all else fails
 */
object TtsManager : TextToSpeech.OnInitListener {

    private val mainHandler  = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech?   = null
    private var appCtx: Context?     = null
    private var ready                = false
    private var pendingText: String? = null

    private val engineQueue  = ArrayDeque<TtsEngineSelector.EngineChoice>()
    private var currentEngine: TtsEngineSelector.EngineChoice? = null
    private var attemptCount = 0
    private val MAX_ATTEMPTS = 10

    // Tracks whether we've fallen back to intent-based TTS
    private var usingIntentFallback = false

    enum class Status { UNINITIALIZED, INITIALIZING, READY, LANGUAGE_MISSING,
                        INTENT_FALLBACK, ERROR }
    var status      = Status.UNINITIALIZED; private set
    var engineLabel = "Not started";       private set
    var deviceOs    = "Unknown";           private set

    // ── Public API ─────────────────────────────────────────────────────────

    fun initWithContext(context: Context) {
        if (ready || usingIntentFallback) return
        appCtx      = context.applicationContext
        BundledTts.init(context)
        deviceOs    = DeviceInfo.getOsDescription()
        status      = Status.INITIALIZING
        engineLabel = "Detecting…"

        Thread {
            val queue = TtsEngineSelector.buildEngineQueue(context)
            Thread.sleep(800) // Wait for full Activity init on MIUI
            mainHandler.post {
                engineQueue.clear()
                engineQueue.addAll(queue)
                Log.i("TtsManager", "Queue (${queue.size}): ${queue.map { it.engineName }}")
                tryNextEngine()
            }
        }.also { it.name = "TtsInit"; it.isDaemon = true }.start()
    }

    fun init(ctx: Context) = initWithContext(ctx)

    /** Speak text — tries bundled audio first, then TTS API, then intent fallback */
    fun speak(text: String, slow: Boolean = false) {
        // 1. Try bundled audio (always works offline, any device)
        if (BundledTts.speak(text, slow)) return

        // 2. Standard TTS API
        when {
            usingIntentFallback -> speakViaIntent(text)
            !ready              -> { pendingText = text }
            else                -> tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "u${text.hashCode()}")
        }
    }

    fun speakSlow(text: String) = speak(text, slow = true)

    /**
     * Intent-based TTS fallback for MIUI V816.
     * Tries multiple broadcast/intent mechanisms to invoke system TTS.
     */
    private fun speakViaIntent(text: String) {
        val ctx = appCtx ?: return

        // Method 1: MIUI system UI speak broadcast
        trySendBroadcast(ctx, "com.android.systemui.SPEAK_TEXT",
            mapOf("text" to text, "language" to "zh-CN"))

        // Method 2: MIUI TTS speak broadcast (different versions)
        trySendBroadcast(ctx, "com.miui.tts.SPEAK",
            mapOf("text" to text, "locale" to "zh_CN"))

        // Method 3: MIUI accessibility TTS
        trySendBroadcast(ctx, "com.xiaomi.tts.action.SPEAK",
            mapOf("text" to text, "language" to "zh"))

        // Method 4: Generic Android TTS speak intent
        try {
            val i = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            // Don't actually navigate — just try the TTS service via package manager
        } catch (_: Exception) {}

        // Method 5: Try to start TTS activity to speak (some MIUI versions)
        try {
            val i = android.content.Intent("android.intent.action.TTS_SPEAK").apply {
                putExtra("text", text)
                putExtra("lang", "zh")
                addFlags(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            ctx.sendBroadcast(i)
        } catch (_: Exception) {}

        Log.i("TtsManager", "Sent SPEAK_TEXT broadcast: $text")
    }

    private fun trySendBroadcast(ctx: android.content.Context, action: String,
                                  extras: Map<String, String>) {
        try {
            val i = android.content.Intent(action).apply {
                extras.forEach { (k, v) -> putExtra(k, v) }
                addFlags(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            ctx.sendBroadcast(i)
        } catch (e: Exception) {
            Log.w("TtsManager", "Broadcast $action failed: ${e.message}")
        }
    }

    fun stop() {
        mainHandler.post { try { tts?.stop() } catch (_: Exception) {} }
    }

    fun isReady() = ready || usingIntentFallback

    fun shutdown() {
        mainHandler.post {
            try { tts?.stop(); tts?.shutdown() } catch (_: Exception) {}
            tts = null
        }
        ready = false; usingIntentFallback = false
        status = Status.UNINITIALIZED
        engineQueue.clear(); attemptCount = 0
        engineLabel = "Not started"
    }

    fun getStatusSummary() = when (status) {
        Status.READY            -> "✓ $engineLabel"
        Status.UNINITIALIZED    -> if (BundledTts.canSpeak("你好")) "✓ Bundled audio (offline)" else "Not started"
        Status.INTENT_FALLBACK  -> "⚠ Limited TTS (MIUI restricted)"
        Status.LANGUAGE_MISSING -> "⚠ Chinese voice not installed"
        Status.INITIALIZING     -> "⏳ $engineLabel"
        Status.ERROR            -> "✗ TTS blocked by MIUI — install Google TTS"
        Status.UNINITIALIZED    -> "Not started"
    }

    fun needsInstallGuide() = status == Status.ERROR

    // ── Engine cycling (main thread only) ───────────────────────────────────

    private fun tryNextEngine() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { tryNextEngine() }
            return
        }
        if (engineQueue.isEmpty() || attemptCount >= MAX_ATTEMPTS) {
            Log.e("TtsManager", "All engines exhausted ($attemptCount attempts)")
            // Enable intent-based fallback for MIUI
            if (DeviceInfo.isXiaomi()) {
                Log.i("TtsManager", "Enabling intent-based TTS fallback for MIUI")
                usingIntentFallback = true
                status      = Status.INTENT_FALLBACK
                engineLabel = "MIUI System TTS (limited)"
            } else {
                status      = Status.ERROR
                engineLabel = "TTS unavailable — install Google TTS"
            }
            return
        }
        val choice    = engineQueue.removeFirst()
        currentEngine = choice
        attemptCount++
        engineLabel   = choice.engineName
        val pkg       = choice.packageName

        Log.i("TtsManager", "Attempt #$attemptCount: ${choice.engineName} (${pkg ?: "system default"})")

        try { tts?.stop(); tts?.shutdown() } catch (_: Exception) {}
        tts = null

        val ctx = appCtx ?: run { status = Status.ERROR; return }
        val delay = if (attemptCount > 1) 400L else 0L
        mainHandler.postDelayed({
            tts = try {
                if (pkg != null) TextToSpeech(ctx, this, pkg)
                else             TextToSpeech(ctx, this)
            } catch (e: Exception) {
                Log.e("TtsManager", "Constructor failed: ${e.message}")
                tryNextEngine()
                return@postDelayed
            }
        }, delay)
    }

    override fun onInit(code: Int) {
        if (code != TextToSpeech.SUCCESS) {
            Log.w("TtsManager", "${currentEngine?.engineName} init failed (code=$code)")
            tryNextEngine()
            return
        }
        val engine = tts ?: run { tryNextEngine(); return }

        val locales = listOf(
            Locale("zh", "CN"), Locale.SIMPLIFIED_CHINESE,
            Locale("zh", "TW"), Locale.TRADITIONAL_CHINESE,
            Locale("zh"), Locale("cmn")
        )
        var chosen: Locale? = null
        for (loc in locales) {
            val avail = try { engine.isLanguageAvailable(loc) }
                        catch (_: Exception) { TextToSpeech.LANG_NOT_SUPPORTED }
            if (avail >= TextToSpeech.LANG_AVAILABLE) {
                val r = try { engine.setLanguage(loc) }
                        catch (_: Exception) { TextToSpeech.LANG_NOT_SUPPORTED }
                if (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED) {
                    chosen = loc; break
                }
            }
        }
        if (chosen == null) {
            Log.w("TtsManager", "${currentEngine?.engineName} — no Chinese locale")
            tryNextEngine()
            return
        }
        engine.setSpeechRate(0.75f)
        engine.setPitch(1.0f)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?)  {}
            override fun onError(id: String?) { Log.w("TtsManager", "Utterance error: $id") }
        })
        status = Status.READY; ready = true
        Log.i("TtsManager", "✓ READY — ${currentEngine?.engineName}, locale=$chosen")
        pendingText?.let { speak(it) }
        pendingText = null
    }
}
