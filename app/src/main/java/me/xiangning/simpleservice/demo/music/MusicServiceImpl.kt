package me.xiangning.simpleservice.demo.music

import android.util.Log

/**
 * Created by xiangning on 2021/7/3.
 */
class MusicServiceImpl : MusicService {

    override fun play(name: String?): Boolean {
        Log.e("MusicServiceImpl", "play: $name")
        return true
    }

    override fun download(name: String, listener: OnDownloadListener) {
        Log.e("MusicServiceImpl", "download start: $name")
        listener.onProgress(66)
        Log.e("MusicServiceImpl", "download end: $name")
    }
}