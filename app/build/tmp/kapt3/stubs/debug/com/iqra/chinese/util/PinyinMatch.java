package com.iqra.chinese.util;

/**
 * Normalizes pinyin input for tone-flexible comparison — strips diacritic
 * tone marks, tone digits, spaces and apostrophes so a learner's typed
 * pinyin matches the stored form whether or not they included tones.
 *
 * Examples that all normalize to "nihao":
 *  "nǐ hǎo", "nihao", "ni hao", "ni3hao3", "NI HAO"
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\f\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tJ\u000e\u0010\u000b\u001a\u00020\t2\u0006\u0010\f\u001a\u00020\tR\u001a\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/iqra/chinese/util/PinyinMatch;", "", "()V", "toneMap", "", "", "matches", "", "input", "", "correct", "normalize", "s", "app_debug"})
public final class PinyinMatch {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.Character, java.lang.Character> toneMap = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.iqra.chinese.util.PinyinMatch INSTANCE = null;
    
    private PinyinMatch() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String normalize(@org.jetbrains.annotations.NotNull()
    java.lang.String s) {
        return null;
    }
    
    public final boolean matches(@org.jetbrains.annotations.NotNull()
    java.lang.String input, @org.jetbrains.annotations.NotNull()
    java.lang.String correct) {
        return false;
    }
}