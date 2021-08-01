package me.xiangning.simpleservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import me.xiangning.simpleservice.exception.RemoteServiceException
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler
import me.xiangning.simpleservice.remote.RemoteService
import me.xiangning.simpleservice.remote.RemoteServiceHelper
import me.xiangning.simpleservice.remote.RemoteServiceManager

/**
 * Created by xiangning on 2021/8/1.
 */
object SimpleService : ServiceManager {

    private const val TAG = "SimpleService"

    private val serviceMap = mutableMapOf<Class<*>, Any>()

    private var remoteServiceManager: RemoteServiceManager? = null

    fun initRemoteService(context: Context) {
        val appContext = context.applicationContext
        appContext.bindService(
            Intent(appContext, RemoteService::class.java),
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    remoteServiceManager =
                        getServiceRemoteProxy(RemoteServiceManager::class.java, service)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    initRemoteService(context)
                }
            },
            Context.BIND_IMPORTANT
        )
    }

    override fun <T : Any> publishService(cls: Class<T>, service: T): Boolean {
        serviceMap[cls] = service
        return true
    }

    override fun <T : Any> publishRemoteService(cls: Class<T>, service: T): Boolean {
        val rsm = remoteServiceManager ?: return false
        return try {
            rsm.publishService(cls.name, getServiceRemote(cls, service))
            true
        } catch (e: Exception) {
            Log.e(TAG, "publishRemoteService: ${e.message}", e)
            false
        }
    }

    override fun <T : Any> getService(cls: Class<T>): T? {
        return serviceMap[cls] as? T
    }

    override fun <T : Any> bindRemoteService(cls: Class<T>, onServiceBind: OnServiceBind<T>) {
        val rsm = remoteServiceManager
        if (rsm == null) {
            onServiceBind.onBindFailed(RemoteServiceException("please initRemoteService before bind a remote service"))
            return
        }

        val rs = rsm.getService(cls.name)
        if (rs == null) {
            onServiceBind.onBindFailed(RemoteServiceException("please make sure remote service has been published: ${cls.name}"))
            return
        }

        onServiceBind.onBindSuccess(getServiceRemoteProxy(cls, rs))

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
}