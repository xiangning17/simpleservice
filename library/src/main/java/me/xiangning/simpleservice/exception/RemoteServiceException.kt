package me.xiangning.simpleservice.exception

/**
 * Created by xiangning on 2021/8/1.
 */
class RemoteServiceException(msg: String) :
    RuntimeException("$msg, please check the usage of simple service and try again.") {
}