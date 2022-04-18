-optimizationpasses 10
-dontusemixedcaseclassnames
-verbose

-keep, allowobfuscation class com.company.*
-keepclassmembers, allowobfuscation class * {
    *;
}

-keepnames class com.authenticalls.flashcall_sdk.*.**
-keepclassmembernames class com.authenticalls.flashcall_sdk.*.** {
    public <methods>;
    public <fields>;
}