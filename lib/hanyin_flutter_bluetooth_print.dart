
import 'hanyin_flutter_bluetooth_print_platform_interface.dart';

class hanyinFlutterBluetoothPrint {
  /// 连接打印机
  /// params 参数
  ///
  Future<String?> connect(Map params) {
    return hanyinFlutterBluetoothPrintPlatform.instance.connect(params);
  }
  /// 断开打印机连接
  /// params 参数
  ///
  Future<String?> disConnect() {
    return hanyinFlutterBluetoothPrintPlatform.instance.disConnect();
  }
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
