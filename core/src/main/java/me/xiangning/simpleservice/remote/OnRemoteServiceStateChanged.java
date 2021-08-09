package me.xiangning.simpleservice.remote;

import android.os.IBinder;

import me.xiangning.simpleservice.annotation.RemoteService;

/**
 * Created by xiangning on 2021/7/3.
 */
@RemoteService
public interface OnRemoteServiceStateChanged {

    void onServicePublish(String name, IBinder service);

}