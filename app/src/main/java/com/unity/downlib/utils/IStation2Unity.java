package com.unity.downlib.utils;



public interface IStation2Unity {

    void onProgress(long totalBytes, long downloadedBytes, String downLoadBean);

    void onCompleteOnlyOne(String downLoadBean);

    void onFail(String downLoadBean);

    void onCompleteAll();

    void networkDisconnection();

    void networkConnection();

    void removeSuccess(String downLoadBean);

    void addSuccess(String downLoadBean);

    void pauseOrResumeDownloadSuccess(String downLoadBean);
}
