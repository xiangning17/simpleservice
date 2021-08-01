package me.xiangning.simpleservice

import android.os.IBinder
import android.os.IInterface

/**
 * Created by xiangning on 2021/7/31.
 */
interface IRemoteServiceProxy : IInterface {

    override fun asBinder(): IBinder

    fun setBinder(binder: IBinder)

}