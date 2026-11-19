# Keep useful source and line information for retraceable release crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve runtime-visible annotations used by AndroidX and Material Components.
-keepattributes *Annotation*

# GeckoView uses reflection and generated JNI bindings; preserve its public bridge types.
-keep class org.mozilla.geckoview.** { *; }
