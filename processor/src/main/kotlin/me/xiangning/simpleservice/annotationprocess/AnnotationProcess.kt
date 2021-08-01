package me.xiangning.simpleservice.annotationprocess

import com.google.auto.service.AutoService
import me.xiangning.simpleservice.annotation.ParcelableAidl
import me.xiangning.simpleservice.annotation.RemoteService
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.getOutDir
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.simpleName
import me.xiangning.simpleservice.annotationprocess.generators.*
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
class AnnotationProcess : AbstractProcessor() {

    companion object {
        val currentServices = mutableSetOf<String>()
    }

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        ProcessUtils.init(processingEnv)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            RemoteService::class.java.canonicalName,
            ParcelableAidl::class.java.canonicalName
        )
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

        val parcelables = roundEnv.getElementsAnnotatedWith(ParcelableAidl::class.java)
            .filterIsInstance<TypeElement>()
            .filter { type ->
                type.kind.isClass && type.interfaces.find {
                    it.simpleName == "Parcelable"
                } != null
            }

        val services = roundEnv.getElementsAnnotatedWith(RemoteService::class.java)
            .filter { it.kind.isInterface }
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
            ServiceRemoteGenerator,
            ServiceRemoteProxyGenerator,
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