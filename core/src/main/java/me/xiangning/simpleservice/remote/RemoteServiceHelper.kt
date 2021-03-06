package me.xiangning.simpleservice.remote

import android.os.IBinder
import me.xiangning.simpleservice.IRemoteServiceProxy
import me.xiangning.simpleservice.ServiceManager
import me.xiangning.simpleservice.SimpleService
import me.xiangning.simpleservice.SimpleServiceConstants
import me.xiangning.simpleservice.exception.RemoteServiceException
import me.xiangning.simpleservice.methoderror.DefaultValueMethodErrorHandler
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by xiangning on 2021/8/1.
 */
internal object RemoteServiceHelper {

    private val remotes by lazy { WeakHashMap<Any, WeakReference<IBinder>>() }
    private val proxies by lazy { WeakHashMap<IBinder, WeakReference<Any>>() }

    private val methodErrorHandlers = mutableMapOf<Class<*>, IMethodErrorHandler>()
    private val DEFAULT_ERROR_HANDLER = DefaultValueMethodErrorHandler()

    fun getServiceRemote(cls: Class<*>, service: Any): IBinder {
        synchronized(remotes) {
            val binder = remotes[service]?.get()
            if (binder != null) {
                return binder
            }

            return createServiceRemote(cls, service).also {
                remotes[service] = WeakReference(it)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getServiceRemoteProxy(cls: Class<T>, service: IBinder): T {
        synchronized(proxies) {
            return (proxies[service]?.get() ?: createServiceRemoteProxy(cls, service).also {
                proxies[service] = WeakReference(it)
            }) as T
        }
    }

    /**
     * 更新远程代理的binder
     */
    fun updateServiceRemoteProxy(proxy: IRemoteServiceProxy) {
        synchronized(proxies) {
            proxies[proxy.getRemoteInterface().asBinder()] = WeakReference(proxy)
        }
    }

    fun registerMethodErrorHandler(cls: Class<*>, handler: IMethodErrorHandler) {
        methodErrorHandlers[cls] = handler
    }

    fun getMethodErrorHandler(cls: Class<*>): IMethodErrorHandler {
        return methodErrorHandlers[cls] ?: DEFAULT_ERROR_HANDLER
    }

    private fun createServiceRemote(cls: Class<*>, service: Any): IBinder {
        val remoteCls = Class.forName(cls.name + SimpleServiceConstants.REMOTE_SUFFIX)
        if (!IBinder::class.java.isAssignableFrom(remoteCls)) {
            throw RuntimeException("${cls.name}Remote is not subclass of IBinder")
        }
        val constructor = remoteCls.getConstructor(ServiceManager::class.java, cls)
        return constructor.newInstance(SimpleService, service) as IBinder
    }

    private fun createServiceRemoteProxy(cls: Class<*>, service: IBinder): Any {
        val proxyCls = Class.forName(cls.name + SimpleServiceConstants.REMOTE_PROXY_SUFFIX)
        if (!IRemoteServiceProxy::class.java.isAssignableFrom(proxyCls)
            || !cls.isAssignableFrom(proxyCls)
        ) {
            throw RemoteServiceException("${cls.name}RemoteProxy is not valid: $cls")
        }
        val constructor = proxyCls.getConstructor(ServiceManager::class.java, IBinder::class.java)
        return constructor.newInstance(SimpleService, service)
    }
}