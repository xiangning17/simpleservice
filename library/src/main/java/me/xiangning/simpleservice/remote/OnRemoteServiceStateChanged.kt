package me.xiangning.simpleservice.remote

import android.os.IBinder
import me.xiangning.simpleservice.annotation.RemoteService

/**
 * Created by xiangning on 2021/7/3.
 */
@RemoteService
interface OnRemoteServiceStateChanged {

    fun onServicePublish(name: String, service: IBinder)

}