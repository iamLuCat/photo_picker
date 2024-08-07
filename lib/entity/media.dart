class Media {
  String path;
  String name;
  int size;
  String type;
  String? thumbnail;
  String mimeType;
  int duration;

  Media({
    this.path = '',
    this.name = '',
    this.size = 0,
    this.type = '',
    this.thumbnail,
    this.mimeType = '',
    this.duration = 0,
  });

  factory Media.fromJson(Map json) {
    return Media(
      path: json['path'] ?? '',
      name: json['name'] ?? '',
      size: json['size'] ?? 0,
      type: json['type'] ?? '',
      thumbnail: json['thumbnail'],
      mimeType: json['mimeType'] ?? '',
      duration: json['duration'] ?? 0,
    );
  }

  Duration get durationDuration => Duration(milliseconds: duration.toInt());

  @override
  String toString() {
    return "Media(\npath: $path, \nname: $name, \nsize: $size, \ntype: $type, \nthumbnail: $thumbnail, \nmimeType: $mimeType, \nduration: $duration)";
  }
}
