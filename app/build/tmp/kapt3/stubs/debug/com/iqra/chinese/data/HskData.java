package com.iqra.chinese.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\u0010\b\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000e\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00190\u00042\u0006\u0010\u001f\u001a\u00020\u000bJ\u0014\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00160\u00042\u0006\u0010!\u001a\u00020\nJ\u0010\u0010\"\u001a\u0004\u0018\u00010\u00192\u0006\u0010#\u001a\u00020\u000bJ\u0010\u0010$\u001a\u0004\u0018\u00010\u00192\u0006\u0010%\u001a\u00020\u000bJ\u0014\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00190\u00042\u0006\u0010!\u001a\u00020\nR\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u001d\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000b0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u001d\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\rR\u001b\u0010\u0010\u001a\u00020\n8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0013\u0010\u0014\u001a\u0004\b\u0011\u0010\u0012R#\u0010\u0015\u001a\u0014\u0012\u0004\u0012\u00020\n\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00040\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\rR#\u0010\u0018\u001a\u0014\u0012\u0004\u0012\u00020\n\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u00040\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\rR\'\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00190\t8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001d\u0010\u0014\u001a\u0004\b\u001c\u0010\r\u00a8\u0006\'"}, d2 = {"Lcom/iqra/chinese/data/HskData;", "", "()V", "ACHIEVEMENTS", "", "Lcom/iqra/chinese/data/Achievement;", "getACHIEVEMENTS", "()Ljava/util/List;", "COLOR", "", "", "", "getCOLOR", "()Ljava/util/Map;", "TOTAL", "getTOTAL", "maxWordLen", "getMaxWordLen", "()I", "maxWordLen$delegate", "Lkotlin/Lazy;", "sentences", "Lcom/iqra/chinese/data/Sentence;", "getSentences", "words", "Lcom/iqra/chinese/data/Word;", "getWords", "wordsByChar", "getWordsByChar", "wordsByChar$delegate", "segmentSentence", "text", "sentencesFor", "level", "wordByChar", "char", "wordById", "id", "wordsFor", "app_debug"})
public final class HskData {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.Integer, java.lang.String> COLOR = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.Integer, java.lang.Integer> TOTAL = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.Integer, java.util.List<com.iqra.chinese.data.Word>> words = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.Integer, java.util.List<com.iqra.chinese.data.Sentence>> sentences = null;
    
    /**
     * Flat index of every word's exact character text → Word, built once.
     * When the same text exists at multiple levels, the LOWEST level wins
     * (levels map is iterated 1→6 in order, first match kept).
     */
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy wordsByChar$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy maxWordLen$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<com.iqra.chinese.data.Achievement> ACHIEVEMENTS = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.iqra.chinese.data.HskData INSTANCE = null;
    
    private HskData() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, java.lang.String> getCOLOR() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, java.lang.Integer> getTOTAL() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, java.util.List<com.iqra.chinese.data.Word>> getWords() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, java.util.List<com.iqra.chinese.data.Sentence>> getSentences() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.iqra.chinese.data.Word> wordsFor(int level) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.iqra.chinese.data.Sentence> sentencesFor(int level) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.iqra.chinese.data.Word wordById(@org.jetbrains.annotations.NotNull()
    java.lang.String id) {
        return null;
    }
    
    /**
     * Flat index of every word's exact character text → Word, built once.
     * When the same text exists at multiple levels, the LOWEST level wins
     * (levels map is iterated 1→6 in order, first match kept).
     */
    private final java.util.Map<java.lang.String, com.iqra.chinese.data.Word> getWordsByChar() {
        return null;
    }
    
    private final int getMaxWordLen() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.iqra.chinese.data.Word wordByChar(@org.jetbrains.annotations.NotNull()
    java.lang.String p0_1526187) {
        return null;
    }
    
    /**
     * Greedy longest-match segmentation: walks the sentence left to right,
     * at each position trying the longest known word first (down to 1
     * character), matching against the SAME word list already shown
     * elsewhere in the app. Characters that match no known word (mostly
     * punctuation, or vocabulary outside the current word list) are simply
     * skipped — no separate/duplicate word list is introduced.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.iqra.chinese.data.Word> segmentSentence(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.iqra.chinese.data.Achievement> getACHIEVEMENTS() {
        return null;
    }
}