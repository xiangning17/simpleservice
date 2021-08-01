package me.xiangning.simpleservice.annotationprocess.generators

import me.xiangning.simpleservice.ServiceManager
import me.xiangning.simpleservice.SimpleServiceConstants
import me.xiangning.simpleservice.annotationprocess.ProcessUtils
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.DATE_FORMAT
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.INDENTS
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.getDefaultValue
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.getOutSourceDir
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.isAidlService
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.normalizeName
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.packageName
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.save
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.simpleName
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.transformService
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.transformServiceName
import me.xiangning.simpleservice.annotationprocess.ServiceSourceGenerator
import org.gradle.internal.impldep.org.apache.commons.lang.text.StrBuilder
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Created by xiangning on 2021/7/11.
 */
object ServiceRemoteGenerator : ServiceSourceGenerator {

    private lateinit var service: TypeElement

    private val content = StrBuilder()

    override fun start(service: TypeElement) {
        ServiceRemoteGenerator.service = service
        content.clear()

        // public class MusicServiceRemoteWrapper extends MusicServiceBinder.Stub {
        //    public MusicService service;
        //
        //    public MusicServiceRemote(MusicService service) {
        //        this.service = service;
        //    }

        val serviceType = service.simpleName.toString()
        val serviceRemoteType = serviceType + SimpleServiceConstants.REMOTE_SUFFIX
        content.append("\n\npublic class ")
            .append(serviceRemoteType)
            .append(" extends ").append(service.simpleName)
            .append(SimpleServiceConstants.AIDL_SUFFIX)
            .append(".Stub {\n")

        content.append(INDENTS).append("private ServiceManager sm;\n")
        content.append(INDENTS).append("public ").append(serviceType).append(" service;\n\n")

        content.append(INDENTS).append("public ").append(serviceRemoteType).append("(")
            .append("ServiceManager sm, ")
            .append(serviceType).append(" service) {\n")
            .append(INDENTS).append(INDENTS).append("this.sm = sm;\n")
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
                    "this.sm.getServiceRemoteProxy(${normalizeName.simpleName()}.class, ${param.simpleName})"
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
        val imported = mutableListOf<String>().apply {
            addAll(imports)
            add(ServiceManager::class.java.canonicalName)
        }
        imported.asSequence()
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
        ProcessUtils.getOutputFile(
            getOutSourceDir(),
            service.packageName,
            "${service.simpleName.toString() + SimpleServiceConstants.REMOTE_SUFFIX}.java"
        ).save(header.append(content).toString())
    }

}