package com.unity.downlib.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.unity.downlib.bean.DownLoadBean;
import com.unity.downlib.bean.ThreadBean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: 1241734684@qq.com
 * Description:
 * Date:2024-08-27 09
 * Time:46
 */
public class ContentProviderHelper {

    public static String[] threadProjection = {
            "id",
            "threadName",
            "url",
            "startLength",
            "endLength",
            "downLength",
            "localPath",
            "tempFilePath"
    };



    // 定义要查询的列
   public static String[] projection = {
            "id",
            "name",
            "iconUrl",
            "localPath",
            "fileUrl",
            "fileMd5",
            "fileSize",
            "createDate",
            "downState",
            "downLoadedSize"
    };

    /**
     * 查询所有数据
     * @param context
     * @return
     */
    public static List<ThreadBean> queryAllThreadDate(Context context){
        List<ThreadBean> mThreadBeanList = new ArrayList<>();

        // 执行查询
        Cursor cursor =context. getContentResolver().query(
                DownLoadContentProvider.THREAD_CONTENT_URI,
                threadProjection,
                null,
                null,
                null
        );

        // 处理查询结果
        if (cursor != null && cursor.moveToFirst()) {

            do {
                ThreadBean mThreadBean = new ThreadBean();

                int id1 = cursor.getColumnIndex("id");
                int id = cursor.getInt(id1);
                mThreadBean.id=id;
                int name1 = cursor.getColumnIndex("url");
                String url = cursor.getString(name1);
                mThreadBean.url=url;
                int threadNameIndex = cursor.getColumnIndex("threadName");
                String threadName = cursor.getString(threadNameIndex);
                mThreadBean.threadName=threadName;
                int startLengthIndex = cursor.getColumnIndex("startLength");
                long startLength = cursor.getLong(startLengthIndex);
                mThreadBean.startLength=startLength;
                int endLengthIndex = cursor.getColumnIndex("endLength");
                long endLength = cursor.getLong(endLengthIndex);
                mThreadBean.endLength=endLength;
                int loadingStateIndex = cursor.getColumnIndex("downLength");
                long downLength = cursor.getLong(loadingStateIndex);
                mThreadBean.downLength=downLength;

                int localPathIndex = cursor.getColumnIndex("localPath");
                String localPath = cursor.getString(localPathIndex);
                mThreadBean.localPath=localPath;

                int tempFilePathIndex = cursor.getColumnIndex("tempFilePath");
                String tempFilePath = cursor.getString(tempFilePathIndex);
                mThreadBean.tempFilePath=tempFilePath;

                System.out.println("mThreadBean: " +mThreadBean.toString());
                mThreadBeanList.add(mThreadBean);
            } while (cursor.moveToNext());

            cursor.close(); // 使用完 Cursor 后一定要关闭

        }
        return mThreadBeanList;
    }

    public static void updateThreadTable(Context context,String threadName,long downLength){
//        "id",

//                "url",
//                "startLength",
//                "endLength",
//                "loadingState"
        ContentValues values = new ContentValues();
        values.put("downLength",downLength);

        // 查询条件
        String selection = "threadName = ?";
        String[] selectionArgs = new String[]{ threadName+""};
        int update = context.getContentResolver().update(DownLoadContentProvider.THREAD_CONTENT_URI, values, selection,selectionArgs);
        // 检查更新的行数
        if (update > 0) {
            System.out.println("Update successful, rows updated: " + update);
        } else {
            System.out.println("No rows updated.");
        }

    }


    public static void insertThreadTable(Context context,ThreadBean threadBean){
//        "id",
//                "url",
//                "startLength",
//                "endLength",
//                "loadingState"

        ContentValues values = new ContentValues();
        values.put("url", threadBean.url);
        values.put("threadName", threadBean.threadName);
        values.put("startLength", threadBean.startLength);
        values.put("endLength",  threadBean.endLength);
        values.put("downLength",  threadBean.downLength);
        values.put("localPath",  threadBean.localPath);
        values.put("tempFilePath",  threadBean.tempFilePath);
        Uri insert = context.getContentResolver().insert(
                DownLoadContentProvider.THREAD_CONTENT_URI,
                values
        );
        if (insert != null) {
            // 插入成功，处理返回的新 URI
            System.out.println("Inserted new record at: " + insert.toString());
        } else {
            // 插入失败
            System.out.println("Failed to insert new record.");
        }


    }

