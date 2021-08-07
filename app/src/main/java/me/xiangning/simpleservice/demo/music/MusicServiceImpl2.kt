package me.xiangning.simpleservice.demo.music

import android.util.Log

/**
 * Created by xiangning on 2021/7/3.
 */
class MusicServiceImpl2 : MusicService {

    override fun play(name: String?): Boolean {
        Log.e("MusicServiceImpl2", "play: $name")
        return true
    }

    override fun download(name: String, listener: OnDownloadListener) {
        Log.e("MusicServiceImpl2", "download start: $name")
        listener.onProgress(80)
        Log.e("MusicServiceImpl2", "download end: $name")
    }
}