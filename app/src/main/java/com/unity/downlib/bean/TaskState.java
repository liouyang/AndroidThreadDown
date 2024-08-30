package com.unity.downlib.bean;

import android.os.Handler;

import java.util.HashMap;

public  class TaskState {
    public static final int None=0;
    public static final int Pause=1;
    public static final int Downloading=2;
    public static final int Await=3;
    public static final int Downloaded=4;
    public static final int Error=5;



    public static final HashMap<Integer,String> map=new HashMap<>();
}