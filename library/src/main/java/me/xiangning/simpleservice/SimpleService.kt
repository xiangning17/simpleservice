package me.xiangning.simpleservice

import android.os.IBinder
import android.os.IInterface
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler

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

    override fun <T : IBinder> getServiceRemote(service: Any): T {
        TODO("Not yet implemented")
    }

    override fun <T> getServiceRemoteProxy(service: IInterface): T {
        TODO("Not yet implemented")
    }

    override fun registerMethodErrorHandler(cls: Class<*>, handler: IMethodErrorHandler) {
        TODO("Not yet implemented")
    }

    override fun getMethodErrorHandler(cls: Class<*>): IMethodErrorHandler {
        TODO("Not yet implemented")
    }
}