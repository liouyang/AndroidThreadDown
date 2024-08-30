package com.unity.downlib.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.unity.downlib.bean.DownLoadBean;
import com.unity.downlib.bean.IDownAction;
import com.unity.downlib.utils.StationDownLoadController;

import java.util.Objects;

/**
 * User: 1241734684@qq.com
 * Description:
 * Date:2024-08-26 09
 * Time:54
 */
public class UploadService extends Service {

    private static final String TAG="UploadService";

    public static final String PARAM_KEY="PARAM_KEY";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"------------<初始化控制器");
        StationDownLoadController.getInstance().init();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    /**
     * 启动服务指令
     * @param mContext
     * @param action
     * @param json
     */
    public static void startCloudDownService(Context mContext, String action,String json) {
        Intent intent = new Intent(mContext, UploadService.class);
        intent.putExtra(PARAM_KEY,json);
        mContext.startService(intent.setAction(action));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        startForegroundService();
        String action = intent.getAction();
        Log.d(TAG, "-------onStartCommand--->"+ Objects.requireNonNull(action));
        switch (Objects.requireNonNull(action)) {
            case IDownAction.DOWN_SERVICE_NOTHING:
                Log.d(TAG,"----nothing----->");
                break;
            case IDownAction.DOWN_ADD_TASK:
                String mBeanJson = intent.getStringExtra(PARAM_KEY);
                Log.d(TAG,"----添加----->"+mBeanJson);
                DownLoadBean downLoadBean = new Gson().fromJson(mBeanJson, DownLoadBean.class);

                StationDownLoadController.getInstance().addDownLoadTask(downLoadBean);
                //添加一个
                break;
            case IDownAction.DOWN_REMOVE_TASK:
                //移除一个
                String mRemoveBeanJson = intent.getStringExtra(PARAM_KEY);
                Log.d(TAG,"----移除----->"+mRemoveBeanJson);
                DownLoadBean mRemoveBean = new Gson().fromJson(mRemoveBeanJson, DownLoadBean.class);
                StationDownLoadController.getInstance().removeBeanTask(mRemoveBean);
                break;
            case IDownAction.DOWN_INIT:
                Log.d(TAG,"----移除初始化上一次下载任务----->");
                //初始化下载服务上次有下载去下载
                StationDownLoadController.getInstance().initLastDownTask();
                break;
            case IDownAction.DOWN_SERVICE_PAUSE:
                //暂停
                Log.d(TAG,"----暂停----->");
                StationDownLoadController.getInstance().pause();
                break;

            case IDownAction.DOWN_SERVICE_RESUME_DOWN:
                //恢复下载
                Log.d(TAG,"----恢复下载----->");
                StationDownLoadController.getInstance().resume();
                break;

            case IDownAction.DOWN_STOP:
                stopSelfService(this);
                break;
            default:
                break;
        }

        return Service.START_STICKY;
    }

    public static void stopSelfService(Context mContext){
        mContext.stopService(new Intent(mContext,UploadService.class));
    }


    private void startForegroundService() {
        // 创建通知
        Notification notification = createNotification();
        // 将服务置于前台，并显示通知
        startForeground(1, notification);
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "my_channel_id")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG,"----暂停服务----->");
        StationDownLoadController.getInstance().onDestroy();
        stopForeground(true);
        super.onDestroy();

    }
}
