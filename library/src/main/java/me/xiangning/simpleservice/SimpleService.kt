package me.xiangning.simpleservice

import android.os.IBinder
import android.os.IInterface
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
        return RemoteServiceHelper.getServiceRemote(cls, service) as R
    }

    override fun <T> getServiceRemoteProxy(cls: Class<T>, service: IInterface): T {
        return RemoteServiceHelper.getServiceRemoteProxy(cls, service)
    }

    override fun registerMethodErrorHandler(cls: Class<*>, handler: IMethodErrorHandler) {
        TODO("Not yet implemented")
    }

    override fun getMethodErrorHandler(cls: Class<*>): IMethodErrorHandler {
        TODO("Not yet implemented")
    }
}