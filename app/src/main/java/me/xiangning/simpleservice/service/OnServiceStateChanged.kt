package me.xiangning.simpleservice.service

import android.os.IBinder
import me.xiangning.annotation.Aidl

/**
 * Created by xiangning on 2021/7/3.
 */
@Aidl
interface OnServiceStateChanged {

    fun onServicePublish(name: String, service: IBinder)

    fun onServiceStop(name: String)

}