package com.unity.downlib.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.unity.downlib.bean.DownLoadBean;
import com.unity.downlib.bean.IDownAction;
import com.unity.downlib.service.UploadService;

import java.util.List;


public class StationDownLoadManager {

    public static final String TAG = "StationDownLoadManager";
    public static Context mContext;
    private static boolean isLog = false;
    private IStation2Unity delegate;

    public static void attachApplication(Context context,boolean log) {
        mContext = context;
        GlobalConstans.context=context;
        isLog = log;
        UploadService.startCloudDownService(context, IDownAction.DOWN_SERVICE_NOTHING,null);

    }
    // 初始化
    public  void initialize(String json)
    {
        UploadService.startCloudDownService(mContext, IDownAction.DOWN_INIT,null);
    }

    // 添加一条下载地址
    public void addData(String json)
    {
        UploadService.startCloudDownService(mContext, IDownAction.DOWN_ADD_TASK,json);
    }
    // 删除一条下载地址
    public void removeData(String json)
    {
        UploadService.startCloudDownService(mContext, IDownAction.DOWN_REMOVE_TASK,json);
    }

    // 获取当前所有下载任务的信息
    public String getDownLoadState()
    {
        List<DownLoadBean> allDownTask = StationDownLoadController.getInstance().getAllDownTask();

        if (allDownTask!=null&&allDownTask.size()>0){
            String allTaskJson = new Gson().toJson(allDownTask);
            Log(allTaskJson);
            return allTaskJson;
        }
        return null;
    }

    public void PauseDownload(Boolean pause)
    {
        // 需要暂停或恢复下载
        if (pause){
            UploadService.startCloudDownService(mContext, IDownAction.DOWN_SERVICE_PAUSE,null);
        }else {
            UploadService.startCloudDownService(mContext, IDownAction.DOWN_SERVICE_RESUME_DOWN,null);
        }
    }

    //关闭应用嗲用
    public void close()
    {
        UploadService.startCloudDownService(mContext, IDownAction.DOWN_STOP,null);

    }

    public void addListener(IStation2Unity delegate) {
        this.delegate = delegate;
        StationDownLoadController.getInstance().registerDownloadActionListener(new StationDownLoadController.DownloadActionListener() {
            @Override
            public void onProgress(long totalBytes, long downloadedBytes, DownLoadBean downLoadBean) {
                if (delegate!=null){
                    delegate.onProgress(totalBytes,downloadedBytes,new Gson().toJson(downLoadBean));
                }
            }

            @Override
            public void onCompleteOnlyOne(DownLoadBean downLoadBean) {
                if (delegate!=null){
                    delegate.onCompleteOnlyOne(new Gson().toJson(downLoadBean));
                }
            }

            @Override
            public void onFail(DownLoadBean downLoadBean) {
                if (delegate!=null){
                    delegate.onFail(new Gson().toJson(downLoadBean));
                }
            }

            @Override
            public void onCompleteAll() {
                if (delegate!=null){
                    delegate.onCompleteAll();
                }
            }

            @Override
            public void networkDisconnection() {
                if (delegate!=null){
                    delegate.networkDisconnection();
                }
            }

            @Override
            public void networkConnection() {
                if (delegate!=null){
                    delegate.networkConnection();
                }
            }

            @Override
            public void removeSuccess(DownLoadBean downLoadBean) {
                if (delegate!=null){
                    delegate.removeSuccess(new Gson().toJson(downLoadBean));
                }
            }

            @Override
            public void addSuccess(DownLoadBean downLoadBean) {
                if (delegate!=null){
                    delegate.addSuccess(new Gson().toJson(downLoadBean));
                }
            }

            @Override
            public void pauseOrResumeDownloadSuccess(DownLoadBean downLoadBean) {
                if (delegate!=null){
                    delegate.pauseOrResumeDownloadSuccess(new Gson().toJson(downLoadBean));
                }
            }
        });
    }
    private void Log(String msg) {
        if (mContext!=null&& isLog) {
            Log.d(TAG, msg);
        }
    }
}