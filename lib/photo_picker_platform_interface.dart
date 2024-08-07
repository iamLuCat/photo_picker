import 'package:photo_picker/entity/media.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'photo_picker_method_channel.dart';

abstract class PhotoPickerPlatform extends PlatformInterface {
  /// Constructs a PhotoPickerPlatform.
  PhotoPickerPlatform() : super(token: _token);

  static final Object _token = Object();

  static PhotoPickerPlatform _instance = MethodChannelPhotoPicker();

  /// The default instance of [PhotoPickerPlatform] to use.
  ///
  /// Defaults to [MethodChannelPhotoPicker].
  static PhotoPickerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [PhotoPickerPlatform] when
  /// they register themselves.
  static set instance(PhotoPickerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<Media?> pickMedia() {
    throw UnimplementedError('pickPhoto() has not been implemented.');
  }

  Future<bool?> requestPermission() {
    throw UnimplementedError('requestPermission() has not been implemented.');
  }
}
