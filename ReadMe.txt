应用打开是九格宫解锁界面，输入正确密码后联网，成功后直接进入聊天界面，打开一个新界面，进入互聊后finish.
聊天要有提醒，声音和震动，图片可以点击放大，支持语音聊天类似打电话，视频看看能不能支持，地图定位也要支持，
主动获取对方的定位，发送关键字，定位，监听收到消息的服务，如果接收到的是定位，返回定位信息，做好做的没有声音，

这个融云默认是有震动和声音的，怎么关闭？
修改圆形头像：http://blog.csdn.net/qq_19986309/article/details/50175897
修改聊天背景图片 http://support.rongcloud.cn/kb/Mjg2  文件里有多处，要找清楚
在IMKIT里
相册：http://support.rongcloud.cn/kb/MzU0   demo: https://github.com/13120241790/SeaStar

如何做到一直接收消息？试了一下，晚上打开，但是早上的消息没哟收到，要登录后才可以收到，看见官方有push消息
https://developer.rongcloud.cn/ticket/info/2OAl4fo+7jry5H7lRw==?type=1
关于常驻内存，太那啥了，没有找到合适的信息，我还是自己写一个前台service吧
说实话呀，我也不清楚自己怎么改的，反正现在清内存清不了，哈哈哈
1.手机要把启动权限给了，这是必须的
2.application 加上 android:persistent="true" 难道是这个？
3.参考：http://blog.csdn.net/mad1989/article/details/22492519
前台线程 setSmallIcon(R.drawable.icon) 这个属性不设置，就不显示了，哈哈哈，跟没有一样

不同机型真的不一样，我在魅族上这么干就可以，内存清不了，但是在小米上，连广播都接收不到！华为上也不行


从颦儿改到我这边，需要修改如下：
我是chenling002:{"code":200,"userId":"chenling002","token":"FgHa3+S58iy9E/UcG/4NI1AaIj884ZIPjiEYlxpZXJaOkHNgP8OYA7sPvuX00tkx8rr3hrSSidzwLgqaAwicRPabOW3CYFve"}
chenling001    :{"code":200,"userId":"chenling001","token":"pUWTJzVVjVeaIl6fGdY0/+7HOY8NyemRZGLd2y9cigDYvuEqaB2tovYOteEA8JH8UQwoKQusoO5bSRgV/pVJTbaVjr61WdEo"}
com.cl.android.chartonlyforyou.password.DrawPwdActivity.connectRongyun  connectRongyun(token02);
com.cl.android.chartonlyforyou.rongchart.SettingFragment.view   RongIM.getInstance().startPrivateChat(getContext(), "chenling001", "私人聊天");
com/cl/android/chartonlyforyou/rongchart/RongCloudEvent.java:190 locationProvider.getLocation("chenling001");
com.cl.android.chartonlyforyou.services.ChartService.connRongyun   RongIM.connect(MyApplication.token02, new RongIMClient.ConnectCallback()

新增发送指令以短信返回当前定位，如果定位失败，则停5s,接着再尝试100次，依旧失败，则发送一条空短信，为的是获取当前手机号。