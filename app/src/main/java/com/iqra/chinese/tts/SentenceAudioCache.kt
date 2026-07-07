package com.iqra.chinese.tts

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Generates and caches TTS audio files for Chinese sentences.
 *
 * How it works:
 *  1. First time a sentence is spoken, TTS synthesizes it and saves to
 *     a local .wav file in the app's cache directory.
 *  2. On subsequent plays, the cached file is used directly — no TTS needed,
 *     works fully offline, instant playback.
 *  3. Cache is stored in:
 *     <cacheDir>/sentence_audio/{sentence_id}.wav
 *
 * The cache is generated lazily (on first play) and persists across app launches.
 * It only re-generates if the file is missing or corrupted.
 */
object SentenceAudioCache {

    private const val TAG     = "SentenceAudioCache"
    private const val DIR     = "sentence_audio"
    private const val MAX_WAIT_MS = 8_000L   // max time to wait for synthesis

    private var player: MediaPlayer? = null

    /** Play sentence audio — uses cache if available, generates and caches otherwise */
    fun speak(ctx: Context, sentenceId: String, text: String, slow: Boolean = false) {
        val cacheFile = cacheFile(ctx, sentenceId, slow)

        if (cacheFile.exists() && cacheFile.length() > 0) {
            // Cache hit — play directly
            playFile(cacheFile)
        } else {
            // Cache miss — synthesize + cache + play
            synthesizeAndCache(ctx, sentenceId, text, slow) { file ->
                if (file != null) playFile(file)
                else {
                    // Fallback to live TTS if synthesis fails
                    TtsManager.speak(text, slow)
                }
            }
        }
    }

    /** Returns true if this sentence is already cached */
    fun isCached(ctx: Context, sentenceId: String, slow: Boolean = false): Boolean =
        cacheFile(ctx, sentenceId, slow).let { it.exists() && it.length() > 0 }

    /**
     * Pre-caches a list of sentences in the background.
     * Call this after loading a level to pre-generate audio for all sentences.
     * Runs in a background thread — safe to call from coroutine.
     */
    suspend fun preCacheLevel(ctx: Context, sentences: List<Pair<String, String>>) {
        val uncached = sentences.filter { (id, _) -> !isCached(ctx, id) }
        if (uncached.isEmpty()) return

        Log.i(TAG, "Pre-caching ${uncached.size} sentences...")
        for ((id, text) in uncached) {
            if (!isCached(ctx, id)) {
                synthesizeBlocking(ctx, id, text, slow = false)
            }
            kotlinx.coroutines.delay(100) // small delay to avoid overloading TTS
        }
        Log.i(TAG, "Pre-caching complete")
    }

    /** Clear all cached audio files (useful for settings/reset) */
    fun clearCache(ctx: Context) {
        cacheDir(ctx).listFiles()?.forEach { it.delete() }
    }

    /** Returns total cache size in MB */
    fun cacheSizeMb(ctx: Context): Float {
        val bytes = cacheDir(ctx).listFiles()?.sumOf { it.length() } ?: 0L
        return bytes / (1024f * 1024f)
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private fun cacheDir(ctx: Context): File =
        File(ctx.cacheDir, DIR).also { it.mkdirs() }

    private fun cacheFile(ctx: Context, id: String, slow: Boolean): File {
        val suffix = if (slow) "_slow" else ""
        val safeName = id.replace("/", "_").replace(" ", "_") + suffix + ".wav"
        return File(cacheDir(ctx), safeName)
    }

    private fun playFile(file: File) {
        try {
            val old = player; player = null
            try { old?.stop(); old?.release() } catch (_: Exception) {}

            val mp = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
            }
            mp.setOnCompletionListener { p ->
                try { p.release() } catch (_: Exception) {}
                if (player === p) player = null
            }
            player = mp
            mp.start()
        } catch (e: Exception) {
            Log.e(TAG, "Playback failed: ${e.message}")
        }
    }

    private fun synthesizeAndCache(
        ctx: Context,
        sentenceId: String,
        text: String,
        slow: Boolean,
        onDone: (File?) -> Unit
    ) {
        Thread {
            val file = synthesizeBlocking(ctx, sentenceId, text, slow)
            onDone(file)
        }.also { it.isDaemon = true; it.start() }
    }

    /**
     * Synthesizes text to a WAV file using the system TTS engine.
     * Blocks until synthesis is complete or timeout is reached.
     * Returns the file on success, null on failure.
     */
    private fun synthesizeBlocking(
        ctx: Context,
        sentenceId: String,
        text: String,
        slow: Boolean
    ): File? {
        val outFile = cacheFile(ctx, sentenceId, slow)
        if (outFile.exists() && outFile.length() > 0) return outFile

        var tts: TextToSpeech? = null
        val latch = CountDownLatch(1)
        var success = false

        val locales = listOf(
            Locale("zh", "CN"),
            Locale.SIMPLIFIED_CHINESE,
            Locale("zh")
        )

        tts = TextToSpeech(ctx.applicationContext) { status ->
            if (status != TextToSpeech.SUCCESS) { latch.countDown(); return@TextToSpeech }

            val engine = tts ?: run { latch.countDown(); return@TextToSpeech }

            // Find a working Chinese locale
            var localeSet = false
            for (loc in locales) {
                val avail = try { engine.isLanguageAvailable(loc) }
                            catch (_: Exception) { TextToSpeech.LANG_NOT_SUPPORTED }
                if (avail >= TextToSpeech.LANG_AVAILABLE) {
                    val r = try { engine.setLanguage(loc) }
                            catch (_: Exception) { TextToSpeech.LANG_NOT_SUPPORTED }
                    if (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED) {
                        localeSet = true; break
                    }
                }
            }
            if (!localeSet) { latch.countDown(); return@TextToSpeech }

            engine.setSpeechRate(if (slow) 0.5f else 0.75f)

            val uttId = "syn_${sentenceId.hashCode()}"
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {
                    success = outFile.exists() && outFile.length() > 0
                    latch.countDown()
                }
                override fun onError(id: String?) { latch.countDown() }
                override fun onError(id: String?, errorCode: Int) { latch.countDown() }
            })

            val params = android.os.Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, uttId)
            }
            val result = engine.synthesizeToFile(text, params, outFile, uttId)
            if (result != TextToSpeech.SUCCESS) latch.countDown()
        }

        latch.await(MAX_WAIT_MS, TimeUnit.MILLISECONDS)
        try { tts?.shutdown() } catch (_: Exception) {}

        return if (success) outFile else {
            outFile.delete() // clean up partial file
            null
        }
    }

    fun stop() {
        try { player?.stop(); player?.release() } catch (_: Exception) {}
        player = null
    }
}
