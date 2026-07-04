package com.iqra.chinese.ui

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.iqra.chinese.R
import com.iqra.chinese.data.HskData
import com.iqra.chinese.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        observeHeader()
        buildLevelCards()
        buildAchievements()
    }

    // All flows collected with repeatOnLifecycle(STARTED) so they cancel
    // automatically when the view goes to STOPPED/DESTROYED — no NPE possible.
    private fun observeHeader() {
        vm.xp.observe(viewLifecycleOwner)     { _b?.tvXP?.text     = "⚡ $it XP" }
        vm.streak.observe(viewLifecycleOwner) { _b?.tvStreak?.text = "🔥 $it" }
        vm.daily.observe(viewLifecycleOwner)  {
            val m = it / 60; val sec = it % 60
            _b?.tvDaily?.text     = "${m}m ${sec.toString().padStart(2,'0')}s / 30m"
            _b?.pbDaily?.progress = minOf(100, it * 100 / 1800)
        }

        // Safe flow collection — cancels when lifecycle drops below STARTED
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.masteredCount().collect { count ->
                    _b?.tvMastered?.text = count.toString()
                }
            }
        }
    }

    private fun buildLevelCards() {
        _b?.llLevels?.removeAllViews() ?: return
        for (lvl in 1..6) {
            val locked = !vm.unlocked.contains(lvl)
            val card   = layoutInflater.inflate(R.layout.item_level_card, b.llLevels, false)
            val tvName    = card.findViewById<TextView>(R.id.tvLevelName)
            val tvInfo    = card.findViewById<TextView>(R.id.tvLevelInfo)
            val pb        = card.findViewById<ProgressBar>(R.id.pbMastery)
            val tvPct     = card.findViewById<TextView>(R.id.tvPct)
            val btnUnlock = card.findViewById<Button>(R.id.btnUnlock)
            val btnSent   = card.findViewById<Button>(R.id.btnSentences)
            val color     = HskData.COLOR[lvl] ?: "#F5C842"

            tvName.text = "HSK Level $lvl"
            tvName.setTextColor(if (locked) 0xFF4A4870.toInt() else Color.parseColor(color))
            card.alpha = if (locked) 0.38f else 1f

            if (!locked) {
                // Mastery flow — safe with repeatOnLifecycle
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        vm.masteryForLevel(lvl).collect { (mastered, total) ->
                            if (_b == null) return@collect
                            val pct = if (total > 0) mastered * 100 / total else 0
                            tvInfo.text    = "$mastered / $total sample words mastered"
                            pb.progress    = pct
                            tvPct.text     = "$pct%"
                            val next = lvl + 1
                            if (pct >= 80 && next <= 6 && !vm.unlocked.contains(next)) {
                                btnUnlock.visibility = View.VISIBLE
                                btnUnlock.text = "🔓 Unlock HSK $next"
                                btnUnlock.setBackgroundColor(
                                    Color.parseColor(HskData.COLOR[next] ?: "#FB923C"))
                                btnUnlock.setOnClickListener {
                                    vm.unlockLevel(next)
                                    Toast.makeText(context,
                                        "HSK $next Unlocked! 🎉", Toast.LENGTH_SHORT).show()
                                    buildLevelCards()
                                }
                            } else {
                                btnUnlock.visibility = View.GONE
                            }
                        }
                    }
                }

                // Practice tap
                card.setOnClickListener {
                    findNavController().navigate(
                        R.id.action_home_to_practice, bundleOf("level" to lvl))
                }

                // Sentences button
                btnSent?.visibility = View.VISIBLE
                btnSent?.setOnClickListener {
                    findNavController().navigate(
                        R.id.action_home_to_sentences, bundleOf("level" to lvl))
                }

            } else {
                tvInfo.text          = "🔒 Complete HSK ${lvl - 1} first"
                pb.progress          = 0
                tvPct.text           = "0%"
                btnUnlock.visibility = View.GONE
                btnSent?.visibility  = View.GONE
            }

            b.llLevels.addView(card)
        }
    }

    private fun buildAchievements() {
        _b?.llAchievements?.removeAllViews() ?: return
        val earned = vm.repo.prefs.earned
        HskData.ACHIEVEMENTS.forEach { a ->
            val got = earned.contains(a.id)
            val row = layoutInflater.inflate(R.layout.item_achievement, b.llAchievements, false)
            row.findViewById<TextView>(R.id.tvAchEmoji).text = a.emoji
            row.findViewById<TextView>(R.id.tvAchTitle).text = a.title
            row.findViewById<TextView>(R.id.tvAchDesc).text  = a.desc
            row.alpha = if (got) 1f else 0.3f
            if (got) row.findViewById<TextView>(R.id.tvAchXP).apply {
                visibility = View.VISIBLE
                text = "+${a.xp}XP"
            }
            b.llAchievements.addView(row)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
