package me.xiangning.simpleservice.annotationprocess.generators

import me.xiangning.simpleservice.annotationprocess.ProcessUtils
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.log
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.normalizeName
import me.xiangning.simpleservice.annotationprocess.ServiceSourceGenerator
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Created by xiangning on 2021/7/11.
 */
object ImportCollector : ServiceSourceGenerator {

    private lateinit var service: TypeElement

    private val imports = mutableSetOf<String>()

    override fun start(service: TypeElement) {
        ImportCollector.service = service
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
        ProcessUtils.REX_QUALIFY_NORMALIZE.findAll(normalize).forEach {
            val cls = it.value
            if (!ProcessUtils.isBasicType(cls)) {
                imports.add(cls)
                log("record import: add $cls")
            }
        }
    }

    fun getImports(): List<String> {
        return imports.toList()
    }
}