import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sensorlib_flutter/sensorlib_flutter.dart';

void main() {
  const MethodChannel channel = MethodChannel('sensorlib_flutter');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
  });
}
