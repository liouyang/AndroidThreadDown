package com.unity.downlib;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unity.downlib.bean.DownLoadBean;
import com.unity.downlib.bean.TaskState;
import com.unity.downlib.dao.ContentProviderHelper;
import com.unity.downlib.dao.DownLoadContentProvider;
import com.unity.downlib.utils.GlobalConstans;
import com.unity.downlib.utils.StationDownLoadController;
import com.unity.downlib.utils.StationDownLoadManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG="MainActivity";

    private MyAdapter myAdapter;
    // 在活动或碎片中创建一个 HandlerThread
    HandlerThread handlerThread =null ;

    private StationDownLoadManager stationDownLoadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handlerThread= new HandlerThread("DownloadObserverThread");
        handlerThread.start();
        TaskState.map.put(TaskState.None,"未初始化");
        TaskState.map.put(TaskState.Await,"等待");
        TaskState.map.put(TaskState.Downloaded,"下载完成");
        TaskState.map.put(TaskState.Downloading,"下载中");
        TaskState.map.put(TaskState.Pause,"暂停");
        TaskState.map.put(TaskState.Error,"异常");
        RecyclerView mRecyclerView = findViewById(R.id.recyclerview);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        GlobalConstans.context=this.getApplicationContext();
        StationDownLoadManager.mContext=this.getApplicationContext();
        if (myAdapter==null){
            myAdapter=new MyAdapter(this);
        }
        mRecyclerView.setAdapter(myAdapter);


        stationDownLoadManager = new StationDownLoadManager();
