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

import java.io.ByteArrayInputStream;
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
    Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(int msg) {
            // 处理消息
        }
    };

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "hanyin_flutter_bluetooth_print");
        channel.setMethodCallHandler(this);
        applicationContext =  flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        if ("connect".equals(call.method)) {
            String bdAddress = call.argument("BDAddress");

            Log.d("hanyin_flutter_bluetooth_print","bd addres =="+bdAddress);

            int connect = -1;
            try {
                connect = PrinterHelper.portOpenBT(applicationContext, bdAddress);
            } catch (Exception e) {
                result.success("{\"code\":\""+ connect + "\",\"desc\":\"bdAddress:" + bdAddress + "的蓝牙设备连接失败\"}");
                Log.d("HPRTSDKSample", e.getMessage().toString());
            }
            int connectStatus = connect;
            if(connectStatus == 0){
                Log.d("hanyin_flutter_bluetooth_print","打印机连接上了");
                result.success("{\"code\":\"0\",\"desc\":\"bdAddress:" + bdAddress + "的蓝牙设备连接成功\"}");
            } else {
                Log.d("hanyin_flutter_bluetooth_print","打印机连接失败--" + connectStatus);
                result.success("{\"code\":\""+ connectStatus + "\",\"desc\":\"bdAddress:" + bdAddress + "的蓝牙设备连接失败\"}");
            }
        } else if ("printImage".equals(call.method)) {
            // 该接口不是实时指令，打印机正在打印时，查询无效
            int status = 0;
            try {
                status = PrinterHelper.getPrinterStatus();
            } catch (Exception e) {
                result.success("{\"code\":\"-1\",\"desc\":\"打印机状态获取失败\"}");
                Log.d("HPRTSDKSample", e.getMessage().toString());
                return;
            }
            if((status & 2) == 2){
                //缺纸
                result.success("{\"code\":\"-1\",\"desc\":\"打印机缺纸或连接失败\"}");
                Log.d("hanyin_flutter_bluetooth_print","打印机缺纸或连接失败");
                return;
            } else if ((status & 4) == 4){
                //开盖
                result.success("{\"code\":\"-1\",\"desc\":\"打印机开盖\"}");
                Log.d("hanyin_flutter_bluetooth_print","打印机开盖");
                return;
            }
            String url = call.argument("url");
            String width = call.argument("width");
            String times = call.argument("times") == null ? "1" : call.argument("times");
            String height = call.argument("height");
            OkHttpClient client = new OkHttpClient();

            if (TextUtils.isEmpty(url)){
                result.success("{\"code\":\"-1\",\"desc\":\"url为空，请确认\"}");
                return;
            }
            Log.d("hanyin_flutter_bluetooth_print", "===url==="+url);
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
                    int newHeight = height == null ? 762 : Integer.parseInt(height);
                    Bitmap img = Bitmap.createScaledBitmap(bitmap,newWidth,newHeight,false);
                    bitmap.recycle();
                    try {
                        Log.d("hanyin_flutter_bluetooth_print","打印准备了--width:" + newWidth + "---height:" + newHeight);
                        try {
                            Bitmap bitmapPrint = img;
                            bitmapPrint = Utility.Tobitmap(bitmapPrint, 555, Utility.getHeight(555, bitmapPrint.getWidth(), bitmapPrint.getHeight()));
                            PrinterHelper.printAreaSize("0","0","0","" + bitmapPrint.getHeight(),times);
                            int printImage = PrinterHelper.printBitmapCPCL(bitmapPrint,0,0,0,0,0);
                            PrinterHelper.Form();
                            PrinterHelper.Print();
                            Log.d("Print", "printImage: " + printImage);
                            if (printImage > 0) {
                                result.success("{\"code\":\"0\",\"desc\":\"打印成功\"}");
                                Log.d("HPRTSDKSample", "打印成功-printImage" + printImage);
                            } else {
                                result.success("{\"code\":\""+printImage+"\",\"desc\":\"打印失败\"}");
                                Log.d("HPRTSDKSample", "打印失败-printImage" + printImage);
                            }
                        } catch (Exception e) {
                            Log.d("HPRTSDKSample", "打印失败-" + e.getMessage().toString());
                            result.success("{\"code\":\"-1\",\"desc\":\"打印失败\"}");
                        }
                    } catch (Exception e) {
                        Log.d("HPRTSDKSample", e.getMessage().toString());
                    }
                }
            });

        } else if ("printBase64Image".equals(call.method)) {
            // 该接口不是实时指令，打印机正在打印时，查询无效
            int status = 0;
            try {
                status = PrinterHelper.getPrinterStatus();
            } catch (Exception e) {
                result.success("{\"code\":\"-1\",\"desc\":\"打印机状态获取失败\"}");
                Log.d("HPRTSDKSample", e.getMessage().toString());
                return;
            }
            if((status & 2) == 2){
                //缺纸
                result.success("{\"code\":\"-1\",\"desc\":\"打印机缺纸或连接失败\"}");
                Log.d("hanyin_flutter_bluetooth_print","打印机缺纸或连接失败");
                return;
            } else if ((status & 4) == 4){
                //开盖
                result.success("{\"code\":\"-1\",\"desc\":\"打印机开盖\"}");
                Log.d("hanyin_flutter_bluetooth_print","打印机开盖");
                return;
            }
            String base64 = call.argument("base64");
            String times = call.argument("times") == null ? "1" : call.argument("times");
            String width = call.argument("width");
            String height = call.argument("height");

            Log.d("hanyin_flutter_bluetooth_print","base64:" + base64);
            if (TextUtils.isEmpty(base64)){
                result.success("{\"code\":\"-1\",\"desc\":\"base64为空，请确认\"}");
                return;
            }

            try {
                Bitmap bitmap = null;
                bitmap = base64ToPicture(base64);
                int newWidth = width == null ? 555 : Integer.parseInt(width);
                int newHeight = height == null ? 762 : Integer.parseInt(height);
                Log.d("hanyin_flutter_bluetooth_print","打印准备了--newWidth"+newWidth);
                Log.d("hanyin_flutter_bluetooth_print","打印准备了--newHeight"+newHeight);
                Bitmap img = Bitmap.createScaledBitmap(bitmap,newWidth,newHeight,true);
                bitmap.recycle();

                Bitmap bitmapPrint = img;
                Log.d("hanyin_flutter_bluetooth_print","打印准备了--width:" + width + "---height:" + height);
                bitmapPrint = Utility.Tobitmap(bitmapPrint, 555, Utility.getHeight(555, bitmapPrint.getWidth(), bitmapPrint.getHeight()));
                PrinterHelper.printAreaSize("0","0","0","" + bitmapPrint.getHeight(),times);
                int printImage = PrinterHelper.printBitmapCPCL(bitmapPrint,0,0,0,0,0);
                PrinterHelper.Form();
                PrinterHelper.Print();

                try {
                    if (printImage > 0) {
                        result.success("{\"code\":\"0\",\"desc\":\"打印成功\"}");
                        Log.d("HPRTSDKSample", "打印成功-printImage" + printImage);
                    } else {
                        result.success("{\"code\":\""+printImage+"\",\"desc\":\"打印失败\"}");
                        Log.d("HPRTSDKSample", "打印失败-printImage" + printImage);
                    }
                } catch (Exception e) {
                    Log.d("HPRTSDKSample", "打印失败-" + e.getMessage().toString());
                    result.success("{\"code\":\"-1\",\"desc\":\"打印失败\"}");
                }
            } catch (Exception e) {
                Log.d("HPRTSDKSample", "打印失败，请重试或检测打印机状态");
                result.success("{\"code\":\"-1\",\"desc\":\"打印失败，请重试或检测打印机状态\"}");
            }
        } else if ("disConnect".equals(call.method)) {
            try {
                boolean disconnect = PrinterHelper.portClose();
                if (disconnect) {
                    result.success("{\"code\":\"0\",\"desc\":\"断开连接成功\"}");
                    Log.d("HPRTSDKSample", "断开连接成功");
                } else {
                    result.success("{\"code\":\"-1\",\"desc\":\"断开连接失败\"}");
                    Log.d("HPRTSDKSample", "断开连接失败");
                }
            } catch (Exception e) {
                result.success("{\"code\":\"-1\",\"desc\":\""+ e.getMessage().toString() +"\"}");
                Log.d("HPRTSDKSample", e.getMessage().toString());
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

        Log.d("hanyin_flutter_bluetooth_print","打印准备了--14" + imgBase64);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
        return bitmap;
//        Bitmap bitmap = null;
//
//        byte[] imgByte = null;
//        InputStream input = null;
//        try{
//            if (picStrInMsg.contains(",")) {
//                picStrInMsg = picStrInMsg.split(",")[1];
//            }
//            imgByte = Base64.decode(picStrInMsg, Base64.DEFAULT);
//            BitmapFactory.Options options=new BitmapFactory.Options();
//            options.inSampleSize = 8;
//            input = new ByteArrayInputStream(imgByte);
//            SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(input, null, options));
//            bitmap = (Bitmap)softRef.get();
//        }catch(Exception e){
//            e.printStackTrace();
//        }finally{
//            if(imgByte!=null){
//                imgByte = null;
//            }
//
//            if(input!=null){
//                try {
//                    input.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return bitmap;
    }

}
