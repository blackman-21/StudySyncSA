# StudySync SA ProGuard / R8 Rules for Play Store Release

# Preserve line numbers and source file names for Crashlytics / Debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database Keep Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Kotlinx Serialization
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Retrofit & OkHttp
-keepclassmembers class * {
    @retrofit2.http.** <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# Firebase Services (Auth, Messaging, Firestore, Storage)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# WorkManager
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Android Security Crypto
-keep class androidx.security.crypto.** { *; }
