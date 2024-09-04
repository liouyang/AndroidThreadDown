package com.unity.downlib;

import android.app.Application;
import android.content.Context;

import com.unity.downlib.utils.StationDownLoadManager;


/**
 * User: 1241734684@qq.com
 * Description:
 * Date:2024-08-27 16
 * Time:42
 */
public class MyApplication extends Application {
    Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        StationDownLoadManager.attachApplication(context,true,"", "");
    }
}
