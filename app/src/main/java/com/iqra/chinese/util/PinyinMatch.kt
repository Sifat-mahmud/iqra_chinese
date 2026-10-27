package com.iqra.chinese.util

/**
 * Normalizes pinyin input for tone-flexible comparison — strips diacritic
 * tone marks, tone digits, spaces and apostrophes so a learner's typed
 * pinyin matches the stored form whether or not they included tones.
 *
 * Examples that all normalize to "nihao":
 *   "nǐ hǎo", "nihao", "ni hao", "ni3hao3", "NI HAO"
 */
object PinyinMatch {
    private val toneMap = mapOf(
        'ā' to 'a','á' to 'a','ǎ' to 'a','à' to 'a',
        'ē' to 'e','é' to 'e','ě' to 'e','è' to 'e',
        'ī' to 'i','í' to 'i','ǐ' to 'i','ì' to 'i',
        'ō' to 'o','ó' to 'o','ǒ' to 'o','ò' to 'o',
        'ū' to 'u','ú' to 'u','ǔ' to 'u','ù' to 'u',
        'ǖ' to 'u','ǘ' to 'u','ǚ' to 'u','ǜ' to 'u','ü' to 'u'
    )

    fun normalize(s: String): String = s.lowercase()
        .map { toneMap[it] ?: it }
        .joinToString("")
        .replace(Regex("[^a-z]"), "")   // strip digits, spaces, and all punctuation
                                          // (periods, commas, ?, !, ', etc.) —
                                          // only letters matter for matching

    fun matches(input: String, correct: String): Boolean =
        normalize(input) == normalize(correct)
}
