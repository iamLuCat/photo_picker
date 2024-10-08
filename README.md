# photo_picker

Selected video from gallery for Android

## Request permissions

First, request the correct storage permissions in the Android manifest, depending on the OS version:

```
<!-- Devices running Android 12L (API level 32) or lower  -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />

<!-- Devices running Android 13 (API level 33) or higher -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />

<!-- To handle the reselection within the app on devices running Android 14
     or higher if your app targets Android 14 (API level 34) or higher.  -->
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED" />
```

## Usage
Request permission to access the gallery:

```dart
import 'package:photo_picker_manager/photo_picker.dart';

Future<void> pickVideo() async {
  final isGranted = await PhotoPicker.requestPermission();
  if (!isGranted) {
    return;
  }
  final media = await PhotoPicker.pickMedia();
  print(media);
}
```

