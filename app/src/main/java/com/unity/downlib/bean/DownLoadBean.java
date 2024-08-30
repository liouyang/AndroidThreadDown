package com.unity.downlib.bean;

/**
 * User: 1241734684@qq.com
 * Description:
 * Date:2024-08-26 11
 * Time:44
 */
public class DownLoadBean {

    public int id;
    public String name;
    public String iconUrl;
    public String localPath;
    public String fileUrl;
    public String fileMd5;
    public long fileSize;
    public String createDate;

    public int downState;
    public long downLoadedSize;

    @Override
    public String toString() {
        return "DownBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", localPath='" + localPath + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileMd5='" + fileMd5 + '\'' +
                ", fileSize=" + fileSize +
                ", downState=" + downState +
                ", downProgress=" + downLoadedSize +
                ", createDate='" + createDate + '\'' +
                '}';
    }
}
