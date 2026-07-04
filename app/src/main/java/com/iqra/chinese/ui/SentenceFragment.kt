package com.iqra.chinese.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iqra.chinese.R
import com.iqra.chinese.data.HskData
import com.iqra.chinese.data.Sentence
import com.iqra.chinese.databinding.FragmentSentenceBinding
import com.iqra.chinese.tts.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SentenceFragment : BaseFragment() {
    private var _b: FragmentSentenceBinding? = null
    private val b get() = _b!!
    private var level = 1; private var sents = listOf<Sentence>()
    private var idx = 0;   private var busy = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSentenceBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        level = arguments?.getInt("level", 1) ?: 1
        buildPills(); load(level)

        b.btnCheck.setOnClickListener    { check() }
        b.btnSkip.setOnClickListener     { idx++; show() }
        b.btnBack.setOnClickListener     { findNavController().navigateUp() }
        b.btnSpeak.setOnClickListener    { speakCurrent() }
        b.btnSpeakSlow.setOnClickListener{ speakCurrentSlow() }

        b.etAnswer.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) { check(); true } else false
        }
    }

    private fun speakCurrent() {
        if (sents.isEmpty()) return
        val s = sents[idx % sents.size]
        when (TtsManager.status) {
            TtsManager.Status.READY -> TtsManager.speak(s.char)
            TtsManager.Status.LANGUAGE_MISSING -> showTtsDialog()
            TtsManager.Status.INITIALIZING -> Toast.makeText(context,"TTS loading…",Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(context,"TTS unavailable",Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakCurrentSlow() {
        if (sents.isEmpty()) return
        val s = sents[idx % sents.size]
        if (TtsManager.isReady()) TtsManager.speakSlow(s.char) else speakCurrent()
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
        level = l; sents = HskData.sentencesFor(l); idx = 0; buildPills()
        if (sents.isEmpty()) {
            b.cardSent.visibility = View.GONE; b.tvEmpty.visibility = View.VISIBLE
        } else {
            b.cardSent.visibility = View.VISIBLE; b.tvEmpty.visibility = View.GONE; show()
        }
    }

    private fun show() {
        if (sents.isEmpty() || _b == null) return
        val s = sents[idx % sents.size]
        b.tvChar.text    = s.char
        b.tvPinyin.text  = if (vm.showPinyin)  s.pinyin  else ""
        b.tvMeaning.text = if (vm.showMeaning) s.meaning else ""
        b.tvCounter.text = "${idx % sents.size + 1} / ${sents.size}"
        b.etAnswer.setText(""); b.etAnswer.setBackgroundResource(R.drawable.bg_input)
        b.btnSpeak.alpha     = if (TtsManager.isReady()) 1f else 0.5f
        b.btnSpeakSlow.alpha = if (TtsManager.isReady()) 1f else 0.5f
    }

    private fun check() {
        if (busy || sents.isEmpty()) return
        val input = b.etAnswer.text.toString().trim().lowercase(); if (input.isEmpty()) return
        val s     = sents[idx % sents.size]
        val words = s.meaning.lowercase().split(" ").filter { it.length > 2 }
        val m     = words.count { input.contains(it.take(3)) }
        val ok    = words.isEmpty() || m.toFloat() / words.size >= 0.6f
        busy = true
        b.etAnswer.setBackgroundResource(if (ok) R.drawable.bg_input_ok else R.drawable.bg_input_err)
        if (ok && TtsManager.isReady()) TtsManager.speak(s.char)
        vm.record(s.id, "sentence", level, ok)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(800); if (_b != null) { idx++; show() }; busy = false
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
