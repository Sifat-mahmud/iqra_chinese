package com.iqra.chinese.ui

import android.app.Activity
import android.content.Context
import android.view.View
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.iqra.chinese.R

/**
 * Single linear first-login tour shown once from MainActivity.
 * All targets are views visible on the Home screen + bottom nav tabs.
 *
 * Flow:
 *  1. 🔥 Streak — explain daily streak
 *  2. 📊 Daily bar — explain study goal
 *  3. 📚 Level cards — tap to start word cards
 *  4. 📖 Sentence tab — bottom nav highlight
 *  5. ⚙️ Settings tab — toggle pinyin/meaning
 *  6. 📈 Stats tab — see progress
 *  7. 🏠 Home tab — welcome, let's go!
 */
object AppTour {

    private const val PREFS = "iqra_meta"
    private const val KEY   = "tour_done_v2"

    fun isDone(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, false)

    private fun markDone(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY, true).apply()

    fun resetAll(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY, false).apply()

    fun startTour(
        activity:    Activity,
        wordCardArea: View,
        dailyBar:    View,
        streakView:  View,
        sentenceTab: View,
        settingsTab: View,
        statsTab:    View,
        homeTab:     View
    ) {
        if (isDone(activity)) return

        TapTargetSequence(activity)
            .targets(
                t(activity, streakView,
                    "🔥 Daily Streak",
                    "Study every day to keep your streak alive. Missing a day resets it to zero — don't break the chain!"),
                t(activity, dailyBar,
                    "📊 Daily Goal",
                    "This bar tracks your study time today. Complete it to silence the alarm. You can change the target in Settings."),
                t(activity, wordCardArea,
                    "📚 Word Cards",
                    "Tap any HSK level to open its word cards. A Chinese character appears — hear it spoken, then type its meaning to answer."),
                t(activity, sentenceTab,
                    "📖 Sentence Cards",
                    "Practice full HSK example sentences here. Audio plays automatically — great for listening and reading comprehension."),
                t(activity, settingsTab,
                    "⚙️ Settings",
                    "Toggle pinyin and meaning hints on/off, set your daily goal, configure when your study alarm fires, and sync your progress to the cloud."),
                t(activity, statsTab,
                    "📈 Your Progress",
                    "See words mastered, time studied, accuracy, and your last 7 days — all in one place."),
                t(activity, homeTab,
                    "🎉 Welcome to Iqra Chinese!",
                    "You're all set! Study daily, build your streak, and master all 6 HSK levels. 加油 — let's go!")
            )
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish()                    { markDone(activity) }
                override fun onSequenceStep(t: TapTarget, b: Boolean) {}
                override fun onSequenceCanceled(t: TapTarget)      { markDone(activity) }
            })
            .start()
    }

    private fun t(activity: Activity, view: View, title: String, desc: String) =
        TapTarget.forView(view, title, desc)
            .outerCircleColor(R.color.violet)
            .outerCircleAlpha(0.93f)
            .targetCircleColor(android.R.color.white)
            .titleTextSize(18)
            .titleTextColor(android.R.color.white)
            .descriptionTextSize(13)
            .descriptionTextColor(android.R.color.white)
            .dimColor(android.R.color.black)
            .drawShadow(true)
            .cancelable(false)
            .tintTarget(false)
            .transparentTarget(false)
}
