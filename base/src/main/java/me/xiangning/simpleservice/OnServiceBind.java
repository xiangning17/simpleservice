package me.xiangning.simpleservice;

/**
 * Created by xiangning on 2021/7/31.
 */
public interface OnServiceBind<T> {
    void onBindSuccess(T service);

    void onBindFailed(Throwable error);
}
