package me.xiangning.simpleservice.demo.music;

import me.xiangning.simpleservice.annotation.RemoteService;

/**
 * Created by xiangning on 2021/7/4.
 */
@RemoteService
public interface OnDownloadListener {
    void onProgress(int progress);
}
