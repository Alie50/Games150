# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontusemixedcaseclassnames
# For using ACRA, we have to keep the old ACRA API.
-keep class org.acra.ACRA {
	void attachBaseContext(android.content.Context);
}

# Preserve line numbers for debugging stack traces.
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Remove logging in release mode.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
