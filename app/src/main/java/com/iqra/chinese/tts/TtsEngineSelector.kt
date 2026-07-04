package com.iqra.chinese.tts

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log

object TtsEngineSelector {

    private val SCORES = mapOf(
        "com.iflytek.speechcloud"     to 100,
        "com.iflytek.tts"             to 99,
        "com.iflytek.speechclouddemo" to 95,
        "com.xiaomi.speech"           to 92,
        "com.xiaomi.ai.engine"        to 91,
        "com.miui.tts"                to 90,
        "com.xiaomi.tts"              to 89,
        "com.baidu.tts"               to 80,
        "com.baidu.duersdk.opensdk"   to 79,
        "com.google.android.tts"      to 70,
        "com.samsung.SMT"             to 60,
        "com.samsung.android.tts"     to 59,
        "com.svox.pico"               to 10
    )

    data class EngineChoice(
        val packageName: String?,
        val engineName: String,
        val deviceOs: String
    )

    fun buildEngineQueue(context: Context): List<EngineChoice> {
        val osDesc   = DeviceInfo.getOsDescription()
        val pm       = context.packageManager
        val isXiaomi = DeviceInfo.isXiaomi()
        val queue    = mutableListOf<EngineChoice>()
        val seen     = mutableSetOf<String?>()

        // Strategy 1: Query services with android.intent.action.TTS_SERVICE intent
        // This finds system-registered TTS services including Mi AI Speech Engine
        val ttsServices = queryTtsServices(pm)
        Log.i("TtsSelector", "TTS_SERVICE intent found: ${ttsServices.map { it.serviceInfo.packageName }}")

        val scored = ttsServices
            .map { ri ->
                val pkg   = ri.serviceInfo.packageName
                val label = ri.loadLabel(pm).toString()
                Triple(pkg, label, SCORES[pkg] ?: scoreByLabel(label))
            }
            .sortedByDescending { it.third }

        for ((pkg, label, _) in scored) {
            if (pkg !in seen) {
                seen.add(pkg)
                queue.add(EngineChoice(pkg, label.ifEmpty { labelOf(pkg) }, osDesc))
                Log.i("TtsSelector", "  + intent: $label ($pkg)")
            }
        }

        // Strategy 2: PackageManager scan of known packages
        for ((pkg, _) in SCORES.entries.sortedByDescending { it.value }) {
            if (pkg !in seen && isInstalled(pm, pkg)) {
                seen.add(pkg)
                queue.add(EngineChoice(pkg, labelOf(pkg), osDesc))
                Log.i("TtsSelector", "  + pm: $pkg")
            }
        }

        // Strategy 3: system default (null)
        // On Xiaomi: try FIRST (Mi AI may be system default)
        // Always also add as last resort
        if (isXiaomi) {
            queue.add(0, EngineChoice(null, "Mi AI (system default)", osDesc))
        }
        queue.add(EngineChoice(null, "System Default TTS", osDesc))

        Log.i("TtsSelector", "Queue: ${queue.map { it.engineName }}")
        return queue
    }

    private fun queryTtsServices(pm: PackageManager): List<ResolveInfo> {
        return try {
            pm.queryIntentServices(
                Intent("android.intent.action.TTS_SERVICE"),
                PackageManager.GET_META_DATA
            ) ?: emptyList()
        } catch (e: Exception) {
            Log.e("TtsSelector", "queryTtsServices: ${e.message}")
            emptyList()
        }
    }

    fun selectBestEngine(context: Context) = buildEngineQueue(context).first()

    private fun isInstalled(pm: PackageManager, pkg: String) = try {
        pm.getPackageInfo(pkg, 0); true
    } catch (e: PackageManager.NameNotFoundException) { false }

    fun scoreByLabel(label: String): Int {
        val l = label.lowercase()
        return when {
            "iflytek" in l || "讯飞" in l               -> 95
            "mi ai"   in l || "xiaomi" in l || "小米" in l -> 90
            "miui"    in l                               -> 88
            "baidu"   in l || "百度" in l                -> 78
            "google"  in l                               -> 70
            "samsung" in l                               -> 60
            else                                         -> 30
        }
    }

    fun labelOf(pkg: String) = when {
        "iflytek" in pkg                    -> "iFlytek TTS"
        "xiaomi"  in pkg || "speech" in pkg -> "Mi AI Speech Engine"
        "miui"    in pkg                    -> "MIUI TTS"
        "google"  in pkg                    -> "Google TTS"
        "samsung" in pkg                    -> "Samsung TTS"
        "baidu"   in pkg                    -> "Baidu TTS"
        "svox"    in pkg                    -> "AOSP Pico TTS"
        else                                -> pkg.substringAfterLast(".")
    }
}
