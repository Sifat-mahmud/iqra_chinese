package com.iqra.chinese.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.iqra.chinese.R
import com.iqra.chinese.data.HskData
import com.iqra.chinese.data.Word
import com.iqra.chinese.databinding.FragmentPracticeBinding
import com.iqra.chinese.tts.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PracticeFragment : BaseFragment() {
    private var _b: FragmentPracticeBinding? = null
    private val b get() = _b!!

    private var level  = 1
    private var words  = listOf<Word>()
    private var idx    = 0
    private var queue  = mutableListOf<Int>()
    private var hint   = false
    private var busy   = false
    private var ttsJob: kotlinx.coroutines.Job? = null
    // Words the user has answered correctly at least once this session
    private val masteredOnce = mutableSetOf<String>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentPracticeBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        level = arguments?.getInt("level", 1) ?: 1
        buildPills(); load(level, restorePosition = true)

        b.btnCheck.setOnClickListener    { check() }
        b.btnHint.setOnClickListener     { hint = !hint; show() }
        b.btnSkip.visibility = View.GONE  // No skipping — must answer correctly once
        b.btnBack.setOnClickListener     { findNavController().navigateUp() }
        b.btnAddGroup.setOnClickListener { showGroupPicker() }
        b.btnSpeak.setOnClickListener    { speakCurrent() }
        b.btnSpeakSlow.setOnClickListener{ speakCurrentSlow() }

        b.etAnswer.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) { check(); true } else false
        }
        vm.xp.observe(viewLifecycleOwner) { _b?.tvXpTop?.text = "⚡ $it XP" }
    }

    private fun speakCurrent() {
        val w = current()
        when (TtsManager.status) {
            TtsManager.Status.READY,
            TtsManager.Status.INTENT_FALLBACK -> TtsManager.speak(w.char)
            TtsManager.Status.LANGUAGE_MISSING -> showTtsSetupDialog()
            TtsManager.Status.INITIALIZING   -> Toast.makeText(context, "TTS loading…", Toast.LENGTH_SHORT).show()
            TtsManager.Status.ERROR          -> showTtsSetupDialog()
            else -> Toast.makeText(context, "TTS unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakCurrentSlow() {
        val w = current()
        when (TtsManager.status) {
            TtsManager.Status.READY          -> TtsManager.speakSlow(w.char)
            TtsManager.Status.LANGUAGE_MISSING -> showTtsSetupDialog()
            else -> speakCurrent()
        }
    }

    private fun showTtsSetupDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Chinese TTS Not Installed")
            .setMessage("To hear Chinese pronunciation offline, install the Google Text-to-Speech engine and download the Chinese (Simplified) language pack.\n\nGo to: Settings → Language & Input → Text-to-Speech → Install voice data → Chinese")
            .setPositiveButton("Open TTS Settings") { _, _ ->
                startActivity(Intent("com.android.settings.TTS_SETTINGS"))
            }
            .setNegativeButton("Later", null)
            .show()
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
            (tv.layoutParams as? LinearLayout.LayoutParams)?.setMargins(0, 0, 12, 0)
        }
    }

    private fun load(l: Int, restorePosition: Boolean = false) {
        level = l; words = HskData.wordsFor(l)
        idx   = if (restorePosition && vm.prefs.lastLevel == l) vm.prefs.lastWordIdx else 0
        queue.clear(); masteredOnce.clear(); hint = false
        buildPills(); show()
    }

    private fun current(): Word {
        val all = words + queue.mapNotNull { words.getOrNull(it) }
        return all.getOrElse(idx % all.size.coerceAtLeast(1)) { words[0] }
    }

    private fun show() {
        if (_b == null) return
        val w = current(); val total = words.size
        b.tvChar.text    = w.char
        b.tvPinyin.text  = if (vm.showPinyin)  w.pinyin  else ""
        b.tvMeaning.text = if (vm.showMeaning) w.meaning else ""
        b.tvCounter.text = "${idx % total + 1} / $total"
        b.pbProgress.max = total; b.pbProgress.progress = idx % total + 1

        val toneColors = listOf("#E8455A","#FB923C","#F5C842","#5B9CF6","#9896C8")
        val toneNames  = listOf("1st Tone ¯","2nd Tone ˊ","3rd Tone ˇ","4th Tone ˋ","Neutral ·")
        val ti = (w.tone - 1).coerceIn(0, 4)
        b.tvTone.text = toneNames[ti]
        b.tvTone.setTextColor(Color.parseColor(toneColors[ti]))

        b.tvHskBadge.text = "HSK $level"
        b.tvHskBadge.setTextColor(Color.parseColor(HskData.COLOR[level] ?: "#F5C842"))
        b.tvReview.visibility = if (queue.isEmpty()) View.GONE else View.VISIBLE
        b.tvReview.text = "⟳ ${queue.size} review"
        b.tvHint.visibility = if (hint) View.VISIBLE else View.GONE
        if (hint) b.tvHint.text = "💡  ${w.meaning.first().uppercaseChar()} — ${w.meaning.length} letters"
        b.etAnswer.setText(""); b.etAnswer.setBackgroundResource(R.drawable.bg_input)

        // Save last position
        vm.prefs.lastLevel   = level
        vm.prefs.lastWordIdx = idx

        // Show TTS status on speak button
        val ttsReady = TtsManager.isReady()
        b.btnSpeak.alpha     = if (ttsReady) 1.0f else 0.5f
        b.btnSpeakSlow.alpha = if (ttsReady) 1.0f else 0.5f

        loadDots(w.id)

        // Auto-pronounce: speak character, then meaning (if shown in settings)
        if (TtsManager.isReady()) {
            ttsJob?.cancel()
            TtsManager.stop()
            TtsManager.speak(w.char)
            if (vm.showMeaning) {
                ttsJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(1200)
                    if (_b != null) TtsManager.speak(w.meaning.split("/").first().trim())
                }
            }
        }
    }

    private fun loadDots(wordId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val att = vm.repo.getAttempt(wordId) ?: return@launch
            if (_b == null) return@launch
            b.llDots.removeAllViews()
            att.history.takeLast(14).forEach { e ->
                val d = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(22, 22).also { it.setMargins(4,0,4,0) }
                    setBackgroundResource(if (e.passed) R.drawable.dot_green else R.drawable.dot_red)
                }
                b.llDots.addView(d)
            }
            if (_b == null) return@launch
            b.tvPassFail.text = "✓${att.pass}  ✗${att.fail}"
        }
    }

    private fun check() {
        if (busy) return
        val input = b.etAnswer.text.toString().trim().lowercase()
        if (input.isEmpty()) return
        val w = current()
        val correct = w.meaning.lowercase()
        val passed  = correct == input
            || correct.split("/").any { it.trim() == input }
            || (correct.contains(input) && input.length > 2)

        vm.record(w.id, "word", level, passed)

        if (passed) {
            // ✓ Correct — mark mastered, flash green, advance
            masteredOnce.add(w.id)
            busy = true
            ttsJob?.cancel()
            TtsManager.stop()
            b.etAnswer.setBackgroundResource(R.drawable.bg_input_ok)
            flashCard()
            if (TtsManager.isReady()) TtsManager.speak(w.char)
            viewLifecycleOwner.lifecycleScope.launch {
                delay(700); if (_b != null) next(true, false); busy = false
            }
        } else {
            // ✗ Wrong — flash red, clear field, stay on this word
            b.etAnswer.setBackgroundResource(R.drawable.bg_input_err)
            viewLifecycleOwner.lifecycleScope.launch {
                delay(600)
                if (_b == null) return@launch
                b.etAnswer.setText("")
                b.etAnswer.setBackgroundResource(R.drawable.bg_input)
                b.etAnswer.requestFocus()
                // Show hint after wrong answer
                hint = true
                b.tvHint.visibility = View.VISIBLE
                b.tvHint.text = "💡  ${w.meaning.first().uppercaseChar()} — ${w.meaning.length} letters"
            }
        }
    }

    private fun next(passed: Boolean, skip: Boolean) {
        val wi = idx % words.size
        if (passed) queue.remove(wi)
        idx++; hint = false; show()
    }

    private fun flashCard() {
        ObjectAnimator.ofArgb(b.cardWord, "cardBackgroundColor",
            0xFF0E0E1C.toInt(), 0xFF1A2200.toInt(), 0xFF0E0E1C.toInt()
        ).apply { duration = 400 }.start()
    }

    private fun showGroupPicker() {
        val wId = current().id
        viewLifecycleOwner.lifecycleScope.launch {
            val groups = vm.allGroups().first()
            val names  = groups.map { it.name }.toMutableList().also { it.add("+ Create New…") }
            if (_b == null) return@launch
            AlertDialog.Builder(requireContext())
                .setTitle("Add to Group")
                .setItems(names.toTypedArray()) { _, i ->
                    if (i == names.size - 1) {
                        val et = android.widget.EditText(requireContext()).apply { hint = "Group name" }
                        AlertDialog.Builder(requireContext()).setTitle("New Group").setView(et)
                            .setPositiveButton("Create") { _, _ ->
                                val n = et.text.toString().trim()
                                if (n.isNotEmpty()) { vm.createGroup(n); vm.addWordToGroup(n, wId) }
                            }.setNegativeButton("Cancel", null).show()
                    } else vm.addWordToGroup(names[i], wId)
                }.show()
        }
    }

    override fun onDestroyView() {
        ttsJob?.cancel()
        TtsManager.stop()
        super.onDestroyView()
        _b = null
    }
}
