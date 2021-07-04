package me.xiangning.simpleservice.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SimpleService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}