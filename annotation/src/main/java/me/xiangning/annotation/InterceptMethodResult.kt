package me.xiangning.annotation

/**
 * Created by xiangning on 2021/7/4.
 */
sealed class InterceptMethodResult {
    data class Ok(val result: Any?) : InterceptMethodResult()
    data class Exception(val error: Throwable) : InterceptMethodResult()
}

