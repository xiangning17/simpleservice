package me.xiangning.simpleservice

/**
 * 绑定远程服务的回调接口
 * Created by xiangning on 2021/7/31.
 */
interface OnRemoteServiceBind<T> {
    fun onBindSuccess(service: T)
    fun onBindFailed(error: Throwable?)
}