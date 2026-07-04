package com.iqra.chinese.tts;

/**
 * Bundled offline TTS using pre-recorded eSpeak-NG audio files.
 * Works on ALL devices with no internet, no TTS engine required.
 * Audio files are stored in res/raw/ as MP3s (~3MB total).
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u0005J\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\bJ\u0018\u0010\u0011\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u00052\b\b\u0002\u0010\u0012\u001a\u00020\fJ\u0006\u0010\u0013\u001a\u00020\u000fR\u001a\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/iqra/chinese/tts/BundledTts;", "", "()V", "AUDIO_MAP", "", "", "", "appCtx", "Landroid/content/Context;", "player", "Landroid/media/MediaPlayer;", "canSpeak", "", "text", "init", "", "context", "speak", "slowSpeed", "stop", "app_debug"})
public final class BundledTts {
    @org.jetbrains.annotations.Nullable()
    private static android.media.MediaPlayer player;
    @org.jetbrains.annotations.Nullable()
    private static android.content.Context appCtx;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.String, java.lang.Integer> AUDIO_MAP = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.iqra.chinese.tts.BundledTts INSTANCE = null;
    
    private BundledTts() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * Returns true if we have a bundled audio file for this text
     */
    public final boolean canSpeak(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
        return false;
    }
    
    /**
     * Speak using bundled audio. Returns true if audio found and played.
     */
    public final boolean speak(@org.jetbrains.annotations.NotNull()
    java.lang.String text, boolean slowSpeed) {
        return false;
    }
    
    public final void stop() {
    }
}