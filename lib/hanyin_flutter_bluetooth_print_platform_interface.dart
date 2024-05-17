import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'hanyin_flutter_bluetooth_print_method_channel.dart';

abstract class hanyinFlutterBluetoothPrintPlatform extends PlatformInterface {
  /// Constructs a hanyinFlutterBluetoothPrintPlatform.
  hanyinFlutterBluetoothPrintPlatform() : super(token: _token);

  static final Object _token = Object();

  static hanyinFlutterBluetoothPrintPlatform _instance = MethodChannelhanyinFlutterBluetoothPrint();

  /// The default instance of [hanyinFlutterBluetoothPrintPlatform] to use.
  ///
  /// Defaults to [MethodChannelhanyinFlutterBluetoothPrint].
  static hanyinFlutterBluetoothPrintPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [hanyinFlutterBluetoothPrintPlatform] when
  /// they register themselves.
  static set instance(hanyinFlutterBluetoothPrintPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> connect(Map params) {
    throw UnimplementedError('connect() has not been implemented.');
  }

  Future<String?> disConnect() {
    throw UnimplementedError('disConnect() has not been implemented.');
  }

  Future<String?> printImage(Map params) {
    throw UnimplementedError('printImage() has not been implemented.');
  }

  Future<String?> printBase64Image(Map params) {
    throw UnimplementedError('printBase64Image() has not been implemented.');
  }
}
