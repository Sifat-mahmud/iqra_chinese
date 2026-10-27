package com.iqra.chinese.tts;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\rB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004J\u0006\u0010\u0005\u001a\u00020\u0004J\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\b\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\u0004J\u0006\u0010\n\u001a\u00020\u000bJ\u0006\u0010\f\u001a\u00020\u000b\u00a8\u0006\u000e"}, d2 = {"Lcom/iqra/chinese/tts/DeviceInfo;", "", "()V", "getMiuiVersion", "", "getOsDescription", "getOsType", "Lcom/iqra/chinese/tts/DeviceInfo$OsType;", "getProp", "key", "isHyperOs", "", "isXiaomi", "OsType", "app_debug"})
public final class DeviceInfo {
    @org.jetbrains.annotations.NotNull()
    public static final com.iqra.chinese.tts.DeviceInfo INSTANCE = null;
    
    private DeviceInfo() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.iqra.chinese.tts.DeviceInfo.OsType getOsType() {
        return null;
    }
    
    public final boolean isXiaomi() {
        return false;
    }
    
    public final boolean isHyperOs() {
        return false;
    }
    
    /**
     * Read system property via reflection — no root needed for public props
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getProp(@org.jetbrains.annotations.NotNull()
    java.lang.String key) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMiuiVersion() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getOsDescription() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/iqra/chinese/tts/DeviceInfo$OsType;", "", "(Ljava/lang/String;I)V", "HYPER_OS", "MIUI", "SAMSUNG", "STANDARD", "app_debug"})
    public static enum OsType {
        /*public static final*/ HYPER_OS /* = new HYPER_OS() */,
        /*public static final*/ MIUI /* = new MIUI() */,
        /*public static final*/ SAMSUNG /* = new SAMSUNG() */,
        /*public static final*/ STANDARD /* = new STANDARD() */;
        
        OsType() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.iqra.chinese.tts.DeviceInfo.OsType> getEntries() {
            return null;
        }
    }
}