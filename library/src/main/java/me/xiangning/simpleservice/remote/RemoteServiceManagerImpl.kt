package me.xiangning.simpleservice.remote

import android.os.IBinder

/**
 * Created by xiangning on 2021/8/1.
 */
class RemoteServiceManagerImpl : RemoteServiceManager {

    private val serviceMap = mutableMapOf<String, IBinder>()

    override fun publishService(name: String, service: IBinder): Boolean {
        serviceMap[name] = service
        return true
    }

    override fun getService(name: String): IBinder? {
        return serviceMap[name]
    }

    override fun registerServiceStateListener(listener: OnRemoteServiceStateChanged) {
        TODO("Not yet implemented")
    }

    override fun unregisterServiceStateListener(listener: OnRemoteServiceStateChanged) {
        TODO("Not yet implemented")
    }
}