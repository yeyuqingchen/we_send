package com.yc.we_send;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ye on 2016/11/4.
 * 开机广播
 */
public class StartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
// 启动应用，参数为需要自动启动的应用的包名，只是启动app的activity的包名
        Intent newIntent = context.getPackageManager()
                .getLaunchIntentForPackage("com.yc.we_send");
        context.startActivity(newIntent);
    }

}
