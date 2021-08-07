package me.xiangning.simpleservice

import android.os.IBinder
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler

/**
 * Created by xiangning on 2021/7/3.
 */
interface ServiceManager {
    fun <T : Any> publishService(cls: Class<T>, service: T): Boolean
    fun <T : Any> getService(cls: Class<T>): T?

    fun <T : Any> publishRemoteService(
        cls: Class<T>,
        service: T,
        onRemoteServicePublish: OnRemoteServicePublish? = null
    )

    fun <T : Any> bindRemoteService(cls: Class<T>, onRemoteServiceBind: OnRemoteServiceBind<T>)

    fun <T : Any, R : IBinder> getServiceRemote(cls: Class<T>, service: T): R
    fun <T> getServiceRemoteProxy(cls: Class<T>, service: IBinder): T

    fun registerMethodErrorHandler(cls: Class<*>, handler: IMethodErrorHandler)
    fun getMethodErrorHandler(cls: Class<*>): IMethodErrorHandler
}