package me.xiangning.simpleservice

import me.xiangning.annotation.Aidl
import me.xiangning.annotation.OneWay

/**
 * Created by xiangning on 2021/7/2.
 */
@Aidl
interface UserService {
    @OneWay
    fun findByName(name: String)
}