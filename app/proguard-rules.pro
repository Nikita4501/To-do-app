# Правила R8/ProGuard для release-сборки (minify отключён в build.gradle.kts).
-keepattributes *Annotation*
-keep class com.example.todoapp.data.** { *; }
