package com.cl.android.chartonlyforyou.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.NetworkStatsManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.cl.android.chartonlyforyou.MyApplication;
import com.cl.android.chartonlyforyou.R;
import com.cl.android.chartonlyforyou.baidulocation.LocationService;
import com.cl.android.chartonlyforyou.password.DrawPwdActivity;
import com.cl.android.chartonlyforyou.rongchart.RongCloudEvent;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.message.TextMessage;

public class ChartService extends Service {

    private Notification notification = null;
    private Timer timer;
    private long lasttime;//上一次扫描时间
    private LocationService locationService;


    public ChartService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
            connRongyun();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 开启定时器，每隔 10分钟 刷新一次
        if (timer == null) {
            timer = new Timer();
            lasttime = System.currentTimeMillis();
            timer.scheduleAtFixedRate(new RefreshTask(), 0, 10*60*1000);
        }

        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    private void connRongyun() {
        RongIM.connect(MyApplication.token01, new RongIMClient.ConnectCallback() {
            @Override
            public void onSuccess(String s) {
                RongCloudEvent.init(getApplicationContext()).initListener();
                Log.i("slack", "ChartService connect....");
                forground();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
            }

            @Override
            public void onTokenIncorrect() {
            }
        });
    }

    private void forground() {
        //开启前台service
        if (Build.VERSION.SDK_INT < 16) {
            notification = new Notification.Builder(this)
//                    .setContentTitle("Enter the Chart").setContentText("")
//                    .setSmallIcon(R.drawable.icon)
                    .getNotification();
        } else {
            Notification.Builder builder = new Notification.Builder(this);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, DrawPwdActivity.class), 0);
            builder.setContentIntent(contentIntent);
//            builder.setSmallIcon(R.drawable.icon);
//        builder.setTicker("Foreground Service Start");
//            builder.setContentTitle("Enter the Chart");
//            builder.setContentText(name);
            notification = builder.build();
        }

        startForeground(1, notification);
        Log.i("slack", "ChartService startForeground....");
    }
    //这里查看短信数据库
    class RefreshTask extends TimerTask {
        @Override
        public void run() {
            Log.i("slack", "RefreshTask run....");
            if(notification == null){
                Log.i("slack", "notification == null....");
                connRongyun();
            }

            getSmsFromPhone();

        }
    }

    public void getSmsFromPhone() {
        //String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
        String[] projection = new String[] { "date","body" };
        String where = " date >  " + lasttime ;//从上一次扫描1小时以内的短信
        //先记录发送命令的时间，找这时间以后收到的短信  ，按时间降序排序
        Cursor cur = getContentResolver().query(Uri.parse("content://sms/"), projection, where, null, "date desc");
        lasttime = System.currentTimeMillis();
        if (null == cur) {
            Log.i("slack", "cur = null..........");
            return;
        }

        // 1458371840372   1458365678000
        while( cur.moveToNext() ) {
            String date = cur.getString(cur.getColumnIndex("date"));
            String body = cur.getString(cur.getColumnIndex("body"));
            Log.i("slack", "date:   ......"+date+"body:"+body);
            //这里我是要判断短信内容中有没有匹配字符串
            int i= body.indexOf("#location");
            Log.i("slack", " #location.indexOf(body)........."+i);
            if (body.indexOf("#location") >= 0) {
               //定位,发送短信
                Log.i("slack", "sendMessage.........");
                //定位需要网络，GPS,这里打开wifi(成功)，GPS（error）,数据流量（error）
                //获取 网络连接
                wifiConnection();
                MyApplication.sendMessage();


                break;
            }
        }
    }

    // 这里的wifi 只开不关吧
    private  void  wifiConnection() {
        // http://developer.android.com/reference/android/content/Context.html#getSystemService(java.lang.Class<T>)
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if(networkInfo != null){
            if(networkInfo.isConnected()){
                Log.i("slack",""+networkInfo.isConnected());
                return ;
            }
        }else{
            Log.i("slack", "no network...");
            wifiManager.setWifiEnabled(true);//打开wifi
        }

    }


    // 但是， 用户一键清理时，onDestroy方法都进不来
    @Override
    public void onDestroy() {
        stopForeground(true);
        // Service被终止的同时也停止定时器继续运行
        timer.cancel();
        timer = null;
        Log.i("slack","services destroy....");
        // 直接在onDestroy（）里startService
        Intent sevice = new Intent(this, ChartService.class);
        this.startService(sevice);

        super.onDestroy();

    }
}
