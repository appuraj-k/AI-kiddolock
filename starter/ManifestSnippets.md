# Manifest snippets (Android Studio project)

## Permissions (typical)

Add to your `AndroidManifest.xml`:

- Usage access (special access screen, not a normal runtime permission):
  - `android.permission.PACKAGE_USAGE_STATS`
- Location:
  - `android.permission.ACCESS_FINE_LOCATION`
  - `android.permission.ACCESS_COARSE_LOCATION`
  - `android.permission.ACCESS_BACKGROUND_LOCATION` (Android 10+ if you want geofences reliably in background)
- (Optional) for better behavior with long-running monitor:
  - `android.permission.FOREGROUND_SERVICE`

## Accessibility service

Add a service with permission `android.permission.BIND_ACCESSIBILITY_SERVICE` and an XML config in `res/xml/`.

Example structure (you must adjust package/class names):

```xml
<service
    android:name=".lock.AppLockAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
    android:exported="false">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/app_lock_accessibility_config" />
</service>
```

And create `res/xml/app_lock_accessibility_config.xml` similar to:

```xml
<accessibility-service
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged|typeWindowContentChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100"
    android:canRetrieveWindowContent="false"
    android:accessibilityFlags="flagDefault" />
```

> Keep `canRetrieveWindowContent=false` for privacy + performance unless you truly need it.


