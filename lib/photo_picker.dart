
import 'package:image_picker_pro_max/entity/media.dart';

import 'photo_picker_platform_interface.dart';

class PhotoPicker {
  Future<String?> getPlatformVersion() {
    return PhotoPickerPlatform.instance.getPlatformVersion();
  }

  Future<Media?> pickMedia() {
    return PhotoPickerPlatform.instance.pickMedia();
  }

  Future<bool?> requestPermission() {
    return PhotoPickerPlatform.instance.requestPermission();
  }
}
