package me.xiangning.simpleservice

/**
 * Created by xiangning on 2021/7/31.
 */
class DefaultValueMethodErrorHandler : IMethodErrorHandler {
    override fun onMethodError(
        e: Throwable,
        returnType: Class<*>,
        methodName: String,
        vararg args: Any?
    ): Any? {
        return when {
            returnType.isAssignableFrom(Boolean::class.java) -> false

            returnType.isAssignableFrom(Char::class.java) ||
                    returnType.isAssignableFrom(Byte::class.java) ||
                    returnType.isAssignableFrom(Short::class.java) ||
                    returnType.isAssignableFrom(Int::class.java) ||
                    returnType.isAssignableFrom(Long::class.java) -> 0

            returnType.isAssignableFrom(Float::class.java) ||
                    returnType.isAssignableFrom(Double::class.java) -> 0.0

            else -> null
        }
    }

}