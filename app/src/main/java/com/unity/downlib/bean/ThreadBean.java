package com.unity.downlib.bean;

/**
 * User: 1241734684@qq.com
 * Description:
 * Date:2024-08-27 18
 * Time:36
 */
public class ThreadBean {
    public int id;
    public String threadName;
    public String url;

    public String localPath;
    public long startLength;

    public long endLength;

    public long downLength;

    public String tempFilePath;
    public ThreadBean(){}

    public ThreadBean(int id, String threadName, String url, String localPath, long startLength, long endLength, long downLength, String tempFilePath) {
        this.id = id;
        this.threadName = threadName;
        this.url = url;
        this.localPath = localPath;
        this.startLength = startLength;
        this.endLength = endLength;
        this.downLength = downLength;
        this.tempFilePath = tempFilePath;
    }

    @Override
    public String toString() {
        return "ThreadBean{" +
                "id=" + id +
                ", threadName='" + threadName + '\'' +
                ", url='" + url + '\'' +
                ", localPath='" + localPath + '\'' +
                ", startLength=" + startLength +
                ", endLength=" + endLength +
                ", downLength=" + downLength +
                ", tempFilePath='" + tempFilePath + '\'' +
                '}';
    }
}
