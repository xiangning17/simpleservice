package me.xiangning.simpleservice.remote

import android.app.Service
import android.content.Intent
import android.os.IBinder
import me.xiangning.simpleservice.SimpleService

class RemoteService : Service() {

    companion object {
        val remoteServiceManager by lazy { RemoteServiceManagerImpl() }
    }

    override fun onBind(intent: Intent): IBinder {
        return SimpleService.getServiceRemote<RemoteServiceManager, RemoteServiceManagerRemote>(
            RemoteServiceManager::class.java,
            remoteServiceManager
        )
    }
}