package me.xiangning.simpleservice.demo

import android.app.Application
import leakcanary.AppWatcher
import me.xiangning.simpleservice.SimpleService

/**
 * Created by xiangning on 2021/8/1.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SimpleService.initRemoteService(this)

        if (!AppWatcher.isInstalled) {
            AppWatcher.manualInstall(this)
        }
    }
}