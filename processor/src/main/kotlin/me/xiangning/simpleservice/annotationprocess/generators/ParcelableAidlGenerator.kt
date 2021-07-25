package me.xiangning.simpleservice.annotationprocess.generators

import me.xiangning.simpleservice.annotationprocess.ProcessUtils.DATE_FORMAT
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.getOutAidlDir
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.getOutputFile
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.packageName
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.save
import me.xiangning.simpleservice.annotationprocess.ServiceSourceGenerator
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Created by xiangning on 2021/7/11.
 */
object ParcelableAidlGenerator : ServiceSourceGenerator {

    private lateinit var parcelable: TypeElement

    override fun start(service: TypeElement) {
        parcelable = service
    }

    override fun onMethod(method: ExecutableElement) {

    }

    override fun generate(imports: List<String>) {
        val pkg = parcelable.packageName
        val name = parcelable.simpleName
        val content = StringBuilder()
            .append("// generate by aidl processor at ${DATE_FORMAT.format(System.currentTimeMillis())}\n\n")
            .append("package $pkg;\n")
            .append("\nparcelable ").append(name)
            .append(";\n")

        getOutputFile(getOutAidlDir(), pkg, "${name}.aidl")
            .save(content.toString())
    }
}