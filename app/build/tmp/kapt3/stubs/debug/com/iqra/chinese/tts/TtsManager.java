package com.iqra.chinese.tts;

/**
 * Offline Chinese TTS with MIUI V816 compatibility.
 *
 * MIUI V816 (BD firmware) blocks standard TTS API binding (code=-1).
 * This manager:
 * 1. Tries standard TextToSpeech API (works on standard Android + HyperOS)
 * 2. On persistent failure, falls back to ACTION_VIEW speech intent
 * 3. Shows install guide for Google TTS if all else fails
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0012\n\u0002\u0010$\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u00015B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u001f\u001a\u00020\u000bJ\u000e\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020\u0006J\u000e\u0010#\u001a\u00020!2\u0006\u0010$\u001a\u00020\u0006J\u0006\u0010%\u001a\u00020\u0017J\u0006\u0010&\u001a\u00020\u0017J\u0010\u0010\'\u001a\u00020!2\u0006\u0010(\u001a\u00020\u0004H\u0016J\u0006\u0010)\u001a\u00020!J\u0018\u0010*\u001a\u00020!2\u0006\u0010+\u001a\u00020\u000b2\b\b\u0002\u0010,\u001a\u00020\u0017J\u000e\u0010-\u001a\u00020!2\u0006\u0010+\u001a\u00020\u000bJ\u0010\u0010.\u001a\u00020!2\u0006\u0010+\u001a\u00020\u000bH\u0002J\u0006\u0010/\u001a\u00020!J\b\u00100\u001a\u00020!H\u0002J,\u00101\u001a\u00020!2\u0006\u0010\"\u001a\u00020\u00062\u0006\u00102\u001a\u00020\u000b2\u0012\u00103\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b04H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\f\u001a\u00020\u000b2\u0006\u0010\n\u001a\u00020\u000b@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u001e\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\n\u001a\u00020\u000b@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000eR\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\t0\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0019\u001a\u00020\u00182\u0006\u0010\n\u001a\u00020\u0018@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0010\u0010\u001c\u001a\u0004\u0018\u00010\u001dX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00066"}, d2 = {"Lcom/iqra/chinese/tts/TtsManager;", "Landroid/speech/tts/TextToSpeech$OnInitListener;", "()V", "MAX_ATTEMPTS", "", "appCtx", "Landroid/content/Context;", "attemptCount", "currentEngine", "Lcom/iqra/chinese/tts/TtsEngineSelector$EngineChoice;", "<set-?>", "", "deviceOs", "getDeviceOs", "()Ljava/lang/String;", "engineLabel", "getEngineLabel", "engineQueue", "Lkotlin/collections/ArrayDeque;", "mainHandler", "Landroid/os/Handler;", "pendingText", "ready", "", "Lcom/iqra/chinese/tts/TtsManager$Status;", "status", "getStatus", "()Lcom/iqra/chinese/tts/TtsManager$Status;", "tts", "Landroid/speech/tts/TextToSpeech;", "usingIntentFallback", "getStatusSummary", "init", "", "ctx", "initWithContext", "context", "isReady", "needsInstallGuide", "onInit", "code", "shutdown", "speak", "text", "slow", "speakSlow", "speakViaIntent", "stop", "tryNextEngine", "trySendBroadcast", "action", "extras", "", "Status", "app_debug"})
public final class TtsManager implements android.speech.tts.TextToSpeech.OnInitListener {
    @org.jetbrains.annotations.NotNull()
    private static final android.os.Handler mainHandler = null;
    @org.jetbrains.annotations.Nullable()
    private static android.speech.tts.TextToSpeech tts;
    @org.jetbrains.annotations.Nullable()
    private static android.content.Context appCtx;
    private static boolean ready = false;
    @org.jetbrains.annotations.Nullable()
    private static java.lang.String pendingText;
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.collections.ArrayDeque<com.iqra.chinese.tts.TtsEngineSelector.EngineChoice> engineQueue = null;
    @org.jetbrains.annotations.Nullable()
    private static com.iqra.chinese.tts.TtsEngineSelector.EngineChoice currentEngine;
    private static int attemptCount = 0;
    private static final int MAX_ATTEMPTS = 10;
    private static boolean usingIntentFallback = false;
    @org.jetbrains.annotations.NotNull()
    private static com.iqra.chinese.tts.TtsManager.Status status = com.iqra.chinese.tts.TtsManager.Status.UNINITIALIZED;
    @org.jetbrains.annotations.NotNull()
    private static java.lang.String engineLabel = "Not started";
    @org.jetbrains.annotations.NotNull()
    private static java.lang.String deviceOs = "Unknown";
    @org.jetbrains.annotations.NotNull()
    public static final com.iqra.chinese.tts.TtsManager INSTANCE = null;
    
    private TtsManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.iqra.chinese.tts.TtsManager.Status getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEngineLabel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDeviceOs() {
        return null;
    }
    
    public final void initWithContext(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    /**
     * Speak text — tries bundled audio first, then TTS API, then intent fallback
     */
    public final void speak(@org.jetbrains.annotations.NotNull()
    java.lang.String text, boolean slow) {
    }
    
    public final void speakSlow(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
    }
    
    /**
     * Intent-based TTS fallback for MIUI V816.
     * Tries multiple broadcast/intent mechanisms to invoke system TTS.
     */
    private final void speakViaIntent(java.lang.String text) {
    }
    
    private final void trySendBroadcast(android.content.Context ctx, java.lang.String action, java.util.Map<java.lang.String, java.lang.String> extras) {
    }
    
    public final void stop() {
    }
    
    public final boolean isReady() {
        return false;
    }
    
    public final void shutdown() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStatusSummary() {
        return null;
    }
    
    public final boolean needsInstallGuide() {
        return false;
    }
    
    private final void tryNextEngine() {
    }
    
    @java.lang.Override()
    public void onInit(int code) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/iqra/chinese/tts/TtsManager$Status;", "", "(Ljava/lang/String;I)V", "UNINITIALIZED", "INITIALIZING", "READY", "LANGUAGE_MISSING", "INTENT_FALLBACK", "ERROR", "app_debug"})
    public static enum Status {
        /*public static final*/ UNINITIALIZED /* = new UNINITIALIZED() */,
        /*public static final*/ INITIALIZING /* = new INITIALIZING() */,
        /*public static final*/ READY /* = new READY() */,
        /*public static final*/ LANGUAGE_MISSING /* = new LANGUAGE_MISSING() */,
        /*public static final*/ INTENT_FALLBACK /* = new INTENT_FALLBACK() */,
        /*public static final*/ ERROR /* = new ERROR() */;
        
        Status() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.iqra.chinese.tts.TtsManager.Status> getEntries() {
            return null;
        }
    }
}