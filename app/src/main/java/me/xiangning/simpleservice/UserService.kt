package me.xiangning.simpleservice

import me.xiangning.simpleservice.annotation.OneWay
import me.xiangning.simpleservice.annotation.ParcelableAidl

/**
 * Created by xiangning on 2021/7/2.
 */
@ParcelableAidl
interface UserService {
    @OneWay
    fun findByName(name: String)
}