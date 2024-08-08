import 'package:flutter/material.dart';
import 'package:photo_picker_manager/entity/media.dart';
import 'package:photo_picker_manager/photo_picker.dart';
import 'package:photo_manager/photo_manager.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _photoPickerPlugin = PhotoPicker();

  @override
  void initState() {
    super.initState();
  }

  Media? media;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              TextButton(
                  onPressed: () async {
                    PhotoManager.requestPermissionExtend();
                  },
                  child: const Text("Request permission")),
              Text("FILE: ${media.toString()}"),
              TextButton(
                  onPressed: () async {
                    final isGranted = await _photoPickerPlugin.requestPermission();
                    if (isGranted == false) {
                      return;
                    }
                    final result = await _photoPickerPlugin.pickMedia();
                    setState(() {
                      media = result;
                    });
                  },
                  child: const Text("Picker")),
            ],
          ),
        ),
      ),
    );
  }
}
