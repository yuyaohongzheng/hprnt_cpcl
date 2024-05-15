package com.unisiot.hanyin_flutter_bluetooth_print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.Tag;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Base64;
import android.os.Handler;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import zpSDK.zpSDK.GZIPFrame;
import zpSDK.zpSDK.zpBluetoothPrinter;
import static cpcl.PrinterHelper.Print;
import static cpcl.PrinterHelper.getElectricity;
import cpcl.IPort;
import cpcl.PrinterHelper;
import cpcl.PublicFunction;
import cpcl.listener.DisConnectBTListener;

/**
 *
 * hanyinFlutterBluetoothPrintPlugin
 *
 * */
public class HanYinFlutterBluetoothPrintPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    private Context applicationContext;
    private Handler handler;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "hanyin_flutter_bluetooth_print");
        channel.setMethodCallHandler(this);
        applicationContext =  flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        if ("printImage".equals(call.method)) {
            String bdAddress = call.argument("BDAddress");
            String url = call.argument("url");
            String width = call.argument("width");
            String height = call.argument("height");
            OkHttpClient client = new OkHttpClient();

            if (TextUtils.isEmpty(url)|| TextUtils.isEmpty(bdAddress)){
                result.success("{\"code\":\"-1\",\"desc\":\"url 或者 设备DBAddress 为空，请确认\"}");
            }
            Log.d("hanyin_flutter_bluetooth_print","bd addres =="+bdAddress+"===url==="+url);
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    result.success("{\"code\":\"-2\",\"desc\":\"下载图片失败，请确认\"}");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (null == response.body()){
                        result.success("{\"code\":\"-2\",\"desc\":\"图片数据流为空，请确认\"}");
                        return;
                    }
                    InputStream bitmapStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(bitmapStream);

                    int newWidth = width == null ? 555 : Integer.parseInt(width);
                    int newHeight = height == null ? 785 : Integer.parseInt(height);
                    Bitmap img =Bitmap.createScaledBitmap(bitmap,newWidth,newHeight,false);

                    zpBluetoothPrinter zpSDK = new zpBluetoothPrinter(applicationContext);
                    boolean connect = zpSDK.connect(bdAddress);
                    if(connect){
                        Log.d("hanyin_flutter_bluetooth_print","打印准备了--width:" + newWidth + "---height:" + newHeight);
//                        zpSDK.pageSetup(574, 0);
//                        byte[] b= GZIPFrame.Draw_Page_Bitmap_(img);
//                        zpSDK.Write(b);
//
//                        zpSDK.print(0, 0);
                        try {
                            Bitmap bitmapPrint = bitmap;
//                            if (isRotate)
//                                bitmapPrint = Utility.Tobitmap90(bitmapPrint);
                            bitmapPrint = Utility.Tobitmap(bitmapPrint, 576, Utility.getHeight(576, bitmapPrint.getWidth(), bitmapPrint.getHeight()));
                            int printImage = PrinterHelper.printBitmap(0, 0, 0, bitmapPrint, 0, false, 0);
                            Log.d("Print", "printImage: " + printImage);
                            if (printImage > 0) {
                                handler.sendEmptyMessage(1);
                            } else {
                                handler.sendEmptyMessage(0);
                            }
                        } catch (Exception e) {
                            handler.sendEmptyMessage(0);
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                zpSDK.disconnect();
                                result.success("{\"code\":\"0\",\"desc\":\"打印成功\"}");
                            }
                        },2000);

                    }else {
                        result.success("{\"code\":\"-3\",\"desc\":\"bdAddress:" + bdAddress + "的蓝牙设备连接失败\"}");
                    }
                }
            });

        } else if ("printBase64Image".equals(call.method)) {
            String bdAddress = call.argument("BDAddress");
            String base64 = call.argument("base64");
            String width = call.argument("width");
            String height = call.argument("height");

            if (TextUtils.isEmpty(base64)|| TextUtils.isEmpty(bdAddress)){
                result.success("{\"code\":\"-1\",\"desc\":\"base64 或者 设备DBAddress 为空，请确认\"}");
            }

            try {
                Bitmap bitmap = null;
                bitmap = base64ToPicture(base64);
                int newWidth = width == null ? 555 : Integer.parseInt(width);
                int newHeight = height == null ? 785 : Integer.parseInt(height);
                Bitmap img = Bitmap.createScaledBitmap(bitmap,newWidth,newHeight,false);

                zpBluetoothPrinter zpSDK = new zpBluetoothPrinter(applicationContext);
                boolean connect = zpSDK.connect(bdAddress);

                if(connect){
                    Log.d("hanyin_flutter_bluetooth_print","打印准备了--width:" + newWidth + "---height:" + newHeight);
//                    zpSDK.pageSetup(574, 0);
//                    byte[] b= GZIPFrame.Draw_Page_Bitmap_(img);
//                    zpSDK.Write(b);
//
//                    zpSDK.print(0, 0);
                    try {
                        Bitmap bitmapPrint = img;
//                            if (isRotate)
//                                bitmapPrint = Utility.Tobitmap90(bitmapPrint);
                        bitmapPrint = Utility.Tobitmap(bitmapPrint, 576, Utility.getHeight(576, bitmapPrint.getWidth(), bitmapPrint.getHeight()));
                        int printImage = PrinterHelper.printBitmap(0, 0, 0, bitmapPrint, 0, false, 0);
                        Log.d("Print", "printImage: " + printImage);
                        if (printImage > 0) {
                            handler.sendEmptyMessage(1);
                        } else {
                            handler.sendEmptyMessage(0);
                        }
                    } catch (Exception e) {
                        handler.sendEmptyMessage(0);
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            zpSDK.disconnect();
                            result.success("{\"code\":\"0\",\"desc\":\"打印成功\"}");
                        }
                    },2000);

                }else {
                    result.success("{\"code\":\"-3\",\"desc\":\"bdAddress:" + bdAddress + "的蓝牙设备连接失败\"}");
                }
            } catch (Exception e) {
                result.success("{\"code\":\"-1\",\"desc\":\"base64 或者 设备DBAddress 为空，请确认\"}");
            }


        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    /**
     * 将图片base64数据转化为bitmap
     *
     * @param imgBase64
     * @return
     * @throws Exception
     */
    public static Bitmap base64ToPicture(String imgBase64) throws Exception {
        //处理头部
        if (imgBase64.contains(",")) {
            imgBase64 = imgBase64.split(",")[1];
        }
        //解码开始
        byte[] decode = Base64.decode(imgBase64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
        return bitmap;
    }

}
