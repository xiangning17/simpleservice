package me.xiangning.simpleservice.methoderror

/**
 * Created by xiangning on 2021/7/31.
 */
interface IMethodErrorHandler {

    /**
     * 方法调用错误时的处理接口，需要通过此方法的返回值返回需要的值以使程序继续，
     * 否则程序会因为异常而中断执行
     */
    fun onMethodError(
        e: Throwable, // 异常
        returnType: Class<*>, // 方法的返回类型
        methodName: String, // 方法名
        vararg args: Any? // 方法的参数列表
    ): Any?
}