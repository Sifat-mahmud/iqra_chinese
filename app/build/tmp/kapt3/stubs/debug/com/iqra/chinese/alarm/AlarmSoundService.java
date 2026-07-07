package com.iqra.chinese.alarm;

/**
 * Foreground service that plays the looping alarm sound.
 * Started by AlarmActivity, stopped by Snooze/Stop actions.
 * Using a foreground service means the sound keeps playing even if
 * the user navigates away from AlarmActivity.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u0001\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0006\u0018\u0000 \u00182\u00020\u0001:\u0001\u0018B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0007\u001a\u00020\bH\u0002J\b\u0010\t\u001a\u00020\nH\u0002J\b\u0010\u000b\u001a\u00020\nH\u0002J\u0014\u0010\f\u001a\u0004\u0018\u00010\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u000fH\u0016J\b\u0010\u0010\u001a\u00020\nH\u0016J\b\u0010\u0011\u001a\u00020\nH\u0016J\"\u0010\u0012\u001a\u00020\u00132\b\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\u0014\u001a\u00020\u00132\u0006\u0010\u0015\u001a\u00020\u0013H\u0016J\b\u0010\u0016\u001a\u00020\nH\u0002J\b\u0010\u0017\u001a\u00020\nH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/iqra/chinese/alarm/AlarmSoundService;", "Landroid/app/Service;", "()V", "mediaPlayer", "Landroid/media/MediaPlayer;", "vibrator", "Landroid/os/Vibrator;", "buildNotification", "Landroid/app/Notification;", "createChannel", "", "launchAlarmGui", "onBind", "", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onStartCommand", "", "flags", "startId", "playAlarm", "startVibration", "Companion", "app_debug"})
public final class AlarmSoundService extends android.app.Service {
    @org.jetbrains.annotations.Nullable()
    private android.media.MediaPlayer mediaPlayer;
    @org.jetbrains.annotations.Nullable()
    private android.os.Vibrator vibrator;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_STOP = "com.iqra.chinese.ALARM_STOP";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CH_SVC = "iqra_alarm_svc";
    @org.jetbrains.annotations.NotNull()
    public static final com.iqra.chinese.alarm.AlarmSoundService.Companion Companion = null;
    
    public AlarmSoundService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    /**
     * Launches the full-screen AlarmActivity GUI so the user sees a clear
     * "study now" screen instead of just hearing sound in the background.
     *
     * Called from a running foreground service context (this), which Android
     * trusts to start activities — unlike a plain BroadcastReceiver, which
     * Android 10+ (and MIUI especially) silently blocks from doing so.
     */
    private final void launchAlarmGui() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Void onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    private final void playAlarm() {
    }
    
    private final void startVibration() {
    }
    
    private final android.app.Notification buildNotification() {
        return null;
    }
    
    private final void createChannel() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/iqra/chinese/alarm/AlarmSoundService$Companion;", "", "()V", "ACTION_STOP", "", "CH_SVC", "start", "", "ctx", "Landroid/content/Context;", "stop", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final void start(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
        }
        
        public final void stop(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
        }
    }
}