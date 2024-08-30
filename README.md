# 一个大文件多线程后台service下载,支持断点续传的案例，站在巨人的肩膀上。

思路：

#### 1.对文件进行分段

#### 2.分段之后进度下载进度保存

#### 3.下载完毕后返回保存的file

演示：
![](https://github.com/liouyang/AndroidThreadDown/tree/main/video/demo.gif)
核心代码：

```
public class StationDownLoadController {

    public static final String TAG = "StationDownLoad";


    /**
     * 下载线程数量
     */
    public int mThreadCount = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 线程池
     */
    public static ExecutorService sExe = null;

    /**
     * 定时器
     */
    private ScheduledExecutorService scheduler = null;

    /**
     * 是否正在下载
     */
    public boolean isDownLoading;//是否正在下载

    /**
     * 该文件所有线程的集合
     */
    private List<DownLoadThread> mDownLoadThreads;

    //已下载的长度：共享变量 volatile和Atomic进行同步
    private volatile AtomicLong mLoadedLen = new AtomicLong();

    /**
     * 是否是暂停下载
     */
    public volatile boolean isPaused = false;


    public DownLoadBean currentBean = null;
    private NetStateReceiver netStateReceiver;

    private DownloadActionListener mDownloadActionListener = null;

    private Handler mHandler = new android.os.Handler(Looper.getMainLooper());

    private static volatile StationDownLoadController instance;

    public static StationDownLoadController getInstance() {
        if (instance == null) {
            synchronized (StationDownLoadController.class) {
                if (instance == null) {
                    instance = new StationDownLoadController();
                }
            }
        }
        return instance;
    }

    public void onDestroy() {
        System.out.println(TAG + "  控制器被销毁了");
        //暂停保存数据
        if (mDownLoadThreads != null) {
            synchronized (mDownLoadThreads) {
                for (DownLoadThread downLoadThread : mDownLoadThreads) {
                    downLoadThread.isDownLoading = false;
                }
                mDownLoadThreads.clear();
                isDownLoading = false;
            }
        }

        stopProgressUpdate();
        if (netStateReceiver != null) {
            GlobalConstans.context.unregisterReceiver(netStateReceiver);
            netStateReceiver = null;
        }

    }


    private StationDownLoadController() {

    }

    public void init() {
        Log.d(TAG, "初始化参数=线程数=======》" + mThreadCount);
        isPaused = false;
        sExe = Executors.newFixedThreadPool(mThreadCount);
        netStateReceiver = new NetStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        GlobalConstans.context.registerReceiver(netStateReceiver, intentFilter);

    }

    class NetStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.getAction()) {
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
                Boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
                if (isConnected) {
                    //网络连接
                    Log.d("网络", "===网络连接=====<>");
                    //所有的都完成了

                    if (mDownloadActionListener != null) {
                        mHandler.post(() -> mDownloadActionListener.networkConnection());
                    }
                } else {
                    Log.d("网络", "===网络断开=====<>");
                    if (mDownloadActionListener != null) {
                        mHandler.post(() -> mDownloadActionListener.networkDisconnection());
                    }
                    pause();
                }
            }
        }
    }


    public List<DownLoadBean> getAllDownTask() {
        return ContentProviderHelper.queryAllDate(GlobalConstans.context);
    }

    public void initLastDownTask() {
        String selection = "downState = ?";
        String[] selectionArgs = {TaskState.Pause + ""};
        //查询又没有暂停的任务
        DownLoadBean downLoadBean = ContentProviderHelper.queryTable(GlobalConstans.context, selection, selectionArgs);
        if (downLoadBean != null) {
            isPaused = true;
            //初始化进度总值的大小
            mLoadedLen = new AtomicLong(downLoadBean.downLoadedSize);
        } else {
            //查询又没有下载的的任务
            String selection2 = "downState = ?";
            String[] selectionArgs2 = {TaskState.Downloading + ""};
            //有下载的中的把下载继续
            DownLoadBean downLoadBean2 = ContentProviderHelper.queryTable(GlobalConstans.context, selection2, selectionArgs2);

            List<ThreadBean> threadBeans = ContentProviderHelper.queryAllThreadDate(GlobalConstans.context);

            if (threadBeans != null && threadBeans.size() > 0) {
                long size = 0;
                Log.d("初始进度", "--------========片" + size);
                for (int i = 0; i < threadBeans.size(); i++) {
                    ThreadBean threadBean = threadBeans.get(i);
                    size += threadBean.downLength;
                }
                mLoadedLen = new AtomicLong(size);
                createTask(downLoadBean2);
            }
        }

    }


    public void removeBeanTask(DownLoadBean bean) {
        //有下载的中的把下载展厅
        int i = ContentProviderHelper.deleterDownLoadStateTable(GlobalConstans.context, bean.fileUrl);
        if (i != 0) {
            //移除成功
            if (mDownloadActionListener != null) {
                mHandler.post(() -> mDownloadActionListener.removeSuccess(bean));
            }
        }
        ContentProviderHelper.deleterThreadTable(GlobalConstans.context, bean.fileUrl);
    }


    // 暂停下载
    public void pause() {
        if (isPaused) {
            return;
        }
        isPaused = true;
        String selection = "downState = ?";
        String[] selectionArgs = {TaskState.Downloading + ""};
        //有下载的中的把下载展厅
        DownLoadBean downLoadBean = ContentProviderHelper.queryTable(GlobalConstans.context, selection, selectionArgs);
        if (downLoadBean != null) {
            int i = ContentProviderHelper.updateDownLoadStateTable(GlobalConstans.context, downLoadBean.downState, downLoadBean.name, TaskState.Pause);
            if (i > 0) {
                //更新
                if (mDownloadActionListener != null) {
                    mHandler.post(() -> mDownloadActionListener.pauseOrResumeDownloadSuccess(downLoadBean));
                }
            }
        }
        if (mDownLoadThreads == null) {
            return;
        }
        for (DownLoadThread downLoadThread : mDownLoadThreads) {
            downLoadThread.isDownLoading = false;
            isDownLoading = false;

        }
        stopProgressUpdate();
    }


    // 恢复下载
    public void resume() {
        if (!isPaused) {
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(GlobalConstans.context)) {
            //无网络
            return;
        }
        isPaused = false;
        String selection = "downState = ?";
        String[] selectionArgs = {TaskState.Pause + ""};
        //有下载的中的把下载继续
        DownLoadBean downLoadBean = ContentProviderHelper.queryTable(GlobalConstans.context, selection, selectionArgs);
        if (downLoadBean != null) {
            int i = ContentProviderHelper.updateDownLoadStateTable(GlobalConstans.context, downLoadBean.downState, downLoadBean.name, TaskState.Downloading);
            if (i > 0) {
                //更新
                if (mDownloadActionListener != null) {
                    mHandler.post(() -> mDownloadActionListener.pauseOrResumeDownloadSuccess(downLoadBean));
                }
            }
            createTask(downLoadBean);
        }
    }

    public void addDownLoadTask(DownLoadBean bean) {
        if (!NetworkUtils.isNetworkAvailable(GlobalConstans.context)) {
            //无网络
            return;
        }
        // 定义查询条件
        String selection = "downState = ?";
        String[] selectionArgs = {TaskState.Pause + ""};
        //查询有没有前置的下载如果有 加入等待队列 没有加入下载任务
        DownLoadBean downLoadBean = ContentProviderHelper.queryTable(GlobalConstans.context, selection, selectionArgs);


        // 定义查询条件
        String selection2 = "downState = ?";
        String[] selectionArgs2 = {TaskState.Downloading + ""};
        //查询有没有前置的下载如果有 加入等待队列 没有加入下载任务
        DownLoadBean downLoadBean2 = ContentProviderHelper.queryTable(GlobalConstans.context, selection2, selectionArgs2);


        if (downLoadBean != null) {
            //有暂停任务
            bean.downState = TaskState.Await;
            Uri uri = ContentProviderHelper.insetDownLoadTable(GlobalConstans.context, bean);
            if (uri != null) {
                //加入成功
                if (mDownloadActionListener != null) {
                    mHandler.post(() -> mDownloadActionListener.addSuccess(bean));
                }
            }
        } else {
            //有无下载任务
            if (downLoadBean2 != null) {
                bean.downState = TaskState.Await;
                Uri uri = ContentProviderHelper.insetDownLoadTable(GlobalConstans.context, bean);
                if (uri != null) {
                    //加入成功
                    if (mDownloadActionListener != null) {
                        mHandler.post(() -> mDownloadActionListener.addSuccess(bean));
                    }
                }
            } else {
                bean.downState = TaskState.Downloading;
                Uri uri = ContentProviderHelper.insetDownLoadTable(GlobalConstans.context, bean);
                if (uri != null) {
                    //加入成功
                    if (mDownloadActionListener != null) {
                        mHandler.post(() -> mDownloadActionListener.addSuccess(bean));
                    }
                }
                createTask(bean);
            }

        }
    }


    void createTask(DownLoadBean bean) {
        currentBean = bean;
        if (!NetworkUtils.isNetworkAvailable(GlobalConstans.context)) {
            //无网络
            return;
        }
        scheduler = Executors.newScheduledThreadPool(1);
        mDownLoadThreads = new ArrayList<>();
        startWakeLock();

        if (bean.localPath == null) {
            // 下载文件的名称
            String fileName = bean.fileUrl.substring(bean.fileUrl.lastIndexOf("/"));
            // 下载文件存放的目录
            File videoF = new File(GlobalConstans.context.getExternalFilesDir(null), "Video");
            if (!videoF.exists()) {
                videoF.mkdirs();
            }
            String directory = videoF.toString();
            //  创建文件
            File file = new File(directory + File.separator + fileName);
            bean.localPath = file.toString();
        }
        //从数据获取线程信息
        List<ThreadBean> threads = ContentProviderHelper.queryAllThreadDate(GlobalConstans.context);
        if (threads.size() == 0) {//如果没有线程信息，就新建线程信息
            //------获取每个进程下载长度
            long len = bean.fileSize / mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                //创建threadCount个线程信息
                ThreadBean threadBean = null;
                long start = len * i;
                long end = (i == mThreadCount - 1) ? bean.fileSize - 1 : (start + len - 1);
                threadBean = new ThreadBean(i, "threadName_" + i, bean.fileUrl, bean.localPath, start, end, 0, "");
                //创建后添加到线程集合中
                threads.add(threadBean);
                //2.如果数据库没有此下载线程的信息，则向数据库插入该线程信息
                ContentProviderHelper.insertThreadTable(GlobalConstans.context, threadBean);
            }
        }

        //启动多个线程
        for (ThreadBean info : threads) {
            DownLoadThread thread = new DownLoadThread(info);//创建下载线程
            sExe.execute(thread);//开始线程
            thread.isDownLoading = true;
            isDownLoading = true;
            mDownLoadThreads.add(thread);//开始下载时将该线程加入集合
        }

        // 每500毫秒更新一次进度
        // 可以在这里添加更多的逻辑，比如更新UI等
        scheduler.scheduleAtFixedRate(this::updateSchedule, 0, 200, TimeUnit.MILLISECONDS);


    }


    private PowerManager mPowerManager = null;
    private PowerManager.WakeLock wakeLock;

    @SuppressLint("InvalidWakeLockTag")
    public void startWakeLock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 对于API级别23及以上，使用类型安全的方法获取PowerManager
            this.mPowerManager = (PowerManager) GlobalConstans.context.getSystemService(PowerManager.class);
        } else {
            // 对于API级别低于23的版本，使用传统的字符串常量方式获取PowerManager
            this.mPowerManager = (PowerManager) GlobalConstans.context.getSystemService(Context.POWER_SERVICE);
        }
        this.wakeLock = this.mPowerManager.newWakeLock(1, "eye_Wakelock_rkd_allow");
        if (this.wakeLock != null && !this.wakeLock.isHeld()) {
            Log.d("WakeLockService", "加锁");
            this.wakeLock.acquire();
        }

    }

    public void stopWakeLocak() {
        Log.d("WakeLockService", "释放锁1");
        if (this.wakeLock != null && this.wakeLock.isHeld()) {
            Log.d("WakeLockService", "释放锁");
            this.wakeLock.release();
        }

    }


    public void stopProgressUpdate() {
        // 停止进度更新
        if (scheduler == null) {
            return;
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow(); // 强制关闭
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }


    public class DownLoadThread extends Thread {
        private ThreadBean mThreadBean;//下载线程的信息
        public volatile boolean isDownLoading;//是否在下载
        File file = null;

        public DownLoadThread(ThreadBean info) {
            mThreadBean = info;
        }


        @Override
        public void run() {
            System.out.println("线程DownLoadThread-->" + mThreadBean.toString());
            super.run();
            if (mThreadBean == null) { // 下载线程的信息为空,直接返回
                return;
            }

            // 文件下载地址
            String downloadUrl = mThreadBean.url;
            // 下载文件的名称
//            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            // 下载文件存放的目录
//            File videoF =new File(mThreadBean.localPath);
//            if (!videoF.exists()) {
//                videoF.mkdirs();
//            }
//            String directory = videoF.toString();
            // 创建文件
            file = new File(mThreadBean.localPath);

            // 设置下载位置
            long start = mThreadBean.startLength + mThreadBean.downLength; // 开始位置
            long end = mThreadBean.endLength; // 结束位置
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            RandomAccessFile randomAccessFile = null;
            try {
                // 创建 URL 和 HttpURLConnection
                URL url = new URL(downloadUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000); // 10秒连接超时
                connection.setReadTimeout(10000); // 10秒读取超时
                // 设置请求头信息
                connection.setRequestProperty("Range", "bytes=" + start + "-" + end);  // 设置下载的区间
                connection.setRequestMethod("GET");
                connection.connect();

                // 检查响应码
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) { // 206 Partial Content
                    inputStream = connection.getInputStream();
                    randomAccessFile = new RandomAccessFile(file, "rw");
                    randomAccessFile.seek(start); // 定位到已下载的地方

                    byte[] buffer = new byte[1024 * 8];
                    int len;

                    while ((len = inputStream.read(buffer)) != -1) {
                        // 写入文件
                        randomAccessFile.write(buffer, 0, len);
                        mLoadedLen.addAndGet(len); // 更新已下载总进度
                        mThreadBean.downLength += len; // 更新当前线程下载进度

                        // 暂停时保存下载进度
                        if (!this.isDownLoading) {

                            ContentProviderHelper.updateThreadTable(GlobalConstans.context, mThreadBean.threadName,
                                    mThreadBean.downLength);
                            ContentProviderHelper.updateDownLoadLengthTableByUrl(GlobalConstans.context, mThreadBean.url,
                                    mLoadedLen.get());
                            return;
                        }
                    }
                    connection.disconnect();
                    inputStream.close();
                    // 下载完成
                    isDownLoading = false;
                    checkIsAllOK(); // 检查所有线程是否完成下载
                    System.out.println("Download completed.");


                } else {
                    saveData();
                }
            } catch (Exception e) {
                e.printStackTrace();
                saveData();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing file: " + e.getMessage());
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                        Log.e(TAG, "Error closing inputStream: " + ex.getMessage());

                    }
                }
            }
        }

        private void saveData() {

            if (mDownloadActionListener != null) {
                mHandler.post(() -> mDownloadActionListener.onFail(currentBean));
            }

            ContentProviderHelper.updateThreadTable(GlobalConstans.context, mThreadBean.threadName,
                    mThreadBean.downLength);
            ContentProviderHelper.updateDownLoadLengthTableByUrl(GlobalConstans.context, mThreadBean.url,
                    mLoadedLen.get());
        }


        /**
         * 检查是否所有线程都已经完成了
         */
        private synchronized void checkIsAllOK() {
            boolean allFinished = true;
            for (DownLoadThread downLoadThread : mDownLoadThreads) {
                if (downLoadThread.isDownLoading) {
                    allFinished = false;
                    break;
                }
            }
            if (allFinished) {

                String selection = "fileUrl = ?";
                String[] selectionArgs = {mThreadBean.url};
                //有下载的中的把下载展厅
                DownLoadBean downLoadBean = ContentProviderHelper.queryTable(GlobalConstans.context, selection, selectionArgs);
                System.out.println("校验前的数据======》" + downLoadBean);
                if (downLoadBean != null) {
                    boolean b = MD5Checksum.compareFiles(downLoadBean.fileMd5, file);
                    if (b) {
                        System.out.println("文件校验一致");
                        updateSchedule();
                        stopProgressUpdate();

                        if (mDownloadActionListener != null) {
                            mHandler.post(() -> mDownloadActionListener.onCompleteOnlyOne(currentBean));
                        }
                        //下载完成，删除线程信息
                        ContentProviderHelper.deleterThreadTable(GlobalConstans.context, mThreadBean.url);
                        ContentProviderHelper.deleterDownLoadStateTable(GlobalConstans.context, mThreadBean.url);
                        mLoadedLen.set(0);
                        doNextDown();
                    } else {
                        System.out.println("文件校验不一致");

                        if (mDownloadActionListener != null) {
                            mHandler.post(() -> mDownloadActionListener.onFail(currentBean));
                        }
                    }
                }


            }
        }

    }


    private void updateSchedule() {
        if (currentBean != null) {
            if (mDownloadActionListener != null) {
                mHandler.post(() -> mDownloadActionListener.onProgress(currentBean.fileSize, mLoadedLen.get(), currentBean));
            }
        }
    }

    private void doNextDown() {
        List<DownLoadBean> downLoadBeans = ContentProviderHelper.queryAllDate(GlobalConstans.context);

        if (downLoadBeans.size() > 0) {
            DownLoadBean downLoadBean = downLoadBeans.get(0);
            ContentProviderHelper.updateDownLoadStateTable(GlobalConstans.context, downLoadBean.downState, downLoadBean.name, TaskState.Downloading);
            downLoadBean.downState = TaskState.Downloading;
            createTask(downLoadBean);
        } else {
            //所有的都完成了
            stopWakeLocak();
            if (mDownloadActionListener != null) {
                mHandler.post(() -> mDownloadActionListener.onCompleteAll());
            }
        }


    }


    public void registerDownloadActionListener(DownloadActionListener mDownloadProgressListener) {
        this.mDownloadActionListener = mDownloadProgressListener;

    }

    /**
     * 动作的监听
     */
    public interface DownloadActionListener {
        void onProgress(long totalBytes, long downloadedBytes, DownLoadBean downLoadBean);

        void onCompleteOnlyOne(DownLoadBean downLoadBean);

        void onFail(DownLoadBean downLoadBean);

        void onCompleteAll();

        void networkDisconnection();

        void networkConnection();

        void removeSuccess(DownLoadBean downLoadBean);

        void addSuccess(DownLoadBean downLoadBean);

        void pauseOrResumeDownloadSuccess(DownLoadBean downLoadBean);
    }
}
```
