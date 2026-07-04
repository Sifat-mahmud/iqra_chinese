package com.iqra.chinese.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.iqra.chinese.R
import com.iqra.chinese.data.*
import com.iqra.chinese.databinding.FragmentStatsBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StatsFragment : BaseFragment() {
    private var _b: FragmentStatsBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentStatsBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        vm.xp.observe(viewLifecycleOwner)     { b.tvXP.text = "⚡ $it XP" }
        vm.streak.observe(viewLifecycleOwner) { b.tvStreak.text = "🔥 $it days" }
        vm.daily.observe(viewLifecycleOwner)  {
            val m = it / 60; val s = it % 60
            b.tvDaily.text = "${m}m ${s.toString().padStart(2,'0')}s / 30m"
            b.pbDaily.progress = minOf(100, it * 100 / 1800)
        }
        b.tvBest.text = "🏆 Best test: ${vm.bestTest}%"

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.allAttempts().collect { atts ->
                if (_b == null) return@collect
                val tp = atts.sumOf { it.pass }
                val tf = atts.sumOf { it.fail }
                val mastered = atts.count { it.pass > 0 && it.fail == 0 }
                val acc = if (tp + tf > 0) tp * 100 / (tp + tf) else 0
                b.tvAttempts.text = "${tp + tf}"
                b.tvAccuracy.text = "$acc%"
                b.tvMastered.text = "$mastered"

                // Pass/fail bar
                val total = (tp + tf).toFloat().coerceAtLeast(1f)
                b.pbPassFail.progress = (tp / total * 100).toInt()
                b.tvPass.text = "✓ $tp"; b.tvFail.text = "✗ $tf"

                // Weekly chart
                val days = listOf("M","T","W","T","F","S","S")
                val now  = System.currentTimeMillis()
                val vals = (0..6).map { i ->
                    val s = now - (6 - i) * 86_400_000L
                    atts.sumOf { r -> r.history.count { h -> h.ts in s until s + 86_400_000L && h.passed } }.toFloat()
                }
                b.barChart.setData(vals, days)

                // Level progress rows
                b.llLevelProgress.removeAllViews()
                vm.unlocked.forEach { l ->
                    val row = layoutInflater.inflate(R.layout.item_level_progress, b.llLevelProgress, false)
                    val tv  = row.findViewById<TextView>(R.id.tvLvlName)
                    val pb  = row.findViewById<android.widget.ProgressBar>(R.id.pbLvl)
                    val tvP = row.findViewById<TextView>(R.id.tvLvlPct)
                    tv.text  = "HSK $l"
                    tv.setTextColor(Color.parseColor(HskData.COLOR[l] ?: "#F5C842"))
                    val lvlAtts  = atts.filter { it.level == l && it.itemType == "word" }
                    val m  = lvlAtts.count { it.pass > 0 }
                    val total2 = HskData.wordsFor(l).size
                    val pct = if (total2 > 0) m * 100 / total2 else 0
                    pb.progress = pct; tvP.text = "$m/$total2 ($pct%)"
                    b.llLevelProgress.addView(row)
                }

                // Recent list
                b.llRecent.removeAllViews()
                if (atts.isEmpty()) {
                    val tv = TextView(requireContext()).apply {
                        text = "No attempts yet – start practicing!"; textSize = 13f
                        setTextColor(0xFF4A4870.toInt()); setPadding(0,16,0,0)
                    }
                    b.llRecent.addView(tv)
                } else {
                    atts.sortedByDescending { it.lastTs }.take(8).forEach { rec ->
                        val word = HskData.wordById(rec.itemId)
                        val tv   = TextView(requireContext()).apply {
                            val ch = word?.char ?: rec.itemId.substringAfterLast("_")
                            text = "$ch  (HSK ${rec.level})   ✓${rec.pass}  ✗${rec.fail}"
                            textSize = 13f; setPadding(0, 10, 0, 10); setTextColor(0xFFF2F0FF.toInt())
                        }
                        b.llRecent.addView(tv)
                        val div = View(requireContext()).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
                            setBackgroundColor(0xFF2A2848.toInt())
                        }
                        b.llRecent.addView(div)
                    }
                }
            }  // collect
            }  // repeatOnLifecycle
        }  // launch
    }  // onViewCreated


    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── Custom BarChart View (no library needed) ──────────────────────────────────
class BarChartView @JvmOverloads constructor(ctx: android.content.Context, attrs: AttributeSet? = null) : View(ctx, attrs) {
    private var vals  = listOf<Float>()
    private var labels = listOf<String>()
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#A78BFA") }
    private val lblPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#4A4870"); textSize = 28f; textAlign = Paint.Align.CENTER }
    private val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#9896C8"); textSize = 26f; textAlign = Paint.Align.CENTER }

    fun setData(v: List<Float>, l: List<String>) { vals = v; labels = l; invalidate() }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if (vals.isEmpty()) return
        val maxV = vals.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val barW = width.toFloat() / vals.size
        val chartH = height - 50f
        vals.forEachIndexed { i, v ->
            val bh  = (v / maxV) * chartH * 0.85f + 4f
            val left = i * barW + barW * 0.1f
            val right = (i + 1) * barW - barW * 0.1f
            val top  = chartH - bh
            c.drawRoundRect(left, top, right, chartH, 6f, 6f, barPaint)
            c.drawText(labels.getOrElse(i) { "" }, left + (right - left) / 2, height - 8f, lblPaint)
            if (v > 0) c.drawText(v.toInt().toString(), left + (right - left) / 2, top - 6f, valPaint)
        }
    }
}
