package me.xiangning.simpleservice.annotationprocess

import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Created by xiangning on 2021/7/11.
 */
interface ServiceSourceGenerator {

    fun start(service: TypeElement)

    fun onMethod(method: ExecutableElement)

    fun generate(imports: List<String>)

}