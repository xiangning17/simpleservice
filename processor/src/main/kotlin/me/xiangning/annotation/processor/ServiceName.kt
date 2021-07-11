package me.xiangning.annotation.processor

/**
 * Created by xiangning on 2021/7/11.
 */
data class ServiceName(val origin: String) {
    val simple: String
        get() = ""
}

val ServiceName.binder: ServiceName
    get() = ServiceName(origin + "")
