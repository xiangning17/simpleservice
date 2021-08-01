package me.xiangning.simpleservice.remote

import android.os.IBinder
import android.os.IInterface
import me.xiangning.simpleservice.ServiceManager
import me.xiangning.simpleservice.SimpleService
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler
import java.util.*

/**
 * Created by xiangning on 2021/8/1.
 */
object RemoteServiceHelper {

    private val remotes = mutableMapOf<Class<*>, WeakHashMap<IBinder, Any>>()
    private val proxies = WeakHashMap<IBinder, Any>()

    fun getServiceRemote(cls: Class<*>, service: Any): IBinder {
        return remotes.getOrPut(cls) { WeakHashMap(1) }.let { map ->
            val exist = map.firstNotNullOfOrNull {
                if (it.value === service) it.key else null
            }

            return@let exist ?: createServiceRemote(cls, service).also {
                map[it] = service
            }
        }
    }

    fun <T> getServiceRemoteProxy(cls: Class<T>, service: IInterface): T {
        return proxies.getOrPut(service.asBinder()) {
            createServiceRemoteProxy(cls, service)
        } as T
    }

    fun registerMethodErrorHandler(cls: Class<*>, handler: IMethodErrorHandler) {
        TODO("Not yet implemented")
    }

    fun getMethodErrorHandler(cls: Class<*>): IMethodErrorHandler {
        TODO("Not yet implemented")
    }

    private fun createServiceRemote(cls: Class<*>, service: Any): IBinder {
        val remoteCls = Class.forName(cls.name + "Remote")
        if (!IBinder::class.java.isAssignableFrom(remoteCls)) {
            throw RuntimeException("${cls.name}Remote is not subclass of IBinder")
        }
        val constructor = remoteCls.getConstructor(ServiceManager::class.java, cls)
        return constructor.newInstance(SimpleService, service) as IBinder
    }

    private fun createServiceRemoteProxy(cls: Class<*>, service: IInterface): Any {
        val proxyCls = Class.forName(cls.name + "RemoteProxy")
        if (!cls.isAssignableFrom(proxyCls)) {
            throw RuntimeException("${cls.name}RemoteProxy is not subclass of $cls")
        }
        val constructor = proxyCls.getConstructor(ServiceManager::class.java, IBinder::class.java)
        return constructor.newInstance(SimpleService, service.asBinder())
    }
}