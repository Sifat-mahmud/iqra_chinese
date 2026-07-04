package com.iqra.chinese.ui

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iqra.chinese.R
import com.iqra.chinese.data.Word
import com.iqra.chinese.databinding.FragmentGroupPracticeBinding
import com.iqra.chinese.tts.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GroupPracticeFragment : BaseFragment() {
    private var _b: FragmentGroupPracticeBinding? = null
    private val b get() = _b!!
    private var words = listOf<Word>(); private var idx = 0; private var busy = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentGroupPracticeBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        val name = arguments?.getString("name") ?: ""
        b.tvTitle.text = name
        viewLifecycleOwner.lifecycleScope.launch {
            val g = vm.allGroups().first().find { it.name == name }
            words = if (g != null) vm.wordsForGroup(g) else emptyList()
            b.tvCount.text = "${words.size} words"
            if (words.isEmpty()) b.tvEmpty.visibility = View.VISIBLE else show()
        }
        b.btnCheck.setOnClickListener    { check() }
        b.btnSkip.setOnClickListener     { idx++; show() }
        b.btnBack.setOnClickListener     { findNavController().navigateUp() }
        b.btnSpeak.setOnClickListener    {
            if (words.isEmpty()) return@setOnClickListener
            val w = words[idx % words.size]
            if (TtsManager.isReady()) TtsManager.speak(w.char)
            else Toast.makeText(context,"TTS not ready",Toast.LENGTH_SHORT).show()
        }
        b.btnSpeakSlow.setOnClickListener {
            if (words.isEmpty()) return@setOnClickListener
            val w = words[idx % words.size]
            if (TtsManager.isReady()) TtsManager.speakSlow(w.char)
        }
        b.etAnswer.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) { check(); true } else false
        }
    }

    private fun show() {
        if (words.isEmpty() || _b == null) return
        val w = words[idx % words.size]
        b.tvChar.text    = w.char
        b.tvPinyin.text  = if (vm.showPinyin)  w.pinyin  else ""
        b.tvMeaning.text = if (vm.showMeaning) w.meaning else ""
        b.tvCounter.text = "${idx % words.size + 1} / ${words.size}"
        b.etAnswer.setText(""); b.etAnswer.setBackgroundResource(R.drawable.bg_input)
        b.btnSpeak.alpha     = if (TtsManager.isReady()) 1f else 0.5f
        b.btnSpeakSlow.alpha = if (TtsManager.isReady()) 1f else 0.5f
    }

    private fun check() {
        if (busy || words.isEmpty()) return
        val input = b.etAnswer.text.toString().trim().lowercase(); if (input.isEmpty()) return
        val w  = words[idx % words.size]
        val ok = w.meaning.lowercase().let { m ->
            m == input || m.split("/").any { it.trim() == input } || (m.contains(input) && input.length > 1)
        }
        busy = true
        b.etAnswer.setBackgroundResource(if (ok) R.drawable.bg_input_ok else R.drawable.bg_input_err)
        if (ok && TtsManager.isReady()) TtsManager.speak(w.char)
        vm.record(w.id, "word", w.level, ok)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(700); if (_b != null) { idx++; show() }; busy = false
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
