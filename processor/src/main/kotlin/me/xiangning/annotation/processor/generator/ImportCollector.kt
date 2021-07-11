package me.xiangning.annotation.processor.generator

import me.xiangning.annotation.processor.AidlUtils
import me.xiangning.annotation.processor.AidlUtils.log
import me.xiangning.annotation.processor.AidlUtils.normalizeName
import me.xiangning.annotation.processor.SourceGenerator
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Created by xiangning on 2021/7/11.
 */
object ImportCollector : SourceGenerator {

    private lateinit var service: TypeElement

    private val imports = mutableSetOf<String>()

    override fun start(service: TypeElement) {
        this.service = service
        imports.clear()
    }

    override fun onMethod(method: ExecutableElement) {
        recordImport(method.returnType)
        method.parameters.forEach { recordImport(it.asType()) }
    }

    override fun generate(imports: List<String>) {

    }

    private fun recordImport(type: TypeMirror) {
        val normalize = type.normalizeName
        log("record import: $type -> $normalize")
        AidlUtils.REX_QUALIFY_NORMALIZE.findAll(normalize).forEach {
            val cls = it.value
            if (!AidlUtils.isBasicType(cls)) {
                imports.add(cls)
                log("record import: add $cls")
            }
        }
    }

    fun getImports(): List<String> {
        return imports.toList()
    }
}