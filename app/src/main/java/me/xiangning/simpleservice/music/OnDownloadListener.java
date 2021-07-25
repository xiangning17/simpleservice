package me.xiangning.simpleservice.music;

import me.xiangning.simpleservice.annotation.ParcelableAidl;

/**
 * Created by xiangning on 2021/7/4.
 */
@ParcelableAidl
public interface OnDownloadListener {
    void onProgress(int progress);
}
