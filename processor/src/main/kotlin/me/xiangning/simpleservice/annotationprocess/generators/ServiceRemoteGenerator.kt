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
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Created by xiangning on 2021/7/11.
 */
object ServiceRemoteGenerator : ServiceSourceGenerator {

    private lateinit var service: TypeElement

    private val content = StringBuilder()

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

        // ????????????
        val ret = method.returnType
        val retDefault = ret.getDefaultValue()

        // ????????????
        val parameters = method.parameters.joinToString(", ", "(", ")") { param ->
            param.asType().transformService.simpleName() + " " + param.simpleName
        }

        content.append(INDENTS).append("@Override\n")
            .append(INDENTS) // ??????
            .append(method.modifiers.filter { it != Modifier.ABSTRACT }.joinToString(" "))
            .append(" ")
            .append(ret.transformService.simpleName()).append(" ") // ?????????
            .append(method.simpleName) // ?????????
            .append(parameters) // ????????????
            .append(" {\n") // ????????????

        content.append(INDENTS).append(INDENTS)
            .append(if (retDefault != null) "return " else "")
            .append("this.service.")
            .append(method.simpleName)
            .append(method.parameters.joinToString(", ", "(", ")") { param ->
                val normalizeName = param.asType().normalizeName
                if (normalizeName.isAidlService()) {
                    "this.sm.getServiceRemoteProxy(${normalizeName.simpleName()}.class, ${param.simpleName}.asBinder())"
                } else {
                    param.simpleName
                }
            })
            .append(";\n")
            .append(INDENTS).append("}\n\n")
    }

    override fun generate(imports: List<String>) {
        // ????????????
        content.append("}")

        // ????????????
        val header = StringBuilder()
            .append("// generate by aidl processor at ${DATE_FORMAT.format(System.currentTimeMillis())}\n")
            .append("\npackage ${service.packageName};\n")

        // import??????
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
            .sorted() // ???import?????????
            .forEach { element ->
                header.append("\nimport ")
                    .append(element)
                    .append(";")
            }

        // ????????????
        ProcessUtils.getOutputFile(
            getOutSourceDir(),
            service.packageName,
            "${service.simpleName.toString() + SimpleServiceConstants.REMOTE_SUFFIX}.java"
        ).save(header.append(content).toString())
    }

}