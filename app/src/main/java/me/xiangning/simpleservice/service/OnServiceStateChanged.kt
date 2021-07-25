package me.xiangning.simpleservice.service

import android.os.IBinder
import me.xiangning.simpleservice.annotation.ParcelableAidl

/**
 * Created by xiangning on 2021/7/3.
 */
@ParcelableAidl
interface OnServiceStateChanged {

    fun onServicePublish(name: String, service: IBinder)

    fun onServiceStop(name: String)

}