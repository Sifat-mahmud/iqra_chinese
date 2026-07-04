package com.iqra.chinese.ui

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iqra.chinese.R
import com.iqra.chinese.data.*
import com.iqra.chinese.databinding.FragmentTestBinding
import com.iqra.chinese.firebase.FirebaseManager
import com.iqra.chinese.tts.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestFragment : BaseFragment() {
    private var _b: FragmentTestBinding? = null
    private val b get() = _b!!
    private var level = 1; private var testType = "words"; private var mode = "meaning"
    private var testWords = listOf<Word>(); private var testSents = listOf<Sentence>()
    private var idx = 0; private var pass = 0; private var fail = 0
    private var wrongWords = mutableListOf<Word>(); private var busy = false; private var started = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentTestBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        showSetup()
        b.btnBack.setOnClickListener { if (started) showSetup() else findNavController().navigateUp() }
        // btnTestBack is the back button inside the active test screen
        b.btnTestBack.setOnClickListener { showSetup() }
    }

    private fun showSetup() {
        started = false
        b.layoutSetup.visibility   = View.VISIBLE
        b.layoutTest.visibility    = View.GONE
        b.layoutResult.visibility  = View.GONE
        buildPills()
        b.tabWords.setOnClickListener     { testType = "words"; updateTabs() }
        b.tabSentences.setOnClickListener { testType = "sentences"; updateTabs() }
        b.btnMeaning.setOnClickListener   { mode = "meaning"; updateModes() }
        b.btnCharacter.setOnClickListener { mode = "character"; updateModes() }
        b.btnStart.setOnClickListener     { startTest() }
        updateTabs(); updateModes()
    }

    private fun buildPills() {
        b.llPills.removeAllViews()
        vm.unlocked.forEach { l ->
            val tv = TextView(requireContext()).apply {
                text = "HSK $l"; textSize = 11f; setPadding(28, 10, 28, 10)
                val c = HskData.COLOR[l] ?: "#F5C842"
                if (l == level) { setBackgroundColor(Color.parseColor(c)); setTextColor(Color.BLACK) }
                else { setTextColor(Color.parseColor(c)); setBackgroundResource(R.drawable.bg_pill) }
                setOnClickListener { level = l; buildPills() }
            }
            b.llPills.addView(tv)
        }
    }

    private fun updateTabs() {
        val gold = 0xFFF5C842.toInt()
        val dim  = 0xFF151526.toInt()
        b.tabWords.setBackgroundColor(     if (testType == "words")     gold else dim)
        b.tabSentences.setBackgroundColor( if (testType == "sentences") gold else dim)
        b.tabWords.setTextColor(    if (testType == "words")     Color.BLACK else 0xFF9896C8.toInt())
        b.tabSentences.setTextColor(if (testType == "sentences") Color.BLACK else 0xFF9896C8.toInt())
    }

    private fun updateModes() {
        val gold = 0xFFF5C842.toInt(); val dim = 0xFF151526.toInt()
        b.btnMeaning.setBackgroundColor(   if (mode == "meaning")   gold else dim)
        b.btnCharacter.setBackgroundColor( if (mode == "character") gold else dim)
        b.btnMeaning.setTextColor(   if (mode == "meaning")   Color.BLACK else 0xFF9896C8.toInt())
        b.btnCharacter.setTextColor( if (mode == "character") Color.BLACK else 0xFF9896C8.toInt())
    }

    private fun startTest() {
        idx = 0; pass = 0; fail = 0; wrongWords.clear(); started = true
        testWords = HskData.wordsFor(level).shuffled().take(20)
        testSents = HskData.sentencesFor(level).shuffled().take(20)
        b.layoutSetup.visibility  = View.GONE
        b.layoutTest.visibility   = View.VISIBLE
        b.layoutResult.visibility = View.GONE
        showQ()
        b.btnTestCheck.setOnClickListener { checkAnswer() }
        b.btnTestSkip.setOnClickListener  { advanceQ() }
        b.etTest.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) { checkAnswer(); true } else false
        }
    }

    private val totalQ get() = if (testType == "words") testWords.size else testSents.size

    private fun showQ() {
        if (idx >= totalQ) { showResult(); return }
        b.pbTest.max = totalQ; b.pbTest.progress = idx + 1
        b.tvCounter.text = "${idx+1} / $totalQ"
        b.tvScore.text   = "✓$pass  ✗$fail"
        if (testType == "words") {
            val w = testWords[idx]
            when (mode) {
                "meaning" -> { b.tvQuestion.text = w.char; b.tvSub.text = w.pinyin; b.etTest.hint = "Type meaning…" }
                else      -> { b.tvQuestion.text = w.meaning; b.tvSub.text = ""; b.etTest.hint = "Type character…" }
            }
        } else {
            val s = testSents[idx]
            b.tvQuestion.text = s.char; b.tvSub.text = if (vm.showPinyin) s.pinyin else ""
            b.etTest.hint = "Type meaning…"
        }
        b.etTest.setText(""); b.etTest.setBackgroundResource(R.drawable.bg_input)

        // Wire speak buttons
        val speakText = if (testType == "words" && idx < testWords.size) testWords[idx].char
                        else if (testType == "sentences" && idx < testSents.size) testSents[idx].char
                        else ""
        b.btnSpeak.setOnClickListener {
            if (TtsManager.isReady() && speakText.isNotEmpty()) TtsManager.speak(speakText)
        }
        b.btnSpeakSlow.setOnClickListener {
            if (TtsManager.isReady() && speakText.isNotEmpty()) TtsManager.speakSlow(speakText)
        }
        b.btnSpeak.alpha     = if (TtsManager.isReady()) 1f else 0.5f
        b.btnSpeakSlow.alpha = if (TtsManager.isReady()) 1f else 0.5f
    }

    private fun checkAnswer() {
        if (busy) return
        val input = b.etTest.text.toString().trim().lowercase(); if (input.isEmpty()) return
        busy = true
        val ok = if (testType == "words") {
            val w = testWords[idx]
            val target = if (mode == "meaning") w.meaning.lowercase() else w.char.lowercase()
            target == input || target.split("/").any { it.trim() == input } || (target.contains(input) && input.length > 2)
        } else {
            val s = testSents[idx]
            val ws = s.meaning.lowercase().split(" ").filter { it.length > 2 }
            ws.isEmpty() || ws.count { input.contains(it.take(3)) }.toFloat() / ws.size >= 0.6f
        }
        b.etTest.setBackgroundResource(if (ok) R.drawable.bg_input_ok else R.drawable.bg_input_err)
        if (ok && TtsManager.isReady()) {
            val charToSpeak = if (testType == "words" && idx < testWords.size) testWords[idx].char
                              else if (testType == "sentences" && idx < testSents.size) testSents[idx].char
                              else ""
            if (charToSpeak.isNotEmpty()) TtsManager.speak(charToSpeak)
        }
        if (ok) pass++ else { fail++; if (testType == "words") wrongWords.add(testWords[idx]) }
        val itemId = if (testType == "words") testWords[idx].id else testSents[idx].id
        vm.record(itemId, testType.dropLast(1), level, ok)
        viewLifecycleOwner.lifecycleScope.launch { delay(700); if (_b != null) advanceQ(); busy = false }
    }

    private fun advanceQ() { idx++; if (idx >= totalQ) showResult() else showQ() }

    private fun showResult() {
        b.layoutTest.visibility   = View.GONE
        b.layoutResult.visibility = View.VISIBLE
        val pct = if (totalQ > 0) pass * 100 / totalQ else 0
        vm.setBestTest(pct)
        FirebaseManager.logTestCompleted(level, pct, testType)
        b.tvResultEmoji.text = when { pct >= 90 -> "🏆"; pct >= 70 -> "📖"; pct >= 50 -> "💪"; else -> "🔄" }
        b.tvResultPct.text    = "$pct%"
        b.tvResultDetail.text = "$pass correct · $fail wrong"
        b.tvResultMsg.text    = when { pct >= 90 -> "Outstanding! 太棒了！"; pct >= 70 -> "Great work! 很好！"; pct >= 50 -> "Keep going! 继续！"; else -> "Practice more! 再练习！" }
        b.llWrong.removeAllViews()
        if (wrongWords.isNotEmpty()) {
            wrongWords.take(5).forEach { ww ->
                val tv = TextView(requireContext()).apply {
                    text = "${ww.char}  ${ww.pinyin}  —  ${ww.meaning}"
                    textSize = 13f; setPadding(0, 8, 0, 8); setTextColor(0xFFF2F0FF.toInt())
                }
                b.llWrong.addView(tv)
            }
        }
        b.btnTryAgain.setOnClickListener { startTest() }
        b.btnResultBack.setOnClickListener { showSetup() }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
