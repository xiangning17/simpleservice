package me.xiangning.simpleservice.demo.music

/**
 * Created by xiangning on 2021/7/3.
 */
class MusicServiceImpl: MusicService {
    override fun play(names: MutableList<String>?): Boolean {
        return true
    }

    override fun download(name: String?, listener: OnDownloadListener?) {
        TODO("Not yet implemented")
    }
}