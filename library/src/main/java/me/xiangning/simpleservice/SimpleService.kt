package me.xiangning.simpleservice

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import me.xiangning.simpleservice.exception.RemoteServiceException
import me.xiangning.simpleservice.log.SimpleServiceLog
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler
import me.xiangning.simpleservice.remote.RemoteService
import me.xiangning.simpleservice.remote.RemoteServiceHelper
import me.xiangning.simpleservice.remote.RemoteServiceManager

/**
 * Created by xiangning on 2021/8/1.
 */
@SuppressLint("StaticFieldLeak")
object SimpleService : ServiceManager {

    private const val TAG = "SimpleService"

    private val serviceMap = mutableMapOf<Class<*>, Any>()

    private var appContext: Context? = null

    @Volatile
    private var remoteServiceManager: RemoteServiceManager? = null

    enum class RemoteServiceState {
        UNINIT,
        INITIALING,
        READY,
        DISCONNECT
    }

    @Volatile
    private var remoteServiceState = RemoteServiceState.UNINIT

    private val delayRemoteServiceCallbacks by lazy { mutableListOf<RemoteConnectCallback>() }

    @Synchronized
    fun initRemoteService(context: Context) {
        if (remoteServiceManager != null
            || remoteServiceState == RemoteServiceState.INITIALING
            || remoteServiceState == RemoteServiceState.READY
        ) {
            return
        }

        remoteServiceState = RemoteServiceState.INITIALING
        SimpleServiceLog.d(TAG) { "initRemoteService" }
        val appContext = context.applicationContext
        appContext.bindService(
            Intent(appContext, RemoteService::class.java),
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    SimpleServiceLog.d(TAG) { "initRemoteService success" }
                    val rsm = getServiceRemoteProxy(RemoteServiceManager::class.java, service)
                    synchronized(this@SimpleService) {
                        remoteServiceManager = rsm
                        remoteServiceState = RemoteServiceState.READY
                    }

                    delayRemoteServiceCallbacks.forEach { it(rsm) }
                    delayRemoteServiceCallbacks.clear()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    SimpleServiceLog.d(TAG) { "initRemoteService disconnect" }
                    remoteServiceState = RemoteServiceState.DISCONNECT
                    // initRemoteService(context)
                }
            },
            Context.BIND_AUTO_CREATE
        )
        this.appContext = appContext
    }

    private fun connectRemoteServiceManager(action: RemoteConnectCallback) {
        val rsm: RemoteServiceManager?
        synchronized(this) {
            rsm = remoteServiceManager
            // 未调用初始化方法，抛出异常
            if (rsm == null) {
                when (remoteServiceState) {
                    RemoteServiceState.UNINIT -> throw RemoteServiceException("you should invoke initRemoteService before all remote operation!!!")
                    RemoteServiceState.INITIALING -> {
                        delayRemoteServiceCallbacks.add(action)
                        return
                    }
                    RemoteServiceState.DISCONNECT -> {
                        appContext?.let {
                            initRemoteService(it)
                            delayRemoteServiceCallbacks.add(action)
                            return
                        }
                    }
                    else -> {
                    }
                }

                throw RemoteServiceException("illegal state, rsm is null and state = $remoteServiceState")
            }
        }

        if (rsm != null) {
            action(rsm)
        }
    }

    override fun <T : Any> publishService(cls: Class<T>, service: T): Boolean {
        serviceMap[cls] = service
        return true
    }

    override fun <T : Any> getService(cls: Class<T>): T? {
        return serviceMap[cls] as? T
    }

    @JvmOverloads
    override fun <T : Any> publishRemoteService(
        cls: Class<T>,
        service: T,
        onRemoteServicePublish: OnRemoteServicePublish?
    ) {
        connectRemoteServiceManager { rsm ->
            try {
                rsm.publishService(cls.name, getServiceRemote(cls, service))
                onRemoteServicePublish?.onPublishResult(null)
            } catch (e: Exception) {
                SimpleServiceLog.e(TAG, e) { "publishRemoteService: ${e.message}" }
                onRemoteServicePublish?.onPublishResult(e)
            }
        }
    }

    override fun <T : Any> bindRemoteService(
        cls: Class<T>,
        onRemoteServiceBind: OnRemoteServiceBind<T>
    ) {
        connectRemoteServiceManager { rsm ->
            val rs = rsm.getService(cls.name)
            if (rs != null) {
                try {
                    onRemoteServiceBind.onBindSuccess(getServiceRemoteProxy(cls, rs))
                } catch (e: Exception) {
                    onRemoteServiceBind.onBindFailed(e)
                }
            } else {
                onRemoteServiceBind.onBindFailed(RemoteServiceException("please make sure remote service has been published: ${cls.name}"))
            }
        }

    }

    override fun <T : Any, R : IBinder> getServiceRemote(cls: Class<T>, service: T): R {
        try {
            return RemoteServiceHelper.getServiceRemote(cls, service) as R
        } catch (e: Exception) {
            throw RemoteServiceException("get service remote failed")
        }
    }

    override fun <T> getServiceRemoteProxy(cls: Class<T>, service: IBinder): T {
        try {
            return RemoteServiceHelper.getServiceRemoteProxy(cls, service)
        } catch (e: Exception) {
            throw RemoteServiceException("get service remote proxy failed")
        }
    }

    override fun registerMethodErrorHandler(cls: Class<*>, handler: IMethodErrorHandler) {
        RemoteServiceHelper.registerMethodErrorHandler(cls, handler)
    }

    override fun getMethodErrorHandler(cls: Class<*>): IMethodErrorHandler {
        return RemoteServiceHelper.getMethodErrorHandler(cls)
    }

    fun setLogDebug(debugable: Boolean) {
        SimpleServiceLog.debugable = debugable
    }
}

private typealias RemoteConnectCallback = (RemoteServiceManager) -> Unit
