package me.xiangning.annotation.processor

import com.google.auto.service.AutoService
import me.xiangning.annotation.Aidl
import me.xiangning.annotation.processor.AidlUtils.getOutDir
import me.xiangning.annotation.processor.AidlUtils.simpleName
import me.xiangning.annotation.processor.generator.*
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class AidlProcess : AbstractProcessor() {

    companion object {
        val currentServices = mutableSetOf<String>()
    }

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        AidlUtils.init(processingEnv)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Aidl::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        if (roundEnv.rootElements.isEmpty()) {
            return false
        }

        val annotated = roundEnv.getElementsAnnotatedWith(Aidl::class.java)
        val parcelables = annotated.filterIsInstance<TypeElement>()
            .filter { type ->
                type.kind.isClass && type.interfaces.find {
                    it.simpleName == "Parcelable"
                } != null
            }

        val services = annotated.filter { it.kind.isInterface }
            .filterIsInstance<TypeElement>()
        // 保存当前服务
        currentServices.addAll(services.map { it.qualifiedName.toString() })

        // 删除out目录
        File(getOutDir()).deleteRecursively()

        // 为parcelable生成aidl文件
        parcelables.forEach {
            ParcelableAidlGenerator.start(it)
            ParcelableAidlGenerator.generate(emptyList())
        }

        // 为service生成aidl
        val generators = listOf(
            ServiceAidlGenerator,
            ServiceRemoteWrapperGenerator,
            ServiceLocalWrapperGenerator,
        )
        services.forEach { service ->
            generators.forEach {
                ImportCollector.start(service)
                it.start(service)
            }

            service.enclosedElements
                .filter { it.kind == ElementKind.METHOD }
                .map { it as ExecutableElement }
                .forEach { method ->
                    ImportCollector.onMethod(method)
                    generators.forEach { it.onMethod(method) }
                }
            generators.forEach { it.generate(ImportCollector.getImports()) }
        }

        currentServices.clear()
        return true
    }

}