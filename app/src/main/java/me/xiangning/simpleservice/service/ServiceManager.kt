package me.xiangning.simpleservice.service

import android.os.IBinder
import me.xiangning.annotation.Aidl

/**
 * Created by xiangning on 2021/7/3.
 */
@Aidl
interface ServiceManager {
    fun publishService(name: String, service: IBinder): Boolean
    fun stopService(name: String)

    fun getService(name: String): IBinder?

    fun registerServiceStateListener(listener: OnServiceStateChanged)
}