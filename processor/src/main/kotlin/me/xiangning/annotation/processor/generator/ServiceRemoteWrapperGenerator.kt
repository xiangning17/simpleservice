package me.xiangning.annotation.processor.generator

import me.xiangning.annotation.processor.AidlUtils
import me.xiangning.annotation.processor.AidlUtils.DATE_FORMAT
import me.xiangning.annotation.processor.AidlUtils.INDENTS
import me.xiangning.annotation.processor.AidlUtils.getDefaultValue
import me.xiangning.annotation.processor.AidlUtils.getOutSourceDir
import me.xiangning.annotation.processor.AidlUtils.isAidlService
import me.xiangning.annotation.processor.AidlUtils.normalizeName
import me.xiangning.annotation.processor.AidlUtils.packageName
import me.xiangning.annotation.processor.AidlUtils.save
import me.xiangning.annotation.processor.AidlUtils.simpleName
import me.xiangning.annotation.processor.AidlUtils.transformService
import me.xiangning.annotation.processor.AidlUtils.transformServiceName
import me.xiangning.annotation.processor.SourceGenerator
import org.gradle.internal.impldep.org.apache.commons.lang.text.StrBuilder
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Created by xiangning on 2021/7/11.
 */
object ServiceRemoteWrapperGenerator : SourceGenerator {

    private lateinit var service: TypeElement

    private val content = StrBuilder()

    override fun start(service: TypeElement) {
        this.service = service
        content.clear()

        // public class MusicServiceRemoteWrapper extends MusicServiceBinder.Stub {
        //    public MusicService service;
        //
        //    public MusicServiceRemote(MusicService service) {
        //        this.service = service;
        //    }

        val serviceType = service.simpleName.toString()
        val serviceRemoteType = serviceType + AidlUtils.REMOTE_WRAPPER_SUFFIX
        content.append("\n\npublic class ")
            .append(serviceRemoteType)
            .append(" extends ").append(service.simpleName).append(AidlUtils.AIDL_SUFFIX)
            .append(".Stub {\n")

        content.append(INDENTS).append("public ").append(serviceType).append(" service;\n\n")
            .append(INDENTS).append("public ").append(serviceRemoteType).append("(")
            .append(serviceType).append(" service) {\n")
            .append(INDENTS).append(INDENTS).append("this.service = service;\n")
            .append(INDENTS).append("}\n\n")
    }

    override fun onMethod(method: ExecutableElement) {
        //    @Override
        //    public boolean play(List<String> names) {
        //        return service.play(names);
        //    }

        // 返回类型
        val ret = method.returnType
        val retDefault = ret.getDefaultValue()

        // 参数列表
        val parameters = method.parameters.joinToString(", ", "(", ")") { param ->
            param.asType().transformService.simpleName() + " " + param.simpleName
        }

        content.append(INDENTS).append("@Override\n")
            .append(INDENTS) // 缩进
            .append(method.modifiers.filter { it != Modifier.ABSTRACT }.joinToString(" "))
            .append(" ")
            .append(ret.transformService.simpleName()).append(" ") // 返回值
            .append(method.simpleName) // 方法名
            .append(parameters) // 参数列表
            .append(" {\n") // 结束换行

        content.append(INDENTS).append(INDENTS)
            .append(if (retDefault != null) "return " else "")
            .append("this.service.")
            .append(method.simpleName)
            .append(method.parameters.joinToString(", ", "(", ")") { param ->
                val normalizeName = param.asType().normalizeName
                if (normalizeName.isAidlService()) {
                    "new " + normalizeName.simpleName() + AidlUtils.LOCAL_WRAPPER_SUFFIX + "(" + param.simpleName + ")"
                } else {
                    param.simpleName
                }
            })
            .append(";\n")
            .append(INDENTS).append("}\n\n")
    }

    override fun generate(imports: List<String>) {
        // 主体结束
        content.append("}")

        // 头部拼接
        val header = StringBuilder()
            .append("// generate by aidl processor at ${DATE_FORMAT.format(System.currentTimeMillis())}\n")
            .append("\npackage ${service.packageName};\n")

        // import声明
        imports.asSequence()
            .flatMap { origin ->
                val transformService = origin.transformServiceName()
                if (transformService != origin) {
                    listOf(origin, transformService).asSequence()
                } else {
                    listOf(origin).asSequence()
                }
            }
            .sorted() // 给import排个序
            .forEach { element ->
                header.append("\nimport ")
                    .append(element)
                    .append(";")
            }

        // 写入文件
        AidlUtils.getOutputFile(
            getOutSourceDir(),
            service.packageName,
            "${service.simpleName.toString() + AidlUtils.REMOTE_WRAPPER_SUFFIX}.java"
        ).save(header.append(content).toString())
    }

}