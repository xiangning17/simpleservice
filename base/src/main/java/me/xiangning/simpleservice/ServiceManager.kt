package me.xiangning.simpleservice

import android.os.IBinder
import android.os.IInterface
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler

/**
 * # 服务管理接口
 * Created by xiangning on 2021/7/3.
 */
interface ServiceManager {
    /**
     * 发布本地服务，[cls]为服务类型，[service]为服务对象
     */
    fun <T : Any> publishService(cls: Class<T>, service: T): Boolean

    /**
     * 获取本地服务，[cls]为服务类型
     * @return 返回对应的服务对象或者null
     */
    fun <T : Any> getService(cls: Class<T>): T?

    /**
     * 发布远程服务，[cls]为服务类型，该类型必须用[@RemoteService]注解修饰，
     * [service]为服务对象，[onRemoteServicePublish]是服务发布结果回调，可以为null
     */
    fun <T : Any> publishRemoteService(
        cls: Class<T>,
        service: T,
        onRemoteServicePublish: OnRemoteServicePublish? = null
    )

    /**
     * 绑定远程服务，[cls]为服务类型，该类型必须用[@RemoteService]注解修饰，
     * [onRemoteServiceBind]是服务绑定结果回调
     */
    fun <T : Any> bindRemoteService(cls: Class<T>, onRemoteServiceBind: OnRemoteServiceBind<T>)

    /**
     * 判断远程服务是否还存活,参数[proxy]为通过[SimpleService]获取的远程服务代理对象
     */
    fun isRemoteServiceAlive(proxy: Any): Boolean

    /**
     * 获取本地服务转换成的Binder.Stub对象
     */
    fun <T : Any, R : IBinder> getServiceRemote(cls: Class<T>, service: T): R

    /**
     * 获取远程服务在本地的代理对象
     */
    fun <T> getServiceRemoteProxy(cls: Class<T>, service: IBinder): T

    /**
     * 获取远程服务在本地的IInterface对象，方便有些远程回调想用RemoteCallbackList进行管理，
     * 可参考‘RemoteServiceManager.registerServiceStateListener’。
     * 泛型是自动生成的‘{服务类型}Binder’，参数[proxy]必须是通过[bindRemoteService]得到的远程服务对象
     */
    fun <T : IInterface> getServiceRemoteInterface(proxy: Any): T

    /**
     * 为特定服务类型注册自定义的错误处理器，只能用于远程服务
     */
    fun registerMethodErrorHandler(cls: Class<*>, handler: IMethodErrorHandler)

    /**
     * 获取当前关联到的错误处理器，只针对于远程服务
     */
    fun getMethodErrorHandler(cls: Class<*>): IMethodErrorHandler
}