-dontobfuscate
-optimizations !code/allocation/variable
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep the FlexboxLayout class since it's used in XML.
# While -dontobfuscate prevents renaming, this ensures the class isn't
# accidentally removed (shrunk) by the optimizer.
-keep class com.google.android.flexbox.FlexboxLayout { *; }
