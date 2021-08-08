package me.xiangning.simpleservice.annotationprocess.generators

import me.xiangning.simpleservice.SimpleServiceConstants
import me.xiangning.simpleservice.annotation.InOut
import me.xiangning.simpleservice.annotation.OneWay
import me.xiangning.simpleservice.annotation.Out
import me.xiangning.simpleservice.annotationprocess.ProcessUtils
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.DATE_FORMAT
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.INDENTS
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.getOutAidlDir
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.isBasicType
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.packageName
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.save
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.simpleName
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.transformService
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.transformServiceName
import me.xiangning.simpleservice.annotationprocess.ServiceSourceGenerator
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.TypeMirror

/**
 * Created by xiangning on 2021/7/11.
 */
object ServiceAidlGenerator : ServiceSourceGenerator {

    private lateinit var service: TypeElement

    private val content = StringBuilder()

    override fun start(service: TypeElement) {
        ServiceAidlGenerator.service = service
        content.clear()

        // 接口声明头部
        content.append("\n\ninterface ")
            .append(service.simpleName).append(SimpleServiceConstants.AIDL_SUFFIX)
            .append(" {\n")
    }

    override fun onMethod(method: ExecutableElement) {
        // 是否是oneway
        val oneway =
            if (method.getAnnotationsByType(OneWay::class.java)
                    .isNotEmpty()
            ) "oneway " else ""

        // 返回类型
        val ret = method.returnType.transformService.simpleName()

        // 参数列表
        val parameters = method.parameters.joinToString(", ", "(", ")") { param ->
            when {
                !param.asType().needInOutQualifier() -> ""
                param.getAnnotationsByType(InOut::class.java).isNotEmpty() -> "inout "
                param.getAnnotationsByType(Out::class.java).isNotEmpty() -> "out "
                else -> "in "
            } + param.asType().transformService.simpleName() +
                    " " + param.simpleName
        }

        content.append(INDENTS) // 缩进
            .append(oneway) // oneway
            .append(ret).append(" ") // 返回值
            .append(method.simpleName) // 方法名
            .append(parameters) // 参数列表
            .append(";\n\n") // 结束换行
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
            .map { it.transformServiceName() }
            .sorted() // 给import排个序
            .forEach { element ->
                header.append("\nimport ")
                    .append(element)
                    .append(";")
            }

        // 写入文件
        ProcessUtils.getOutputFile(
            getOutAidlDir(),
            service.packageName, "${service.asType().transformService.simpleName()}.aidl"
        ).save(header.append(content).toString())
    }

    private fun TypeMirror.needInOutQualifier(): Boolean {
        return this is ArrayType || !isBasicType(toString())
    }
}