    public static void deleterThreadTable(Context context,String url){
//        "id",
//                "url",
//                "startLength",
//                "endLength",
//                "loadingState"

        // 查询条件
        String selection = "url = ?";
        String[] selectionArgs = new String[]{ url};
        int delete = context.getContentResolver().delete(DownLoadContentProvider.THREAD_CONTENT_URI, selection, selectionArgs);
        // 检查更新的行数
        if (delete != 0) {
            System.out.println("Update successful, rows updated: " + delete);
        } else {
            System.out.println("No rows updated.");
        }

    }



    /**
     * 查询有没有在下载中的任务表
     * @param context
     * @return
     */
    public static DownLoadBean queryTable(Context context,String selection,String[] selectionArgs){

        // 执行查询
        Cursor cursor =context. getContentResolver().query(
                DownLoadContentProvider.SSID_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );

        // 处理查询结果
        if (cursor != null && cursor.moveToFirst()) {
            DownLoadBean downLoadBean = new DownLoadBean();

            do {
                int id1 = cursor.getColumnIndex("id");
                int id = cursor.getInt(id1);
                downLoadBean.id=id;
                int name1 = cursor.getColumnIndex("name");
                String name = cursor.getString(name1);
                downLoadBean.name=name;
                int iconUrl1 = cursor.getColumnIndex("iconUrl");
                String iconUrl = cursor.getString(iconUrl1);
                downLoadBean.iconUrl=iconUrl;
                int localPath1 = cursor.getColumnIndex("localPath");
                String localPath = cursor.getString(localPath1);
                downLoadBean.localPath=localPath;
                int fileUrl1 = cursor.getColumnIndex("fileUrl");
                String fileUrl = cursor.getString(fileUrl1);
                downLoadBean.fileUrl=fileUrl;
                int fileMd51 = cursor.getColumnIndex("fileMd5");
                String fileMd5 = cursor.getString(fileMd51);
                downLoadBean.fileMd5=fileMd5;
                int fileSize1 = cursor.getColumnIndex("fileSize");
                long fileSize = cursor.getLong(fileSize1);
                downLoadBean.fileSize=fileSize;
                int createDate1 = cursor.getColumnIndex("createDate");
                String createDate = cursor.getString(createDate1);
                downLoadBean.createDate=createDate;
                int downState1 = cursor.getColumnIndex("downState");
                int downState = cursor.getInt(downState1);
                downLoadBean.downState=downState;
                int downLoadedSize1 = cursor.getColumnIndex("downLoadedSize");
                long downLoadedSize = cursor.getLong(downLoadedSize1);
                downLoadBean.downLoadedSize=downLoadedSize;
                // 在这里处理每一行的数据，例如：
                System.out.println("ID: " + id + ", Name: " + name + ", State: " + downState);

            } while (cursor.moveToNext());

            cursor.close(); // 使用完 Cursor 后一定要关闭

            return downLoadBean;
        }

        return null;
    }

