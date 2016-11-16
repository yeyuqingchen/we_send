package com.yc.we_send;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;


/**
 * Created by Administrator on 2016/11/4.
 * 用来发送位置信息
 */
public class SendLocationService extends Service implements AMapLocationListener {

    //定位
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;

    public  double lat ;
    public  double lng ;



    @Override
    public void onCreate() {
        super.onCreate();
        getLocation();

    }

    //定位获取经纬度
    private void getLocation(){
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置定位监听
        locationClient.setLocationListener(this);

        // 设置定位参数
        locationOption.setInterval(90000);         //设置定位时间的间隔
        locationClient.setLocationOption(locationOption);

        // 启动定位
        locationClient.startLocation();
        mHandler.sendEmptyMessage(LaLocationUtils.MSG_LOCATION_START);
    }
    Handler mHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                //开始定位
                case LaLocationUtils.MSG_LOCATION_START:
                    break;
                // 定位完成
                case LaLocationUtils.MSG_LOCATION_FINISH:
                    AMapLocation loc = (AMapLocation) msg.obj;
                    lat=loc.getLatitude();
                    lng=loc.getLongitude();
                    send(lat,lng,loc.getAddress(), MyApplication.my187);//发送位置
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
        EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, toChatUsername);
        EMClient.getInstance().chatManager().sendMessage(message);
        Log.i("yc","发送了位置消息");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
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
