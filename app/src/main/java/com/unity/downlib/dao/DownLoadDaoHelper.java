package com.unity.downlib.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * User: 1241734684@qq.com
 * Description:
 * Date:2024-08-26 10
 * Time:34
 */
public class DownLoadDaoHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "mydatabase.db";

    public static final String DOWNLOADS = "downloads_table";

    public static final String THREAD = "thread";
    private static final int DATABASE_VERSION = 1;



    private static DownLoadDaoHelper mDownDao;

    public static Context mContext;

    // 构造函数
    private DownLoadDaoHelper() {
        super(mContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DownLoadDaoHelper getInstance(){
        if (mDownDao==null){
            mDownDao=new DownLoadDaoHelper();
        }
        return mDownDao;
    }


    public static void registContext(Context context) {
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("DownDao","=======onCreate=======>创建数据库");
        String createTableQuery = "CREATE TABLE "+DOWNLOADS+" (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, iconUrl TEXT, fileUrl TEXT, fileMd5 TEXT, fileSize INTEGER, localPath TEXT, downState INTEGER, downLoadedSize INTEGER, createDate TEXT)";
        sqLiteDatabase.execSQL(createTableQuery);
        String thread = "CREATE TABLE "+THREAD+" (id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT,threadName TEXT, startLength INTEGER, endLength INTEGER, downLength INTEGER, localPath TEXT, tempFilePath TEXT)";
        sqLiteDatabase.execSQL(thread);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d("DownDao","======onUpgrade========>数据库升级");
    }
}
