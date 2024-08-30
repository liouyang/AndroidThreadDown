package com.unity.downlib.dao;

import static com.unity.downlib.dao.DownLoadDaoHelper.DOWNLOADS;
import static com.unity.downlib.dao.DownLoadDaoHelper.THREAD;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * User: 1241734684@qq.com
 * Description:
 * Date:2024-08-26 10
 * Time:28
 */
public class DownLoadContentProvider extends ContentProvider {

    public static final String TAG="DownLoadContentProvider";

    public final static String  AUTHORITY="cm.station.provider.contentprovider";



    public static final Uri SSID_CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/"+DOWNLOADS);

    public static final Uri THREAD_CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/"+THREAD);




    private static final int TABLE1 = 1;
    private static final int TABLE2 = 2;

    private static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,DOWNLOADS,TABLE1);
        uriMatcher.addURI(AUTHORITY,THREAD,TABLE2);
    }

    private DownLoadDaoHelper downLoadDaoHelper;

    @Override
    public boolean onCreate() {
        DownLoadDaoHelper.registContext(getContext());
        downLoadDaoHelper = DownLoadDaoHelper.getInstance();
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase readableDatabase = downLoadDaoHelper.getWritableDatabase();

        Cursor query =null;


        switch (uriMatcher.match(uri)){
            case TABLE1:
                query=   readableDatabase.query(DOWNLOADS, projection, selection, selectionArgs, null, null, sortOrder);
                Log.e(TAG, "query TABLE1 " + uri);
                break;
            case TABLE2:
                query=   readableDatabase.query(THREAD, projection, selection, selectionArgs, null, null, sortOrder);
                Log.e(TAG, "query TABLE2 " + uri);
                break;

            default:
        }

        if (query != null) {
            query.setNotificationUri(getContext().getContentResolver(), uri);
        }
//                Log.d(TAG,query.)
        return query;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase writableDatabase = downLoadDaoHelper.getWritableDatabase();
        long id = -1;
        switch (uriMatcher.match(uri)){
            case TABLE1:
                id= writableDatabase.insert(DOWNLOADS, null, contentValues);
                Log.e(TAG, "insert TABLE1 " + uri);
                getContext().getContentResolver().notifyChange(uri,null);

                break;
            case TABLE2:
                Log.e(TAG, "insert TABLE2 " + uri);
                id=   writableDatabase.insert(THREAD, null, contentValues);
                break;

            default:
        }

        if (id == -1) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase writableDatabase = downLoadDaoHelper.getWritableDatabase();
        int delete = 0;

        switch (uriMatcher.match(uri)){
            case TABLE1:
                Log.e(TAG, "delete TABLE1 " + uri);
                delete=writableDatabase.delete(DOWNLOADS, s, strings);
                if (delete!=0){
                    Log.e(TAG, "delete row for " + uri);
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            case TABLE2:
                Log.e(TAG, "delete TABLE2 " + uri);
                delete=   writableDatabase.delete(THREAD, s, strings);
                break;

            default:
        }

        return delete;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] strings) {
        SQLiteDatabase database = downLoadDaoHelper.getWritableDatabase();

        int rowsUpdated = 0;

        switch (uriMatcher.match(uri)){
            case TABLE1:
                Log.e(TAG, "update TABLE1 " + uri);
                rowsUpdated=database.update(DOWNLOADS, contentValues, selection, strings);
                if (rowsUpdated != 0) {
                    Log.e(TAG, "update row for " + uri);
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            case TABLE2:
                Log.e(TAG, "update TABLE2 " + uri);
                rowsUpdated=database.update(THREAD, contentValues, selection, strings);

            default:
        }


        return rowsUpdated;
    }
}
