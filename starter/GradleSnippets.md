# Gradle snippets

Add Google Play Services Location for geofencing (free):

In `app/build.gradle`:

```gradle
dependencies {
    implementation "com.google.android.gms:play-services-location:21.3.0"
}
```

If you use modern AndroidX:

```gradle
dependencies {
    implementation "androidx.core:core-ktx:1.13.1"
    implementation "androidx.appcompat:appcompat:1.7.0"
    implementation "com.google.android.material:material:1.12.0"
}
```


