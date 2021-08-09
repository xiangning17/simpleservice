package me.xiangning.simpleservice.remote

import android.app.Service
import android.content.Intent
import android.os.IBinder
import me.xiangning.simpleservice.SimpleService
import me.xiangning.simpleservice.log.SimpleServiceLog

class RemoteServiceBridge : Service() {

    companion object {
        private const val TAG = "RemoteServiceBridge"
    }

    override fun onBind(intent: Intent): IBinder {
        SimpleServiceLog.d(TAG) { "onBind start" }
        val binder =
            SimpleService.getServiceRemote<RemoteServiceManager, RemoteServiceManagerRemote>(
                RemoteServiceManager::class.java,
                RemoteServiceManagerImpl
            )

        SimpleServiceLog.d(TAG) { "onBind end, $binder" }
        return binder
    }
}