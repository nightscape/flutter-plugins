import 'dart:async';

import 'package:flutter/services.dart';
import 'dart:convert';
import 'dart:io' show Platform;

/// Custom Exception for the plugin,
/// thrown whenever the plugin is used on platforms other than Android
class SensorLibException implements Exception {
  String _cause;

  SensorLibException(this._cause);

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

/// Keys for SensorLib data points
const String TAP_MARKER = 'tap_marker',
    BATTERY_LEVEL = 'battery_level',
    STEP_COUNT = 'step_count',
    MET = 'met',
    MET_LEVEL = 'met_level',
    BODY_POSITION = 'body_position',
    MOVEMENT_ACCELERATION = 'movement_acceleration',
    CONNECTION_STATUS = 'connection_status';

/// Generic SensorLib data-point which all concrete data-points inherit from. Each data-point has a timestamp.
abstract class SensorLibDataPoint {
  DateTime _timeStamp;

  SensorLibDataPoint() {
    /// Log timestamp of data point creation
    _timeStamp = DateTime.now();
  }

  DateTime get timeStamp => _timeStamp;
}

/// Metabolic buffered level, holds met level values for a sedentary, light and moderate state.
class SensorLibMetLevel extends SensorLibDataPoint {
  double _sedentary, _light, _moderate, _vigorous;

  SensorLibMetLevel(String metLevelString) {
    String metLevelJson = metLevelString.replaceAllMapped(
        new RegExp(r'([a-z]+)\=([\d.]+)'), (g) => '"${g[1]}":"${g[2]}"');
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

/// Battery level of the SensorLib device, in percent (%)
class SensorLibBatteryLevel extends SensorLibDataPoint {
  double _batteryLevel;

  SensorLibBatteryLevel(String batteryString) {
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

/// Step count monitored by the SensorLib device
class SensorLibStepCount extends SensorLibDataPoint {
  int _stepCount;

  SensorLibStepCount(String value) {
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

/// A generic class which only contains a timestamp, for when the sensorlib device was tapped.
class SensorLibTapMarker extends SensorLibDataPoint {
  @override
  String toString() {
    return 'TapMarker: {time: ${timeStampHHMMSS(timeStamp)}}';
  }
}

class SensorLibMet extends SensorLibDataPoint {
  double _met;

  SensorLibMet(dynamic value) {
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

/// SensorLib body-position, which depends on the sensor location
class SensorLibBodyPosition extends SensorLibDataPoint {
  String _bodyPosition;

  SensorLibBodyPosition(this._bodyPosition);

  String get bodyPosition => _bodyPosition;

  @override
  String toString() {
    return 'BodyPosition: {'
        'time: ${timeStampHHMMSS(timeStamp)}, '
        'body_position: $bodyPosition'
        '}';
  }
}

/// Accelerometer measure of the SensorLib device
class SensorLibMovementAcceleration extends SensorLibDataPoint {
  double _movementAcceleration;

  SensorLibMovementAcceleration(String value) {
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

/// Accelerometer measure of the SensorLib device
class SensorLibStatus extends SensorLibDataPoint {
  String _connectionStatus;

  SensorLibStatus(String _connectionStatus);

  String get connectionStatus => _connectionStatus;

  @override
  String toString() {
    return 'ConnectionStatus: {'
        'time: ${timeStampHHMMSS(timeStamp)}, '
        'connection_status: $_connectionStatus'
        '}';
  }
}

/// Factory function for converting a generic object sent through the platform channel into a concrete [SensorLibDataPoint] object.
SensorLibDataPoint parseDataPoint(dynamic javaMap) {
  Map<String, dynamic> data = Map<String, dynamic>.from(javaMap);
  String _batteryLevel =
      data.containsKey(BATTERY_LEVEL) ? data[BATTERY_LEVEL] : null;
  String _tapMarker = data.containsKey(TAP_MARKER) ? data[TAP_MARKER] : null;
  String _stepCount = data.containsKey(STEP_COUNT) ? data[STEP_COUNT] : null;
  String _met = data.containsKey(MET) ? data[MET] : null;
  String _metLevel = data.containsKey(MET_LEVEL) ? data[MET_LEVEL] : null;
  String _bodyPosition =
      data.containsKey(BODY_POSITION) ? data[BODY_POSITION] : null;
  String _movementAcceleration = data.containsKey(MOVEMENT_ACCELERATION)
      ? data[MOVEMENT_ACCELERATION]
      : null;
  String _connectionStatus =
      data.containsKey(CONNECTION_STATUS) ? data[CONNECTION_STATUS] : null;

  print(_connectionStatus);

  if (_batteryLevel != null) {
    return new SensorLibBatteryLevel(_batteryLevel);
  }
  if (_tapMarker != null) {
    return new SensorLibTapMarker();
  }
  if (_stepCount != null) {
    return new SensorLibStepCount(_stepCount);
  }
  if (_met != null) {
    return new SensorLibMet(_met);
  }
  if (_metLevel != null) {
    return new SensorLibMetLevel(_metLevel);
  }
  if (_bodyPosition != null) {
    return new SensorLibBodyPosition(_bodyPosition);
  }
  if (_movementAcceleration != null) {
    return new SensorLibMovementAcceleration(_movementAcceleration);
  }
  if (_connectionStatus != null && _connectionStatus != 'null') {
    return new SensorLibStatus(_connectionStatus);
  }
  return null;
}

/// The main plugin class which establishes a [MethodChannel] and an [EventChannel].
class SensorlibFlutter {
  MethodChannel _methodChannel = MethodChannel('sensorlib.method_channel');
  EventChannel _eventChannel = EventChannel('sensorlib.event_channel');

  Stream<Map<String, dynamic>> connectDevice(String deviceName, Map<String, dynamic> args) {
    if (Platform.isAndroid) {
      Stream<Map<String, dynamic>> _sensorlibStream = _eventChannel
            .receiveBroadcastStream()
            .map((d) => Map<String, dynamic>.from(d));
      final Map<String, dynamic> allArgs = {"deviceName": deviceName };
      allArgs.addAll(args);
      _sensorlibStream.first.then((map) => 0);
      Future.delayed(Duration(seconds: 1)).then((_) => _methodChannel.invokeMethod("connectDevice", allArgs));
      return _sensorlibStream;
    } else {
      throw SensorLibException('SensorLib API exclusively available on Android!');
    }
  }

  Future<String> get test => Future.sync(() => "Yes");
}
