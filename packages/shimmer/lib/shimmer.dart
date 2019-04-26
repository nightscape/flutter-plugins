import 'dart:async';

import 'package:flutter/services.dart';
import 'dart:convert';
import 'dart:io' show Platform;

/// Custom Exception for the plugin,
/// thrown whenever the plugin is used on platforms other than Android
class ShimmerException implements Exception {
  String _cause;

  ShimmerException(this._cause);

  @override
  String toString() {
    return _cause;
  }
}

enum Gender { male, female }

enum SensorLocation {
  left_ankle,
  left_hip,
  left_thigh,
  left_upper_arm,
  left_wrist,
  right_ankle,
  right_hip,
  right_thigh,
  right_upper_arm,
  right_wrist,
  chest
}

class UserData {
  int weight, height, age;

  /// Weight in kg, height in cm, age in years
  Gender gender;

  /// Gender: male or female
  SensorLocation sensorLocation;

  /// Sensor placement on body
  String sensorAddress, sensorName;

  /// Sensor device addresss and name

  UserData(this.weight, this.height, this.gender, this.age, this.sensorLocation,
      this.sensorAddress, this.sensorName);

  Map<String, String> get asMap {
    return {
      'weight': '$weight',
      'height': '$height',
      'age': '$age',
      'gender': '$gender',
      'sensor_location': '$sensorLocation',
      'sensor_address': '$sensorAddress',
      'sensor_name': '$sensorName'
    };
  }
}

String timeStampHHMMSS(DateTime timeStamp) {
  return timeStamp.toIso8601String();
}

/// Keys for Shimmer data points
const String TAP_MARKER = 'tap_marker',
    BATTERY_LEVEL = 'battery_level',
    STEP_COUNT = 'step_count',
    MET = 'met',
    MET_LEVEL = 'met_level',
    MOVEMENT_ACCELERATION = 'movement_acceleration',
    CONNECTION_STATUS = 'connection_status';

/// Generic Shimmer data-point which all concrete data-points inherit from. Each data-point has a timestamp.
abstract class ShimmerDataPoint {
  DateTime _timeStamp;

  ShimmerDataPoint() {
    /// Log timestamp of data point creation
    _timeStamp = DateTime.now();
  }

  DateTime get timeStamp => _timeStamp;
}

/// Metabolic buffered level, holds met level values for a sedentary, light and moderate state.
class ShimmerMetLevel extends ShimmerDataPoint {
  double _sedentary, _light, _moderate, _vigorous;

  ShimmerMetLevel(String metLevelString) {
    String metLevelJson = metLevelString.replaceAllMapped(
        new RegExp(r'([a-z]+)=([\d.]+)'), (g) => '"${g[1]}":"${g[2]}"');
    Map<String, dynamic> metLevel = jsonDecode(metLevelJson);

    _sedentary = double.parse(metLevel['sedentary']);
    _light = double.parse(metLevel['light']);
    _moderate = double.parse(metLevel['moderate']);
    _vigorous = double.parse(metLevel['vigorous']);
  }

  double get sedentary => _sedentary;

  double get light => _light;

  double get moderate => _moderate;

  double get vigorous => _vigorous;

  @override
  String toString() {
    return 'MetLevel: {'
        'time: ${timeStampHHMMSS(timeStamp)}, '
        'sedentary: $sedentary, '
        'light: $light, '
        'moderate: $moderate, '
        'vigorous: $vigorous'
        '}';
  }
}

/// Battery level of the Shimmer device, in percent (%)
class ShimmerBatteryLevel extends ShimmerDataPoint {
  double _batteryLevel;

  ShimmerBatteryLevel(String batteryString) {
    _batteryLevel = double.parse(batteryString);
  }

  double get batteryLevel => _batteryLevel;

  @override
  String toString() {
    return 'BatteryLevel: {'
        'time: ${timeStampHHMMSS(timeStamp)}, '
        'battery_level: $batteryLevel'
        '}';
  }
}

/// Step count monitored by the Shimmer device
class ShimmerStepCount extends ShimmerDataPoint {
  int _stepCount;

  ShimmerStepCount(String value) {
    _stepCount = int.parse(value);
  }

  int get stepCount => _stepCount;

  @override
  String toString() {
    return 'StepCount: {'
        'time: ${timeStampHHMMSS(timeStamp)}, '
        'step_count: $stepCount'
        '}';
  }
}

