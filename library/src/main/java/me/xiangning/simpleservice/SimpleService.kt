package me.xiangning.simpleservice

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.IInterface
import android.os.Looper
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import me.xiangning.simpleservice.exception.RemoteServiceException
import me.xiangning.simpleservice.log.SimpleServiceLog
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler
import me.xiangning.simpleservice.remote.RemoteServiceBridge
import me.xiangning.simpleservice.remote.RemoteServiceHelper
import me.xiangning.simpleservice.remote.RemoteServiceManager
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Created by xiangning on 2021/8/1.
 */
@SuppressLint("StaticFieldLeak")
object SimpleService : ServiceManager {

    private const val TAG = "SimpleService"

    private val serviceMap = ConcurrentHashMap<Class<*>, Any>()

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
            Intent(appContext, RemoteServiceBridge::class.java),
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    SimpleServiceLog.d(TAG) { "initRemoteService success" }
                    onRemoteServiceConnected(service)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    SimpleServiceLog.d(TAG) { "initRemoteService disconnect" }
                    synchronized(this@SimpleService) {
                        remoteServiceState = RemoteServiceState.DISCONNECT
                    }
                    // initRemoteService(context)
                }
            },
            Context.BIND_AUTO_CREATE
        )
        this.appContext = appContext
    }

    private fun onRemoteServiceConnected(service: IBinder) {
        val rsm = getServiceRemoteProxy(RemoteServiceManager::class.java, service)
        synchronized(this@SimpleService) {
            remoteServiceManager = rsm
            remoteServiceState = RemoteServiceState.READY
        }

        delayRemoteServiceCallbacks.forEach { it(rsm) }
        delayRemoteServiceCallbacks.clear()

        rsm.registerServiceStateListener { name, updated ->
            val cls = try {
                Class.forName(name)
            } catch (e: Exception) {
                return@registerServiceStateListener
            }

            (serviceMap[cls] as? IRemoteServiceProxy)?.let { proxy ->
                SimpleServiceLog.d(TAG) { "update remote service: $name" }
                proxy.setBinder(updated)
            }
        }
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
        SimpleServiceLog.d(TAG) { "publish service: [${cls.name}] $service" }
        return true
    }

    override fun <T : Any> getService(cls: Class<T>): T? {
        return serviceMap[cls] as? T
    }

    override fun <T : Any> publishRemoteService(
        cls: Class<T>,
        service: T,
        onRemoteServicePublish: OnRemoteServicePublish?
    ) {
        connectRemoteServiceManager { rsm ->
            try {
                rsm.publishService(cls.name, getServiceRemote(cls, service))
                SimpleServiceLog.d(TAG) { "publish remote service: [${cls.name}] $service" }
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
        getRemoteServiceFromLocal(cls)?.let {
            onRemoteServiceBind.onBindSuccess(it)
            return
        }

        connectRemoteServiceManager { rsm ->
            val rs = rsm.getService(cls.name)
            if (rs != null) {
                try {
                    val proxy = getServiceRemoteProxy(cls, rs)
                    serviceMap[cls] = proxy
                    SimpleServiceLog.d(TAG) { "bind remote service: [${cls.name}] $onRemoteServiceBind" }
                    onRemoteServiceBind.onBindSuccess(proxy)
                } catch (e: Exception) {
                    onRemoteServiceBind.onBindFailed(e)
                }
            } else {
                onRemoteServiceBind.onBindFailed(RemoteServiceException("please make sure remote service has been published: ${cls.name}"))
            }
        }

    }

    private fun <T : Any> getRemoteServiceFromLocal(cls: Class<T>): T? {
        val cached = serviceMap[cls]
        if (cls.isInstance(cached)) {
            return cached as T
        }

        return null
    }

    suspend fun <T : Any> getRemoteService(cls: Class<T>): T {
        return suspendCoroutine {
            bindRemoteService(cls, object : OnRemoteServiceBind<T> {
                override fun onBindSuccess(service: T) {
                    it.resume(service)
                }

                override fun onBindFailed(error: Throwable?) {
                    it.resumeWithException(error!!)
                }

            })
        }
    }

    @JvmOverloads
    fun <T : Any> getRemoteServiceWait(cls: Class<T>, awaitTime: Long = Long.MAX_VALUE): T? {
        getRemoteServiceFromLocal(cls)?.let {
            return it
        }

        // rsm not ready, and current thread is main.
        // rsm would never be ready if main thread block, so just return null.
        // when awaitTime is max it would cause dead lock evenly.
        if (remoteServiceState != RemoteServiceState.READY && Looper.myLooper() == Looper.getMainLooper()) {
            SimpleServiceLog.w(TAG) { "RemoteService not ready, and current thread is main, just return null." }
            return null
        }

        return runBlocking {
            withTimeoutOrNull(awaitTime) {
                getRemoteService(cls)
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

    override fun <T : IInterface> getServiceRemoteInterface(proxy: Any): T {
        try {
            return (proxy as IRemoteServiceProxy).getRemoteInterface() as T
        } catch (e: Exception) {
            throw RemoteServiceException(
                "make sure the parameter is from 'bindRemoteService'," +
                        " and the return type is correct", e
            )
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
