package me.xiangning.simpleservice;

import java.util.List;

import me.xiangning.annotation.Aidl;
import me.xiangning.annotation.Out;

/**
 * Created by xiangning on 2021/7/3.
 */
@Aidl
public interface MusicService {
    void play(@Out List<String> names);
}
