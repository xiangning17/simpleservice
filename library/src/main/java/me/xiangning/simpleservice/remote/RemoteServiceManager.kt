package me.xiangning.simpleservice.remote

import android.os.IBinder
import me.xiangning.simpleservice.annotation.RemoteService

/**
 * Created by xiangning on 2021/7/31.
 */
@RemoteService
interface RemoteServiceManager {
    fun publishService(name: String, service: IBinder): Boolean
    fun getService(name: String): IBinder?

    fun registerServiceStateListener(listener: OnRemoteServiceStateChanged)
    fun unregisterServiceStateListener(listener: OnRemoteServiceStateChanged)
}