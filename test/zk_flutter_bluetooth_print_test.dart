import 'package:flutter_test/flutter_test.dart';
import 'package:hanyin_flutter_bluetooth_print/hanyin_flutter_bluetooth_print.dart';
import 'package:hanyin_flutter_bluetooth_print/hanyin_flutter_bluetooth_print_platform_interface.dart';
import 'package:hanyin_flutter_bluetooth_print/hanyin_flutter_bluetooth_print_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockhanyinFlutterBluetoothPrintPlatform
    with MockPlatformInterfaceMixin
    implements hanyinFlutterBluetoothPrintPlatform {

  @override
  Future<String?> printImage(Map params) => Future.value('42');

  @override
  Future<String?> printBase64Image(Map params) {
    // TODO: implement printBase64Image
    throw UnimplementedError();
  }

  @override
  Future<String?> connect(Map params) {
    // TODO: implement connect
    throw UnimplementedError();
  }

  @override
  Future<String?> disConnect() {
    // TODO: implement disConnect
    throw UnimplementedError();
  }
}

void main() {
  final hanyinFlutterBluetoothPrintPlatform initialPlatform = hanyinFlutterBluetoothPrintPlatform.instance;

  test('$MethodChannelhanyinFlutterBluetoothPrint is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelhanyinFlutterBluetoothPrint>());
  });

  // test('getPlatformVersion', () async {
  //   hanyinFlutterBluetoothPrint hanyinFlutterBluetoothPrintPlugin = hanyinFlutterBluetoothPrint();
  //   MockhanyinFlutterBluetoothPrintPlatform fakePlatform = MockhanyinFlutterBluetoothPrintPlatform();
  //   hanyinFlutterBluetoothPrintPlatform.instance = fakePlatform;
  //
  //   expect(await hanyinFlutterBluetoothPrintPlugin.getPlatformVersion(), '42');
  // });
}