//        stationDownLoadManager.initialize("");

        StationDownLoadController.getInstance().registerDownloadActionListener(new StationDownLoadController.DownloadActionListener() {

            @Override
            public void onProgress(long totalBytes, long downloadedBytes, DownLoadBean downLoadBean3) {
               if (myAdapter!=null){
                   if (myAdapter.dataList!=null&&myAdapter.dataList.size()>0){
                       for (int i1 = 0; i1 < myAdapter.dataList.size(); i1++) {
                           DownLoadBean downLoadBean1 = myAdapter.dataList.get(i1);
                           if (downLoadBean1.name.equals(downLoadBean3.name)){
                               downLoadBean1.downLoadedSize=downloadedBytes;

                           }
                       }
                       MainActivity.this.runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               myAdapter.notifyDataSetChanged();
                           }
                       });
                   }
               }
            }

            @Override
            public void onCompleteOnlyOne(DownLoadBean downLoadBean) {
                Log.d(TAG,"+=====onCompleteOnlyOne======》"+downLoadBean.toString());
            }

            @Override
            public void onFail(DownLoadBean downLoadBean) {
                Log.d(TAG,"+=====onFail======》"+downLoadBean.toString());
            }

            @Override
            public void onCompleteAll() {
                Log.d(TAG,"+=====onCompleteAll======》");
            }

            @Override
            public void networkDisconnection() {
                Log.d(TAG,"+=====networkDisconnection======》");
            }

            @Override
            public void networkConnection() {
                Log.d(TAG,"+=====networkConnection======》");
            }

            @Override
            public void removeSuccess(DownLoadBean downLoadBean) {
                Log.d(TAG,"+=====removeSuccess======》");
            }

            @Override
            public void addSuccess(DownLoadBean downLoadBean) {
                Log.d(TAG,"+=====addSuccess======》");
            }

            @Override
            public void pauseOrResumeDownloadSuccess(DownLoadBean downLoadBean) {
                Log.d(TAG,"+=====pauseOrResumeDownloadSuccess======》");
            }


        });

        myAdapter.setItemCallBack(new ItemCallBack() {
            @Override
            public void callBack(DownLoadBean downBean, int position, View view) {
                Log.d("点击","=-=----<"+downBean.toString());
                stationDownLoadManager.addData(new Gson().toJson(downBean));
            }
        });
       View view= findViewById(R.id.tv_state2);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StationDownLoadController.getInstance().isPaused){
                    stationDownLoadManager.pauseDownload(false);
                }else {
                    stationDownLoadManager.pauseDownload(true);
                }
            }
        });

        View tv_del= findViewById(R.id.tv_del);
        tv_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stationDownLoadManager.removeData( new Gson().toJson(StationDownLoadController.getInstance().currentBean));
            }
        });

        View tv_start= findViewById(R.id.tv_start);
        tv_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,TestActivity.class));
            }
        });

        getContentResolver().registerContentObserver(DownLoadContentProvider.SSID_CONTENT_URI, true, new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                List<DownLoadBean> downLoadBeans = ContentProviderHelper.queryAllDate(MainActivity.this);
                for (int i = 0; i < downLoadBeans.size(); i++) {
                    DownLoadBean downLoadBean = downLoadBeans.get(i);
                    Log.d(TAG,"---downLoadBean----<"+downLoadBean.toString());
                }
                if (downLoadBeans.size()>0){
                    for (int i = 0; i < downLoadBeans.size(); i++) {
                        DownLoadBean downLoadBean = downLoadBeans.get(i);

                        for (int i1 = 0; i1 < myAdapter.dataList.size(); i1++) {
                            DownLoadBean downLoadBean1 = myAdapter.dataList.get(i1);
                            if (downLoadBean1.name.equals(downLoadBean.name)){
                                downLoadBean1.downState=downLoadBean.downState;
                            }
                        }
                    }
                }
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myAdapter.notifyDataSetChanged();
                    }
                });

            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    InputStream open = getAssets().open("testobject.json");
                    List <DownLoadBean> dataList= new Gson().fromJson(convertStreamToString(open), new TypeToken<List<DownLoadBean>>() {
                    }.getType());


                    List<DownLoadBean> downLoadBeans = ContentProviderHelper.queryAllDate(MainActivity.this);

                    if (downLoadBeans.size()>0){

                        for (int i = 0; i < dataList.size(); i++) {
                            DownLoadBean downLoadBean = dataList.get(i);
                            for (int j = 0; j < downLoadBeans.size(); j++) {
                                DownLoadBean downLoadBean1 = downLoadBeans.get(j);
                                if (downLoadBean1.name.equals(downLoadBean.name)){
                                    downLoadBean.downState=downLoadBean1.downState;
                                    downLoadBean.downLoadedSize=downLoadBean1.downLoadedSize;

                                }
                            }
                        }


                    }
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myAdapter.setNewData(dataList);
                        }
                    });

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }

    public String convertStreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        reader.close();
        return stringBuilder.toString();
    }

    class MyAdapter extends RecyclerView.Adapter<MyHolder>{

        private Context context;
        public List<DownLoadBean> dataList;

        public MyAdapter(Context context) {
            this.context=context;
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyHolder(LayoutInflater.from(context).inflate(R.layout.item_down,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {

            int ps=position;

            DownLoadBean downBean = dataList.get(position);

            Glide.with(context).load(downBean.iconUrl).into(holder.iv_cover);

            holder.tv_name.setText(downBean.name);

            holder.tv_state.setText(TaskState.map.get(downBean.downState)+"");
            holder.mProgress.setMax(Integer.valueOf((int) downBean.fileSize));
            holder.mProgress.setProgress((int)((downBean.downLoadedSize)));
            float percentage = (downBean.downLoadedSize / (float) downBean.fileSize) * 100;

            DecimalFormat df = new DecimalFormat("#"); // 仅显示整数部分
            String formattedPercentage = df.format(percentage);


            holder.tv_progress.setText("进度"+(formattedPercentage)+"%");

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemCallBack!=null){
                        mItemCallBack.callBack(downBean,ps,view);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return dataList==null?0:dataList.size();
        }

        public void setNewData(List<DownLoadBean> dataList) {
            this.dataList = dataList;
            notifyDataSetChanged();
        }

        private ItemCallBack mItemCallBack=null;

        public  void  setItemCallBack(ItemCallBack mItemCallBack){
            this.mItemCallBack=mItemCallBack;
        }

    }


    interface ItemCallBack{
        void callBack(DownLoadBean downBean, int position, View view);
    }



    static class MyHolder extends RecyclerView.ViewHolder {

        private  ImageView iv_cover;
        private TextView tv_name;

        private  TextView tv_progress;
        private  TextView tv_state;
        private ProgressBar mProgress;

        private View  itemView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
            iv_cover = itemView.findViewById(R.id.iv_cover);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_state = itemView.findViewById(R.id.tv_state);
            tv_progress = itemView.findViewById(R.id.tv_progress);
            mProgress = itemView.findViewById(R.id.mProgress);
        }
    };

    @Override
    protected void onDestroy() {
        stationDownLoadManager.close();
        super.onDestroy();
        Log.d("onDestroy","------------页面关闭了-<");
    }
}