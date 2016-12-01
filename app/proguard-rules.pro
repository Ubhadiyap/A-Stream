-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-keepattributes Signature, *Annotation*
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}


-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

#########################


-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }

-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-dontwarn com.google.api.services.youtube.**
-keep class com.google.api.services.youtube.** { *; }
-keep interface com.google.api.services.youtube.** { *; }

-dontwarn com.google.**
-keep class com.google.** { *; }
-keep interface com.google.** { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

###########

-keep class com.google.api.client.util.ArrayMap.** { *; }
-keep class com.google.api.services.youtube.model.** { *; }

-dontwarn org.apache.**
-keep class org.apache.net.** { *; }
-keep interface org.apache.net.**