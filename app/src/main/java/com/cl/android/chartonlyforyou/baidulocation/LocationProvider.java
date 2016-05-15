package com.cl.android.chartonlyforyou.baidulocation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.cl.android.chartonlyforyou.MyApplication;

import io.rong.imkit.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

/**
 * Created by chenling on 2016/3/27.
 */
public class LocationProvider extends InputProvider.ExtendProvider {

    private LocationService locationService;
    private boolean issuccess = false;//定位是否成功
    private boolean isurgent = false;
    private String targetId;


    public LocationProvider(RongContext context) {
        super(context);
        isurgent = false;
    }
    /**
     * 设置展示的图标
     * @param context
     * @return
     */
    @Override
    public Drawable obtainPluginDrawable(Context context) {
        return context.getResources().getDrawable(io.rong.imkit.R.drawable.rc_ic_location);
    }

    /**
     * 设置图标下的title
     * @param context
     * @return
     */
    @Override
    public CharSequence obtainPluginTitle(Context context) {
        return context.getString(io.rong.imkit.R.string.rc_plugins_location);
    }
    /**
     * click 事件
     * @param view
     */
    @Override
    public void onPluginClick(View view) {
        getLocation("");
    }
    //获取定位信息，并发送
    public void getLocation(String urgent) {
        Log.i("slack","location start...");
        if(!TextUtils.isEmpty(urgent)){
            isurgent = true;
            targetId = urgent;
        }
        MyApplication.locationTimes = 0;
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
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     */
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.i("slack","location onReceiveLocation...");
            StringBuffer sb = new StringBuffer(256);
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {

                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\nerror code : ");
                sb.append(location.getLocType());
                sb.append("\nlatitude : ");
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");
                sb.append(location.getLongitude());
                sb.append("\nradius : ");
                sb.append(location.getRadius());
                sb.append("\nCountryCode : ");
                sb.append(location.getCountryCode());
                sb.append("\nCountry : ");
                sb.append(location.getCountry());
                sb.append("\ncitycode : ");
                sb.append(location.getCityCode());
                sb.append("\ncity : ");
                sb.append(location.getCity());
                sb.append("\nDistrict : ");
                sb.append(location.getDistrict());
                sb.append("\nStreet : ");
                sb.append(location.getStreet());
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\nDescribe: ");
                sb.append(location.getLocationDescribe());
                sb.append("\nDirection(not all devices have value): ");
                sb.append(location.getDirection());
                sb.append("\nPoi: ");
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
                }
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 单位：米
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                    issuccess = true;
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    sb.append("\noperationers : ");
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                    issuccess = true;
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
            }
            if(issuccess){
                // 信息都在 sb 里
                Log.i("slack",sb.toString());
                //发送定位消息
                final TextMessage content = TextMessage.obtain(sb.toString());
                if (isurgent){
                    //这里真不爽，写死了
                    LocationStart(content,targetId);
                }else{
                    LocationStart(content);
                }

            }else{
                Log.i("slack", "location not success...");
                // 测试100次
                if(MyApplication.locationTimes < 100){
                    MyApplication.locationTimes ++;
                    try {
                        Log.i("slack","location Thread.sleep..."+MyApplication.locationTimes);
                        Thread.sleep( 5 * 1000);//休息5 S
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i("slack","location restart...");
                    locationService.start();// 继续定位SDK
                }else{
                    locationService.unregisterListener(mListener); //注销掉监听
                    locationService.stop();
                }

            }
        }

    };

    private void LocationStart(TextMessage content,String targetId) {
        if (RongIM.getInstance().getRongIMClient() != null)
            Log.i("slack","insertMessage...");
            RongIM.getInstance().getRongIMClient().insertMessage(Conversation.ConversationType.PRIVATE,
                    targetId,
                    null, content,
                    new RongIMClient.ResultCallback<Message>() {
                        @Override
                        public void onSuccess(Message message) {
                            Log.i("slack","message:"+message.toString());
                            message.setSentStatus(Message.SentStatus.SENDING);
                            RongContext.getInstance().executorBackground(new UploadRunnable(message));
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode e) {
                        }
                    });
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop();
    }


    private void LocationStart(TextMessage content) {
        Conversation conversation = getCurrentConversation();

        if (RongIM.getInstance().getRongIMClient() != null)
            RongIM.getInstance().getRongIMClient().insertMessage(conversation.getConversationType(),
                    conversation.getTargetId(),
                    null, content,
                    new RongIMClient.ResultCallback<Message>() {
                        @Override
                        public void onSuccess(Message message) {
                            Log.i("slack","message:"+message.toString());
                            message.setSentStatus(Message.SentStatus.SENDING);
                            RongContext.getInstance().executorBackground(new UploadRunnable(message));
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode e) {
                        }
                    });
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop();
    }

    class UploadRunnable implements Runnable {
        Message msg;

        public UploadRunnable(Message msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            Log.i("slack", "UploadRunnable sendImageMessage");
            RongIM.getInstance().getRongIMClient().sendMessage(msg, null, null, new RongIMClient.SendMessageCallback() {

                @Override
                public void onSuccess(Integer integer) {
                    Log.i("slack", "onSuccess"+integer);
                }

                @Override
                public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                    Log.i("slack", "onError"+errorCode.toString());
                }
            });
        }
    }
}
