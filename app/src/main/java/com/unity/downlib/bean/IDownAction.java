package com.unity.downlib.bean;

/**
 * 对服务的操作指令
 */
public interface IDownAction {
    //不做任何事情 只开服务
    String DOWN_SERVICE_NOTHING = "com.unity.downlib.service.NOTHING";
    //初始化下载服务 上次有下载就下载
    String DOWN_INIT = "com.unity.downlib.service.DOWN_INIT";
    //恢复下载服务
    String DOWN_SERVICE_RESUME_DOWN = "com.unity.downlib.service.DOWN_SERVICE_resume_down";
    //暂停
    String DOWN_SERVICE_PAUSE = "com.unity.downlib.service.DOWN_SERVICE_PAUSE";
    //添加一个任务
    String DOWN_ADD_TASK = "com.unity.downlib.service.DOWN_SERVICE_addTask";
    //移除一个任务
    String DOWN_REMOVE_TASK = "com.unity.downlib.service.DOWN_SERVICE_removeTask";

    //应用关闭了
    String DOWN_STOP = "com.unity.downlib.service.DOWN_SERVICE_DOWN_STOP";
}
