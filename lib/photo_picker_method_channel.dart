import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:photo_picker/entity/media.dart';

import 'photo_picker_platform_interface.dart';

/// An implementation of [PhotoPickerPlatform] that uses method channels.
class MethodChannelPhotoPicker extends PhotoPickerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('photo_picker');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<Media?> pickMedia() async {
    final result = await methodChannel.invokeMethod('pickMedia');
    if (result != null) {
      return Media.fromJson(result as Map);
    }
    return result;
  }

  @override
  Future<bool?> requestPermission() async {
    final result = await methodChannel.invokeMethod('requestPermission');
    return result;
  }
}
