# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Ktor client ProGuard rules (required for Google Generative AI SDK)
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# Google Generative AI SDK ProGuard rules
-dontwarn com.google.ai.client.generativeai.**
-keep class com.google.ai.client.generativeai.** { *; }

# kotlinx-serialization ProGuard rules
-dontwarn kotlinx.serialization.**
-keep class kotlinx.serialization.** { *; }

# Keep application and android component classes to prevent ClassNotFoundException at runtime
-keep class com.asistenpetani.AsistenPetaniApp { *; }
-keep class com.asistenpetani.MainActivity { *; }
-keep class com.asistenpetani.service.notification.MyFirebaseMessagingService { *; }
-keep class com.asistenpetani.service.BootReceiver { *; }
-keep class com.asistenpetani.di.** { *; }
-keep class com.asistenpetani.data.** { *; }
-keep class com.asistenpetani.domain.** { *; }
-keep class com.asistenpetani.ui.** { *; }


