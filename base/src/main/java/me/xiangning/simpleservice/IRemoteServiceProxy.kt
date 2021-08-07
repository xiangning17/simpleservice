package me.xiangning.simpleservice

import android.os.IBinder
import android.os.IInterface
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler

/**
 * Created by xiangning on 2021/7/31.
 */
interface IRemoteServiceProxy {

    fun setBinder(binder: IBinder)
    fun getRemoteInterface(): IInterface

    fun setMethodErrorHandler(handler: IMethodErrorHandler)
    fun getMethodErrorHandler(): IMethodErrorHandler

}