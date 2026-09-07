# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers and source file attributes for Play Console deobfuscation and crash analysis
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Data models and entities
-keep class com.example.data.** { *; }
-keep class com.example.util.** { *; }

# Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Retrofit, OkHttp & Moshi
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Coil image loader
-keep class coil.** { *; }

