import Flutter
import UIKit

public class hanyinFlutterBluetoothPrintPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "hanyin_flutter_bluetooth_print", binaryMessenger: registrar.messenger())
    let instance = hanyinFlutterBluetoothPrintPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
