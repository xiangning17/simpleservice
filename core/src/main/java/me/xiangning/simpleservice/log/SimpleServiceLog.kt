package me.xiangning.simpleservice.log

import android.util.Log
import me.xiangning.simpleservice.BuildConfig

/**
 * Created by xiangning on 2021/8/7.
 */
internal object SimpleServiceLog {

    var debugable = BuildConfig.DEBUG

    inline fun d(tag: String, msg: () -> String) {
        if (debugable) {
            Log.d(tag, msg())
        }
    }

    inline fun i(tag: String, msg: () -> String) {
        if (debugable) {
            Log.i(tag, msg())
        }
    }

    inline fun w(tag: String, msg: () -> String) {
        if (debugable) {
            Log.w(tag, msg())
        }
    }

    inline fun e(tag: String, e: Exception, msg: () -> String) {
        Log.e(tag, msg(), e)
    }
}