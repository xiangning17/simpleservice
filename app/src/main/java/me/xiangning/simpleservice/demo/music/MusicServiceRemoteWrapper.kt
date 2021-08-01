package me.xiangning.simpleservice.demo.music

/**
 * Created by xiangning on 2021/7/3.
 */
//class MusicServiceRemoteWrapper(var real: MusicService?): MusicServiceBinder.Stub(), MusicService {
//
//    override fun play(names: MutableList<String>?) {
//        val intercepted = onMethodIntercept("play", names as Any, 1, "", null)
//        if (intercepted != null) {
//            when (intercepted) {
//                is InterceptMethodResult.Ok -> return intercepted.result as Unit
//                is InterceptMethodResult.Exception -> throw intercepted.error
//            }
//        }
//    }
//
//    override fun download(name: String?, listener: OnDownloadListener?) {
//        real?.download(name, listener)
//    }
//
//    override fun download(name: String?, listener: OnDownloadListenerBinder) {
//        real?.download(name, listener.asInterface())
//    }
//
//    open fun onMethodIntercept(method: String, vararg args: Any?): InterceptMethodResult? {
//        return null
//    }
//
//}

//fun MusicService.asRemoteWrapper() = MusicServiceRemoteWrapper(this)
//fun IBinder.asMusicService() = MusicServiceProxyWrapper(this)