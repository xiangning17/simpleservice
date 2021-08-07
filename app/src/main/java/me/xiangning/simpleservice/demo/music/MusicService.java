package me.xiangning.simpleservice.demo.music;

import me.xiangning.simpleservice.annotation.RemoteService;

/**
 * Created by xiangning on 2021/7/3.
 */
@RemoteService
public interface MusicService {
    boolean play(String name);

    void download(String name, OnDownloadListener listener);
}
