package com.cl.android.chartonlyforyou.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by chenling on 2016/3/28.
 */
public class BootBroadcastReceiver  extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        //开启前台service
        Intent service = new Intent(context,ChartService.class);
        context.startService(service);
        Log.i("slack","BootBroadcastReceiver....");

    }
}