/// A generic class which only contains a timestamp, for when the shimmer device was tapped.
class ShimmerTapMarker extends ShimmerDataPoint {
  @override
  String toString() {
    return 'TapMarker: {time: ${timeStampHHMMSS(timeStamp)}}';
  }
}

class ShimmerMet extends ShimmerDataPoint {
  double _met;

  ShimmerMet(dynamic value) {
    String met = value;
    _met = double.parse(met.split(',').removeLast());
  }

  double get met => _met;

  @override
  String toString() {
    return 'MET: {'
        'time: ${timeStampHHMMSS(timeStamp)}, '
        'met: $met'
        '}';
  }
}

/// Accelerometer measure of the Shimmer device
class ShimmerAcceleration extends ShimmerDataPoint {
  double _movementAcceleration;

  ShimmerAcceleration(String value) {
    _movementAcceleration = double.parse(value);
  }

  double get movementAcceleration => _movementAcceleration;

  @override
  String toString() {
    return 'MovementAcceleration: {'
        'time: ${timeStampHHMMSS(timeStamp)}, '
        'movement_acceleration: $_movementAcceleration'
        '}';
  }
}

/// Accelerometer measure of the Shimmer device
class ShimmerStatus extends ShimmerDataPoint {
  String _connectionStatus;

  ShimmerStatus(String _connectionStatus);

  String get connectionStatus => _connectionStatus;

  @override
  String toString() {
    return 'ConnectionStatus: {'
        'time: ${timeStampHHMMSS(timeStamp)}, '
        'connection_status: $_connectionStatus'
        '}';
  }
}

/// Factory function for converting a generic object sent through the platform channel into a concrete [ShimmerDataPoint] object.
ShimmerDataPoint parseDataPoint(dynamic javaMap) {
  Map<String, dynamic> data = Map<String, dynamic>.from(javaMap);
  String _batteryLevel =
      data.containsKey(BATTERY_LEVEL) ? data[BATTERY_LEVEL] : null;
  String _tapMarker = data.containsKey(TAP_MARKER) ? data[TAP_MARKER] : null;
  String _stepCount = data.containsKey(STEP_COUNT) ? data[STEP_COUNT] : null;
  String _met = data.containsKey(MET) ? data[MET] : null;
  String _metLevel = data.containsKey(MET_LEVEL) ? data[MET_LEVEL] : null;
  String _movementAcceleration = data.containsKey(MOVEMENT_ACCELERATION)
      ? data[MOVEMENT_ACCELERATION]
      : null;
  String _connectionStatus =
      data.containsKey(CONNECTION_STATUS) ? data[CONNECTION_STATUS] : null;

  print(_connectionStatus);

  if (_batteryLevel != null) {
    return new ShimmerBatteryLevel(_batteryLevel);
  }
  if (_tapMarker != null) {
    return new ShimmerTapMarker();
  }
  if (_stepCount != null) {
    return new ShimmerStepCount(_stepCount);
  }
  if (_met != null) {
    return new ShimmerMet(_met);
  }
  if (_metLevel != null) {
    return new ShimmerMetLevel(_metLevel);
  }
  if (_movementAcceleration != null) {
    return new ShimmerAcceleration(_movementAcceleration);
  }
  if (_connectionStatus != null && _connectionStatus != 'null') {
    return new ShimmerStatus(_connectionStatus);
  }
  return null;
}

/// The main plugin class which establishes a [MethodChannel] and an [EventChannel].
class Shimmer {
  MethodChannel _methodChannel = MethodChannel('shimmer.method_channel');
  EventChannel _eventChannel = EventChannel('shimmer.event_channel');

  Stream<Map<String, dynamic>> connectDevice(String deviceName, Map<String, dynamic> args) {
    if (Platform.isAndroid) {
      Stream<Map<String, dynamic>> _shimmerStream = _eventChannel
            .receiveBroadcastStream()
            .map((d) => Map<String, dynamic>.from(d));
      final Map<String, dynamic> allArgs = {"deviceName": deviceName };
      allArgs.addAll(args);
      _shimmerStream.first.then((map) => 0);
      Future.delayed(Duration(seconds: 1)).then((_) => _methodChannel.invokeMethod("connectDevice", allArgs));
      return _shimmerStream;
    } else {
      throw ShimmerException('Shimmer API exclusively available on Android!');
    }
  }

  Future<String> get test => Future.sync(() => "Yes");
}
