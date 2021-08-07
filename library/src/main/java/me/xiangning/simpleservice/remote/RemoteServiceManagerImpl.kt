package me.xiangning.simpleservice.remote

import android.os.IBinder
import android.os.RemoteCallbackList
import me.xiangning.simpleservice.SimpleService
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by xiangning on 2021/8/1.
 */
object RemoteServiceManagerImpl : RemoteServiceManager {

    private val serviceMap = ConcurrentHashMap<String, IBinder>()
    private val remoteCallbackList = RemoteCallbackList<OnRemoteServiceStateChangedBinder>()

    override fun publishService(name: String, service: IBinder): Boolean {
        serviceMap[name] = service
        notifyListener { listener -> listener.onServicePublish(name, service) }
        return true
    }

    override fun getService(name: String): IBinder? {
        return serviceMap[name]
    }

    override fun registerServiceStateListener(listener: OnRemoteServiceStateChanged) {
        remoteCallbackList.register(SimpleService.getServiceRemoteInterface(listener))
    }

    override fun unregisterServiceStateListener(listener: OnRemoteServiceStateChanged) {
        remoteCallbackList.unregister(SimpleService.getServiceRemoteInterface(listener))
    }

    private inline fun notifyListener(action: (OnRemoteServiceStateChanged) -> Unit) {
        val count = remoteCallbackList.beginBroadcast()
        for (i in 0 until count) {
            action(
                SimpleService.getServiceRemoteProxy(
                    OnRemoteServiceStateChanged::class.java,
                    remoteCallbackList.getBroadcastItem(i).asBinder()
                )
            )
        }
        remoteCallbackList.finishBroadcast()
    }
}