import 'package:flutter_test/flutter_test.dart';
import 'package:photo_picker/entity/media.dart';
import 'package:photo_picker/photo_picker.dart';
import 'package:photo_picker/photo_picker_platform_interface.dart';
import 'package:photo_picker/photo_picker_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockPhotoPickerPlatform with MockPlatformInterfaceMixin implements PhotoPickerPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<Media?> pickMedia() {
    return Future.value(Media(
      path: 'path',
      name: 'name',
      size: 42,
      type: 'type',
      duration: 42,
    ));
  }

  @override
  Future<bool?> requestPermission() {
    return Future.value(true);
  }
}

void main() {
  final PhotoPickerPlatform initialPlatform = PhotoPickerPlatform.instance;

  test('$MethodChannelPhotoPicker is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelPhotoPicker>());
  });

  test('getPlatformVersion', () async {
    PhotoPicker photoPickerPlugin = PhotoPicker();
    MockPhotoPickerPlatform fakePlatform = MockPhotoPickerPlatform();
    PhotoPickerPlatform.instance = fakePlatform;

    expect(await photoPickerPlugin.getPlatformVersion(), '42');
  });
}
