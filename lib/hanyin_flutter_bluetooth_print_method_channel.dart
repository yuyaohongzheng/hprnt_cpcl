import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'hanyin_flutter_bluetooth_print_platform_interface.dart';

/// An implementation of [hanyinFlutterBluetoothPrintPlatform] that uses method channels.
class MethodChannelhanyinFlutterBluetoothPrint extends hanyinFlutterBluetoothPrintPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('hanyin_flutter_bluetooth_print');

  @override
  Future<String?> connect(Map params) async {
    final result = await methodChannel.invokeMethod<String>('connect', params);
    return result;
  }

  @override
  Future<String?> disConnect() async {
    final result = await methodChannel.invokeMethod<String>('disConnect');
    return result;
  }

  @override
  Future<String?> printImage(Map params) async {
    final result = await methodChannel.invokeMethod<String>('printImage', params);
    return result;
  }

  @override
  Future<String?> printBase64Image(Map params) async {
    final result = await methodChannel.invokeMethod<String>('printBase64Image', params);
    return result;
  }
}
