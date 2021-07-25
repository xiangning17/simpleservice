package me.xiangning.simpleservice.music;

import java.util.List;

import me.xiangning.simpleservice.annotation.Out;
import me.xiangning.simpleservice.annotation.ParcelableAidl;

/**
 * Created by xiangning on 2021/7/3.
 */
@ParcelableAidl
public interface MusicService {
    boolean play(@Out List<String> names);

    void download(String name, OnDownloadListener listener);
}
