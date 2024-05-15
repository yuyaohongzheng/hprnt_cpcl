
import 'hanyin_flutter_bluetooth_print_platform_interface.dart';

class hanyinFlutterBluetoothPrint {
  /// params 参数
  ///
  Future<String?> printImage(Map params) {
    return hanyinFlutterBluetoothPrintPlatform.instance.printImage(params);
  }

  /// 打印base64图片
  ///
  Future<String?> printBase64Image(Map params) {
    return hanyinFlutterBluetoothPrintPlatform.instance.printBase64Image(params);
  }
}
