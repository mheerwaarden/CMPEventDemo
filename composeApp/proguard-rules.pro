# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Don't print notes about potential mistakes or omissions in the configuration for kotlinx-serialization classes
# See also https://github.com/Kotlin/kotlinx.serialization/issues/1900
-dontnote kotlinx.serialization.**

# Serialization core uses `java.lang.ClassValue` for caching inside these specified classes.
# If there is no `java.lang.ClassValue` (for example, in Android), then R8/ProGuard will print a warning.
# However, since in this case they will not be used, we can disable these warnings
-dontwarn kotlinx.serialization.internal.ClassValueReferences

# disable optimisation for descriptor field because in some versions of ProGuard, optimization generates incorrect bytecode that causes a verification error
# see https://github.com/Kotlin/kotlinx.serialization/issues/2719
-keepclassmembers public class **$$serializer {
    private ** descriptor;
}


-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }
-keepclassmembers class * {
    public <init>(...);
}
-keep class kotlin.jvm.internal.** {
  *;
}

-keepnames class androidx.lifecycle.** { *; }
-keepnames class androidx.compose.ui.text.platform.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.collection.** { *; }
-keep class androidx.lifecycle.** { *; }

-keep class **KoinModule { *; }
-keep class org.koin.** { *; }
-keepnames class * {
  @org.koin.core.annotation.Module *;
  @org.koin.core.annotation.Single *;
  @org.koin.core.annotation.Factory *;
  @org.koin.core.annotation.Named *;
  @org.koin.core.annotation.Scope *;
}

-keep class org.jetbrains.skiko.** { *; }

-dontwarn **$Preview*
-dontwarn **$DefaultImpls