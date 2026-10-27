package com.iqra.chinese.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iqra.chinese.R
import com.iqra.chinese.data.HskData
import com.iqra.chinese.data.Sentence
import com.iqra.chinese.databinding.FragmentSentenceBinding
import com.iqra.chinese.tts.SentenceAudioCache
import com.iqra.chinese.tts.TtsManager
import com.iqra.chinese.util.PinyinMatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SentenceFragment : BaseFragment() {
    private var _b: FragmentSentenceBinding? = null
    private val b get() = _b!!
    private var level = 1; private var sents = listOf<Sentence>()
    private var idx = 0;   private var busy = false
    private var currentSentId: String? = null
    private var ttsJob: kotlinx.coroutines.Job? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSentenceBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        level = arguments?.getInt("level", 1) ?: 1
        buildPills(); load(level)

        b.btnCheck.setOnClickListener    { check() }
        b.btnSkip.setOnClickListener     { stopAutoSpeak(); idx++; show() }
        b.btnBack.setOnClickListener     { findNavController().navigateUp() }
        b.btnSpeak.setOnClickListener    { speakCurrent() }
        b.btnSpeakSlow.setOnClickListener{ speakCurrentSlow() }

        b.etPinyinAnswer.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_NEXT) { b.etAnswer.requestFocus(); true } else false
        }
        b.etAnswer.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) { check(); true } else false
        }
        // Backspace on an empty meaning field jumps back to the pinyin field
        // Typing clears the "please type a value" error on that field
        b.etPinyinAnswer.doAfterTextChanged { clearFieldError(b.etPinyinAnswer, b.tvPinyinError) }
        b.etAnswer.doAfterTextChanged { clearFieldError(b.etAnswer, b.tvMeaningError) }
        b.etAnswer.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN
                && b.etAnswer.text.isNullOrEmpty()) {
                b.etPinyinAnswer.requestFocus()
                b.etPinyinAnswer.setSelection(b.etPinyinAnswer.text?.length ?: 0)
                true
            } else false
        }
    }

    private fun speakCurrent() {
        if (sents.isEmpty()) return
        val s = sents[idx % sents.size]
        speakSentence(s, slow = false)
    }

    private fun speakCurrentSlow() {
        if (sents.isEmpty()) return
        val s = sents[idx % sents.size]
        speakSentence(s, slow = true)
    }

    private fun speakSentence(s: Sentence, slow: Boolean) {
        val ctx = context ?: return
        when (TtsManager.status) {
            TtsManager.Status.LANGUAGE_MISSING -> showTtsDialog()
            TtsManager.Status.INITIALIZING ->
                Toast.makeText(context, "TTS loading…", Toast.LENGTH_SHORT).show()
            else -> {
                // Use cached audio if available, otherwise synthesize + cache
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    SentenceAudioCache.speak(ctx, s.id, s.char, slow)
                }
            }
        }
    }

    private fun showTtsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Chinese TTS Not Installed")
            .setMessage("Install Chinese (Simplified) voice data in:\nSettings → Language & Input → Text-to-Speech → Install voice data")
            .setPositiveButton("Open Settings") { _,_ -> startActivity(Intent("com.android.settings.TTS_SETTINGS")) }
            .setNegativeButton("Later", null).show()
    }

    private fun buildPills() {
        _b?.llPills?.removeAllViews() ?: return
        vm.unlocked.forEach { l ->
            val tv = TextView(requireContext()).apply {
                text = "HSK $l"; textSize = 11f; setPadding(28, 10, 28, 10)
                val c = HskData.COLOR[l] ?: "#F5C842"
                if (l == level) { setBackgroundColor(Color.parseColor(c)); setTextColor(Color.BLACK) }
                else { setTextColor(Color.parseColor(c)); setBackgroundResource(R.drawable.bg_pill) }
                setOnClickListener { load(l) }
            }
            b.llPills.addView(tv)
        }
    }

    private fun load(l: Int) {
        level = l; sents = HskData.sentencesFor(l)
        idx   = if (sents.isNotEmpty()) vm.prefs.sentIdxFor(l) % sents.size else 0
        buildPills()
        if (sents.isEmpty()) {
            b.cardSent.visibility = View.GONE; b.tvEmpty.visibility = View.VISIBLE
        } else {
            b.cardSent.visibility = View.VISIBLE; b.tvEmpty.visibility = View.GONE; show()
            // Pre-cache all sentences for this level in background
            preCacheLevel()
        }
    }

    /**
     * Pre-generates and caches audio for all sentences in the current level.
     * Runs in background so it doesn't block UI. Only processes un-cached sentences.
     */
    private fun preCacheLevel() {
        val ctx = context ?: return
        val pairs = sents.map { it.id to it.char }
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                SentenceAudioCache.preCacheLevel(ctx, pairs)
            }
        }
    }

    private fun show() {
        if (sents.isEmpty() || _b == null) return
        val s = sents[idx % sents.size]
        b.tvChar.text    = s.char
        b.tvPinyin.text  = if (vm.showPinyin)  s.pinyin  else ""
        b.tvMeaning.text = if (vm.showMeaning) s.meaning else ""
        b.tvCounter.text = "${idx % sents.size + 1} / ${sents.size}"
        b.etPinyinAnswer.setText(""); b.etPinyinAnswer.setBackgroundResource(R.drawable.bg_input)
        b.etAnswer.setText(""); b.etAnswer.setBackgroundResource(R.drawable.bg_input)
        b.tvPinyinError.visibility = View.GONE
        b.tvMeaningError.visibility = View.GONE
        b.etPinyinAnswer.requestFocus()
        b.btnSpeak.alpha     = 1f
        b.btnSpeakSlow.alpha = 1f

        // Save last position — per level, so each HSK level's sentence set
        // remembers its own spot, resuming exactly where left off
        vm.prefs.setSentIdxFor(level, idx % sents.size)

        buildWordBreakdown(s)

        // Auto-speak on card appear — loops with 1s pause between repeats
        stopAutoSpeak()
        currentSentId = s.id
        autoSpeakSentence(s, s.id)
    }

    /**
     * Breaks the current sentence down into its component words, reusing the
     * SAME HSK word list shown everywhere else in the app (no separate/
     * duplicate word data) — matched via HskData.segmentSentence(), a greedy
     * longest-match segmentation against HskData.words. Each matched word
     * shows its character, pinyin, English meaning, and the HSK level it
     * belongs to (color-coded to match the level pill colors used elsewhere).
     */
    private fun buildWordBreakdown(s: Sentence) {
        val container = b.llWordBreakdown
        container.removeAllViews()
        val ctx = context ?: return

        val matchedWords = HskData.segmentSentence(s.char)
        if (matchedWords.isEmpty()) {
            b.tvWordBreakdownHeader.visibility = View.GONE
            return
        }
        b.tvWordBreakdownHeader.visibility = View.VISIBLE

        for (w in matchedWords) {
            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(14, 10, 14, 10)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8 }
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#1AFFFFFF"))
                    cornerRadius = 20f
                }
            }

            // Pinyin + meaning stacked
            val textCol = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvPinyin = TextView(ctx).apply {
                text = w.pinyin
                textSize = 11f
                setTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
            }
            val tvMeaning = TextView(ctx).apply {
                text = w.meaning
                textSize = 13f
                setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
            }
            textCol.addView(tvPinyin)
            textCol.addView(tvMeaning)

            // HSK level badge
            val tvBadge = TextView(ctx).apply {
                text = "HSK ${w.level}"
                textSize = 9f
                setTextColor(Color.parseColor(HskData.COLOR[w.level] ?: "#F5C842"))
                setPadding(12, 4, 12, 4)
                background = GradientDrawable().apply {
                    setStroke(2, Color.parseColor(HskData.COLOR[w.level] ?: "#F5C842"))
                    cornerRadius = 20f
                }
            }

            // Chinese character — FIXED width column so every row's pinyin/
            // meaning starts at the same horizontal position, regardless of
            // whether this word is 1, 2, 3 or 4 characters long.
            val charColWidthPx = (56 * ctx.resources.displayMetrics.density).toInt()
            row.addView(TextView(ctx).apply {
                text = w.char
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor(HskData.COLOR[w.level] ?: "#F5C842"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(
                    charColWidthPx, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 10 }
            })
            row.addView(textCol)
            row.addView(tvBadge)

            container.addView(row)
        }
    }

    private fun autoSpeakSentence(s: Sentence, sentId: String) {
        if (currentSentId != sentId || _b == null) return
        val ctx = context ?: return
        // Try bundled audio first (has onDone callback)
        val played = com.iqra.chinese.tts.BundledTts.speak(s.char, onDone = {
            if (currentSentId != sentId || _b == null) return@speak
            ttsJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(1000)
                if (currentSentId == sentId && _b != null) autoSpeakSentence(s, sentId)
            }
        })
        if (!played) {
            // Fallback: SentenceAudioCache (no onDone), loop via fixed delay estimate
            viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SentenceAudioCache.speak(ctx, s.id, s.char, slow = false)
                val estimatedDurationMs = (s.char.length * 350L).coerceIn(1500L, 6000L)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    ttsJob = viewLifecycleOwner.lifecycleScope.launch {
                        delay(estimatedDurationMs + 1000L)
                        if (currentSentId == sentId && _b != null) autoSpeakSentence(s, sentId)
                    }
                }
            }
        }
    }

    private fun stopAutoSpeak() {
        currentSentId = null
        ttsJob?.cancel()
        ttsJob = null
        com.iqra.chinese.tts.BundledTts.stop()
    }

    /** Red border + message under a field the user left empty on Check. */
    private fun showFieldError(field: EditText, errorView: TextView) {
        field.setBackgroundResource(R.drawable.bg_input_err)
        errorView.visibility = View.VISIBLE
    }

    private fun clearFieldError(field: EditText, errorView: TextView) {
        field.setBackgroundResource(R.drawable.bg_input)
        errorView.visibility = View.GONE
    }

    private fun check() {
        if (busy || sents.isEmpty()) return
        val pinyinInput  = b.etPinyinAnswer.text.toString().trim()
        val meaningInput = b.etAnswer.text.toString().trim().lowercase()
        val pinyinEmpty  = pinyinInput.isEmpty()
        val meaningEmpty = meaningInput.isEmpty()
        if (pinyinEmpty || meaningEmpty) {
            // Tell the user which field(s) still need a value
            if (pinyinEmpty)  showFieldError(b.etPinyinAnswer, b.tvPinyinError)
            if (meaningEmpty) showFieldError(b.etAnswer, b.tvMeaningError)
            if (pinyinEmpty) b.etPinyinAnswer.requestFocus() else b.etAnswer.requestFocus()
            return
        }

        val s = sents[idx % sents.size]
        val words   = s.meaning.lowercase().split(" ").filter { it.length > 2 }
        val m       = words.count { meaningInput.contains(it.take(3)) }
        val meaningOk = words.isEmpty() || m.toFloat() / words.size >= 0.6f
        val pinyinOk  = PinyinMatch.matches(pinyinInput, s.pinyin)
        val ok = pinyinOk && meaningOk

        vm.record(s.id, "sentence", level, ok)

        if (ok) {
            busy = true
            b.etPinyinAnswer.setBackgroundResource(R.drawable.bg_input_ok)
            b.etAnswer.setBackgroundResource(R.drawable.bg_input_ok)
            speakSentence(s, slow = false)
            viewLifecycleOwner.lifecycleScope.launch {
                delay(800)
                if (_b != null) { stopAutoSpeak(); idx++; show() }
                busy = false
            }
        } else {
            // Flash red on whichever field(s) are wrong, stay on this sentence
            b.etPinyinAnswer.setBackgroundResource(
                if (pinyinOk) R.drawable.bg_input_ok else R.drawable.bg_input_err)
            b.etAnswer.setBackgroundResource(
                if (meaningOk) R.drawable.bg_input_ok else R.drawable.bg_input_err)
            viewLifecycleOwner.lifecycleScope.launch {
                delay(600)
                if (_b == null) return@launch
                if (!pinyinOk) {
                    b.etPinyinAnswer.setText("")
                    b.etPinyinAnswer.setBackgroundResource(R.drawable.bg_input)
                } else {
                    b.etPinyinAnswer.setBackgroundResource(R.drawable.bg_input)
                }
                if (!meaningOk) {
                    b.etAnswer.setText("")
                    b.etAnswer.setBackgroundResource(R.drawable.bg_input)
                } else {
                    b.etAnswer.setBackgroundResource(R.drawable.bg_input)
                }
                if (!pinyinOk) b.etPinyinAnswer.requestFocus() else b.etAnswer.requestFocus()
            }
        }
    }

    override fun onDestroyView() {
        stopAutoSpeak()
        SentenceAudioCache.stop()
        super.onDestroyView()
        _b = null
    }
}

