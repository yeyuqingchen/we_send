package com.yc.we_send;

import android.app.Application;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;

import java.util.List;

/**
 * Created by Administrator on 2016/11/4.
 */
public class MyApplication extends Application implements AMapLocationListener {

    protected static MyApplication mInstance;

    public static final String my153="15375113906";
    public static final String my187="18715028079";

    //定位
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;

    public  double lat ;
    public  double lng ;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
        EMClient.getInstance().init(mInstance, new EMOptions());
        login();
//        getLocation();



    }
    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        super.onTerminate();
        // 启动应用，参数为需要自动启动的应用的包名，只是启动app的activity的包名
        Intent newIntent = getPackageManager()
                .getLaunchIntentForPackage("com.yc.we_send");
        startActivity(newIntent);
    }
    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        super.onLowMemory();
    }
    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        super.onTrimMemory(level);
    }

    //环信登陆
    public void login(){
        //登录
        new Thread(new Runnable() {
            @Override
            public void run() {
                EMClient.getInstance().login(my187, "123456", new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        Log.i("yc"," 登陆成功");
                        EMMessageListener msgListener = new EMMessageListener() {

                            @Override
                            public void onMessageReceived(List<EMMessage> messages) {
                                //收到消息
                                Log.i("yc"," 收到普通消息");
                            }

                            @Override
                            public void onCmdMessageReceived(List<EMMessage> messages) {
                                Log.i("yc"," 收到穿透消息");
                                getLocation();
//                                Toast.makeText(MyApplication.getApp(),"收到穿透",Toast.LENGTH_LONG).show();
//                                mInstance.startService(new Intent("com.yc.service.SendLocationService"));
                                //收到透传消息
                            }

                            @Override
                            public void onMessageReadAckReceived(List<EMMessage> messages) {
                                //收到已读回执
                            }

                            @Override
                            public void onMessageDeliveryAckReceived(List<EMMessage> message) {
                                //收到已送达回执
                            }

                            @Override
                            public void onMessageChanged(EMMessage message, Object change) {
                                //消息状态变动
                            }
                        };
                        EMClient.getInstance().chatManager().addMessageListener(msgListener);
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(int code, String error) {
                        Log.i("yc"," 登陆失败");
                        login();
//                        Toast.makeText(MyApplication.getApp(),"登陆失败",Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();

    }

    //定位获取经纬度
    private void getLocation(){
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置定位监听
        locationClient.setLocationListener(mInstance);

        // 设置定位参数
        locationOption.setInterval(90000);         //设置定位时间的间隔
        locationClient.setLocationOption(locationOption);

        // 启动定位
        locationClient.startLocation();
        mHandler.sendEmptyMessage(LaLocationUtils.MSG_LOCATION_START);
    }
    Handler mHandler = new Handler() {
        public void dispatchMessage(android.os.Message msg) {
            switch (msg.what) {
                //开始定位
                case LaLocationUtils.MSG_LOCATION_START:
                    break;
                // 定位完成
                case LaLocationUtils.MSG_LOCATION_FINISH:
                    AMapLocation loc = (AMapLocation) msg.obj;
                    if (loc==null)return;
                    lat=loc.getLatitude();
                    lng=loc.getLongitude();
                    Log.i("yc","lat:"+lat+"  lng:"+lng);
                    send(lat,lng,loc.getAddress(), MyApplication.my153);//发送位置
                    break;
                //停止定位
                case LaLocationUtils.MSG_LOCATION_STOP:
                    break;
                default:
                    break;
            }
        };
    };

    private void send(double latitude,double longitude,String locationAddress,String toChatUsername){
        //latitude为纬度，longitude为经度，locationAddress为具体位置内容
//        EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, toChatUsername);
//        EMClient.getInstance().chatManager().sendMessage(message);
        send();
        Log.i("yc","发送了位置消息");
        mHandler.sendEmptyMessage(LaLocationUtils.MSG_LOCATION_FINISH);

    }
    private void send(){
        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        String action=lat+":"+lng;//action可以自定义
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        String toUsername = MyApplication.my153;//发送给某个人
        cmdMsg.setReceipt(toUsername);
        cmdMsg.addBody(cmdBody);
        cmdMsg.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.i("yc","onSuccess");
            }

            @Override
            public void onError(int i, String s) {
                Log.i("yc","onError:"+s);
                if (s.equals("User has not login.")){
                    login();
                }
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
        EMClient.getInstance().chatManager().sendMessage(cmdMsg);

    }

    public static MyApplication getApp() {
        if (mInstance != null && mInstance instanceof MyApplication) {
            return mInstance;
        } else {
            mInstance = new MyApplication();
            mInstance.onCreate();
            return mInstance;
        }
    }


    @Override
    public void onLocationChanged(AMapLocation loc) {
        if (null != loc) {
            Message msg = mHandler.obtainMessage();
            msg.obj = loc;
            msg.what = LaLocationUtils.MSG_LOCATION_FINISH;
            mHandler.sendMessage(msg);
        }
    }
}