    /**
     * 查询所有数据
     * @param context
     * @return
     */
    public static List<DownLoadBean> queryAllDate(Context context){
        List<DownLoadBean> downLoadBeans = new ArrayList<>();

        // 执行查询
        Cursor cursor =context. getContentResolver().query(
                DownLoadContentProvider.SSID_CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        // 处理查询结果
        if (cursor != null && cursor.moveToFirst()) {

            do {
                DownLoadBean downLoadBean = new DownLoadBean();
                int id1 = cursor.getColumnIndex("id");
                int id = cursor.getInt(id1);
                downLoadBean.id=id;
                int name1 = cursor.getColumnIndex("name");
                String name = cursor.getString(name1);
                downLoadBean.name=name;
                int iconUrl1 = cursor.getColumnIndex("iconUrl");
                String iconUrl = cursor.getString(iconUrl1);
                downLoadBean.iconUrl=iconUrl;
                int localPath1 = cursor.getColumnIndex("localPath");
                String localPath = cursor.getString(localPath1);
                downLoadBean.localPath=localPath;
                int fileUrl1 = cursor.getColumnIndex("fileUrl");
                String fileUrl = cursor.getString(fileUrl1);
                downLoadBean.fileUrl=fileUrl;
                int fileMd51 = cursor.getColumnIndex("fileMd5");
                String fileMd5 = cursor.getString(fileMd51);
                downLoadBean.fileMd5=fileMd5;
                int fileSize1 = cursor.getColumnIndex("fileSize");
                long fileSize = cursor.getLong(fileSize1);
                downLoadBean.fileSize=fileSize;
                int createDate1 = cursor.getColumnIndex("createDate");
                String createDate = cursor.getString(createDate1);
                downLoadBean.createDate=createDate;
                int downState1 = cursor.getColumnIndex("downState");
                int downState = cursor.getInt(downState1);
                downLoadBean.downState=downState;
                int downLoadedSize1 = cursor.getColumnIndex("downLoadedSize");
                long downLoadedSize = cursor.getLong(downLoadedSize1);
                downLoadBean.downLoadedSize=downLoadedSize;
                // 在这里处理每一行的数据，例如：
                System.out.println("ID: " + id + ", Name: " + name + ", State: " + downState);
                downLoadBeans.add(downLoadBean);
            } while (cursor.moveToNext());

            cursor.close(); // 使用完 Cursor 后一定要关闭

        }
        return downLoadBeans;
    }

    /**
     * 插入数据
     * @param context
     * @param downLoadBean
     * @return
     */
    public static Uri insetDownLoadTable(Context context,DownLoadBean downLoadBean){
        ContentValues values = new ContentValues();
        values.put("name", downLoadBean.name);
        values.put("iconUrl", downLoadBean.iconUrl);
        values.put("localPath",  downLoadBean.localPath);
        values.put("fileUrl",  downLoadBean.fileUrl);
        values.put("fileMd5", downLoadBean.fileMd5);
        values.put("fileSize", downLoadBean.fileSize);
        values.put("createDate",  downLoadBean.createDate);
        values.put("downState", downLoadBean.downState);
        values.put("downLoadedSize", downLoadBean.downLoadedSize);
        Uri insert = context.getContentResolver().insert(
                DownLoadContentProvider.SSID_CONTENT_URI,
                values
        );
        if (insert != null) {
            // 插入成功，处理返回的新 URI
            System.out.println("Inserted new record at: " + insert.toString());
        } else {
            // 插入失败
            System.out.println("Failed to insert new record.");
        }
        return insert;
    }


    /**
     *
     * @param context
     * @param oldDownState
     * @param name
     * @param newState
     */
    public static int updateDownLoadStateTable(Context context,int oldDownState,String name,int newState){
        ContentValues values = new ContentValues();
        values.put("downState",newState);

        // 查询条件
        String selection = "downState = ? AND name = ?";
        String[] selectionArgs = new String[]{oldDownState+"", name};
        int update = context.getContentResolver().update(DownLoadContentProvider.SSID_CONTENT_URI, values, selection, selectionArgs);
        // 检查更新的行数
        if (update > 0) {
            System.out.println("Update successful, rows updated: " + update);
        } else {
            System.out.println("No rows updated.");
        }
        return update;
    }


    public static void updateDownLoadFilePathTable(Context context,String name,String localPath){
        ContentValues values = new ContentValues();
        values.put("localPath",localPath);

        // 查询条件
        String selection = "name = ?";
        String[] selectionArgs = new String[]{ name};
        int update = context.getContentResolver().update(DownLoadContentProvider.SSID_CONTENT_URI, values, selection, selectionArgs);
        // 检查更新的行数
        if (update > 0) {
            System.out.println("Update successful, rows updated: " + update);
        } else {
            System.out.println("No rows updated.");
        }

    }


    public static void updateDownLoadLengthTable(Context context,String name,long downLoadSize){
        ContentValues values = new ContentValues();
        values.put("downLoadedSize",downLoadSize);

        // 查询条件
        String selection = "fileUrl = ?";
        String[] selectionArgs = new String[]{ name};
        int update = context.getContentResolver().update(DownLoadContentProvider.SSID_CONTENT_URI, values, selection, selectionArgs);
        // 检查更新的行数
        if (update > 0) {
            System.out.println("Update successful, rows updated: " + update);
        } else {
            System.out.println("No rows updated.");
        }

    }

    /**
     *
     * @param context
     * @param url
     * @param downLoadSize
     */
    public static void updateDownLoadLengthTableByUrl(Context context,String url,long downLoadSize){
        ContentValues values = new ContentValues();
        values.put("downLoadedSize",downLoadSize);

        // 查询条件
        String selection = "fileUrl = ?";
        String[] selectionArgs = new String[]{ url};
        int update = context.getContentResolver().update(DownLoadContentProvider.SSID_CONTENT_URI, values, selection, selectionArgs);
        // 检查更新的行数
        if (update > 0) {
            System.out.println("Update successful, rows updated: " + update);
        } else {
            System.out.println("No rows updated.");
        }

    }





    public static int deleterDownLoadStateTable(Context context,String fileUrl){


        // 查询条件
        String selection = "fileUrl = ?";
        String[] selectionArgs = new String[]{ fileUrl};
        int delete = context.getContentResolver().delete(DownLoadContentProvider.SSID_CONTENT_URI, selection, selectionArgs);
        // 检查更新的行数
        if (delete != 0) {
            System.out.println("Update successful, rows updated: " + delete);
        } else {
            System.out.println("No rows updated.");
        }
        return delete;
    }

}
