package com.iqra.chinese.tts

import android.os.Build

object DeviceInfo {

    enum class OsType { HYPER_OS, MIUI, SAMSUNG, STANDARD }

    fun getOsType(): OsType {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand        = Build.BRAND.lowercase()
        val model        = Build.MODEL.lowercase()

        // HyperOS — 2023+ (MIUI successor)
        val hyperOsVer = getProp("ro.build.version.hyperos")
        if (hyperOsVer.isNotEmpty()) return OsType.HYPER_OS

        // MIUI — check multiple properties (old + new)
        val miuiProps = listOf(
            "ro.miui.ui.version.name",   // e.g. V816, V12, V14
            "ro.miui.ui.version.code",
            "ro.build.miui.ui.version",
            "ro.miui.version.code",
            "ro.miui.cust_variant",
            "ro.miui.region"
        )
        if (miuiProps.any { getProp(it).isNotEmpty() }) return OsType.MIUI

        // Xiaomi by manufacturer/brand even without MIUI props
        val xiaomiBrands = listOf("xiaomi","redmi","poco","blackshark","civi")
        if (manufacturer == "xiaomi" || brand in xiaomiBrands ||
            model.startsWith("mi ") || model.startsWith("redmi")) return OsType.MIUI

        // Samsung
        if (manufacturer == "samsung") return OsType.SAMSUNG

        return OsType.STANDARD
    }

    fun isXiaomi()  = getOsType() in listOf(OsType.HYPER_OS, OsType.MIUI)
    fun isHyperOs() = getOsType() == OsType.HYPER_OS

    /** Read system property via reflection — no root needed for public props */
    fun getProp(key: String): String = try {
        val cls    = Class.forName("android.os.SystemProperties")
        val method = cls.getMethod("get", String::class.java, String::class.java)
        (method.invoke(null, key, "") as? String)?.trim() ?: ""
    } catch (e: Exception) { "" }

    fun getMiuiVersion(): String {
        // V816 format = MIUI 8.x  |  V14 = MIUI 14  |  etc.
        val raw = getProp("ro.miui.ui.version.name")
        if (raw.isEmpty()) return ""
        // V816 → "8.16", V14 → "14"
        return if (raw.startsWith("V")) raw.substring(1) else raw
    }

    fun getOsDescription(): String {
        val type     = getOsType()
        val hyperVer = getProp("ro.build.version.hyperos")
        val miuiVer  = getProp("ro.miui.ui.version.name")
        val brand    = Build.BRAND.replaceFirstChar { it.uppercase() }
        return when (type) {
            OsType.HYPER_OS -> "$brand HyperOS${if (hyperVer.isNotEmpty()) " $hyperVer" else ""}"
            OsType.MIUI     -> "$brand MIUI${if (miuiVer.isNotEmpty()) " $miuiVer" else ""}"
            OsType.SAMSUNG  -> "Samsung One UI (Android ${Build.VERSION.RELEASE})"
            OsType.STANDARD -> "${Build.MANUFACTURER.replaceFirstChar{it.uppercase()}} Android ${Build.VERSION.RELEASE}"
        }
    }
}
