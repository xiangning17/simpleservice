package me.xiangning.simpleservice.music;

import me.xiangning.annotation.Aidl;

/**
 * Created by xiangning on 2021/7/4.
 */
@Aidl
public interface OnDownloadListener {
    void onProgress(int progress);
}
