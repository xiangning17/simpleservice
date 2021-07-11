package me.xiangning.annotation.processor

import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Created by xiangning on 2021/7/11.
 */
interface SourceGenerator {

    fun start(service: TypeElement)

    fun onMethod(method: ExecutableElement)

    fun generate(imports: List<String>)

}