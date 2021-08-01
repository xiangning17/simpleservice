package me.xiangning.simpleservice

import android.os.IBinder
import android.os.IInterface
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler

/**
 * Created by xiangning on 2021/7/3.
 */
interface ServiceManager {
    fun <T> publishService(cls: Class<T>, service: T): Boolean
    fun <T> publishRemoteService(cls: Class<T>, service: T): Boolean

    fun <T> getService(cls: Class<T>): T?
    fun <T> bindRemoteService(cls: Class<T>, onServiceBind: OnServiceBind<T>)

    fun <T : IBinder> getServiceRemote(service: Any): T
    fun <T> getServiceRemoteProxy(service: IInterface): T

    fun registerMethodErrorHandler(cls: Class<*>, handler: IMethodErrorHandler)
    fun getMethodErrorHandler(cls: Class<*>): IMethodErrorHandler
}