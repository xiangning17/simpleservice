package me.xiangning.simpleservice.demo.music

/**
 * Created by xiangning on 2021/7/4.
 */
//class DownloadRemoteWrapper(val real: OnDownloadListener) : OnDownloadListenerBinder.Stub(), OnDownloadListener by real
//class DownloadListenerProxyWrapper constructor(val stub: OnDownloadListenerBinder): OnDownloadListenerBinder by stub, OnDownloadListener
//
//fun OnDownloadListener.asRemoteWrapper() = DownloadRemoteWrapper(this)
//fun OnDownloadListenerBinder.asInterface(): OnDownloadListener = DownloadListenerProxyWrapper(this)