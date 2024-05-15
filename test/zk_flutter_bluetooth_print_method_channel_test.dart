import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hanyin_flutter_bluetooth_print/hanyin_flutter_bluetooth_print_method_channel.dart';

void main() {
  MethodChannelhanyinFlutterBluetoothPrint platform = MethodChannelhanyinFlutterBluetoothPrint();
  const MethodChannel channel = MethodChannel('hanyin_flutter_bluetooth_print');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  // test('getPlatformVersion', () async {
  //   expect(await platform.getPlatformVersion(), '42');
  // });
}
