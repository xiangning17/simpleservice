package me.xiangning.simpleservice.demo

import android.app.Application
import me.xiangning.simpleservice.SimpleService

/**
 * Created by xiangning on 2021/8/1.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SimpleService.initRemoteService(this)
    }
}