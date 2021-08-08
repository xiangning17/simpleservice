package me.xiangning.simpleservice.annotationprocess

import me.xiangning.simpleservice.SimpleServiceConstants
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ReferenceType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import javax.tools.StandardLocation

/**
 * Created by xiangning on 2021/7/11.
 */
object ProcessUtils {

    const val INDENTS = "    "
    val REX_QUALIFY_NORMALIZE = Regex("[_a-zA-Z0-9.]+\\.([_a-zA-Z0-9]+)")
    val REX_REMOVE_WILDCARD = Regex("\\? (extends|super) ")
    val DATE_FORMAT = SimpleDateFormat("YYYY/MM/dd HH:mm:ss")

    private lateinit var env: ProcessingEnvironment
    private lateinit var basicTypes: BasicType

    fun init(processingEnv: ProcessingEnvironment) {
        env = processingEnv
        basicTypes = BasicType(processingEnv.typeUtils)
    }

    fun getOutDir(): String {
        return env.options["simpleservice.outdir"]
            ?: kotlin.run {
                loge("未找到输出路径，请确保启用了SimpleService插件")
                throw IllegalArgumentException("未找到输出路径，请确保启用了SimpleService插件")
            }
    }

    fun getOutAidlDir(): String {
        return env.options["simpleservice.outdir.aidl"]
            ?: kotlin.run {
                loge("未找到输出路径，请确保启用了SimpleService插件")
                throw IllegalArgumentException("未找到输出路径，请确保启用了SimpleService插件")
            }
    }

    fun getOutSourceDir(): String {
        return env.options["simpleservice.outdir.source"]
            ?: kotlin.run {
                loge("未找到输出路径，请确保启用了SimpleService插件")
                throw IllegalArgumentException("未找到输出路径，请确保启用了SimpleService插件")
            }
    }

    fun getOutputFile(outputDir: String?, pkg: String, name: String): File {
        val path = if (outputDir != null) {
            File(outputDir, pkg.replace(".", File.separator) + File.separator + name).absolutePath
        } else {
            env.filer.getResource(StandardLocation.SOURCE_OUTPUT, pkg, name).name
        }
        return File(path)
    }

    val TypeElement.packageName: String
        get() = env.elementUtils.getPackageOf(this).toString()

    val TypeMirror.normalizeName: String
        get() = toString().normalizeName()

    val TypeMirror.simpleName: String
        get() = normalizeName.simpleName()

    val TypeMirror.transformService: String
        get() = normalizeName.transformServiceName()

    /**
     * 把全限定类型转为简单类名
     */
    fun String.simpleName(): String {
        return REX_QUALIFY_NORMALIZE.replace(this, "$1")
            // 防止‘List<List<String>>’中的‘>>’导致的编译错误
            .replace(">>", "> >")
    }

    /**
     * 去除类型中的通配符
     */
    fun String.normalizeName(): String {
        return REX_REMOVE_WILDCARD.replace(this, "")
    }

    /**
     * 将类型转为对应的Binder类型名
     */
    fun String.transformServiceName(): String {
        return REX_QUALIFY_NORMALIZE.replace(this) {
            if (AnnotationProcess.currentServices.contains(it.value)) {
                it.value + SimpleServiceConstants.AIDL_SUFFIX
            } else {
                it.value
            }
        }
    }

    fun String.isAidlService(): Boolean {
        return AnnotationProcess.currentServices.contains(this)
    }

    fun isBasicType(type: String) = basicTypes.isBasicType(type)

    fun Class<*>.asType(): TypeMirror =
        env.elementUtils.getTypeElement(this.canonicalName).asType()

    fun TypeMirror.asElement() = env.typeUtils.asElement(this)

    fun TypeMirror.getDefaultValue(): String? {
        return when (kind) {
            TypeKind.BOOLEAN -> "false"
            TypeKind.CHAR,
            TypeKind.BYTE,
            TypeKind.SHORT,
            TypeKind.INT,
            TypeKind.LONG -> "0"
            TypeKind.FLOAT,
            TypeKind.DOUBLE -> "0.0"
            else -> {
                if (this is ReferenceType) {
                    return "null"
                }

                return null
            }
        }
    }

    @Throws(IOException::class)
    fun File.save(content: String) {
        if (!this.exists()) {
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
            createNewFile()
        }

        try {
            FileWriter(this).use { writer ->
                writer.write(content)
                writer.flush()
            }
        } catch (e: Exception) {
            try {
                this.delete()
            } catch (ignored: Exception) {
            }
            throw e
        }
    }

    fun log(msg: String) {
        env.messager.printMessage(Diagnostic.Kind.NOTE, msg + "\n")
    }

    fun logw(msg: String) {
        env.messager.printMessage(Diagnostic.Kind.WARNING, msg + "\n")
    }

    fun loge(msg: String) {
        env.messager.printMessage(Diagnostic.Kind.ERROR, msg + "\n")
    }

}