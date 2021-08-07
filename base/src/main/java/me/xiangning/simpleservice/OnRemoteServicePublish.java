package me.xiangning.simpleservice;

/**
 * Created by xiangning on 2021/8/7.
 */
public interface OnRemoteServicePublish {
    /**
     * success if error is null, otherwise failed.
     */
    void onPublishResult(Throwable error);
}
