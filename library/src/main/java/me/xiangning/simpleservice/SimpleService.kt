package me.xiangning.simpleservice

import android.os.IBinder
import android.os.IInterface
import me.xiangning.simpleservice.exception.RemoteServiceException
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler
import me.xiangning.simpleservice.remote.RemoteServiceHelper

/**
 * Created by xiangning on 2021/8/1.
 */
object SimpleService : ServiceManager {
    override fun <T> publishService(cls: Class<T>, service: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T> publishRemoteService(cls: Class<T>, service: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T> getService(cls: Class<T>): T? {
        TODO("Not yet implemented")
    }

    override fun <T> bindRemoteService(cls: Class<T>, onServiceBind: OnServiceBind<T>) {
        TODO("Not yet implemented")
    }

    override fun <T : Any, R : IBinder> getServiceRemote(cls: Class<T>, service: T): R {
        try {
            return RemoteServiceHelper.getServiceRemote(cls, service) as R
        } catch (e: Exception) {
            throw RemoteServiceException("get service remote failed")
        }
    }

    override fun <T> getServiceRemoteProxy(cls: Class<T>, service: IInterface): T {
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