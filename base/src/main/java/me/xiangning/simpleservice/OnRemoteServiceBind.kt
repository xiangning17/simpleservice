package me.xiangning.simpleservice

/**
 * Created by xiangning on 2021/7/31.
 */
interface OnRemoteServiceBind<T> {
    fun onBindSuccess(service: T)
    fun onBindFailed(error: Throwable?)
}