package me.xiangning.annotation.processor.generator

import me.xiangning.annotation.processor.AidlUtils.DATE_FORMAT
import me.xiangning.annotation.processor.AidlUtils.getOutAidlDir
import me.xiangning.annotation.processor.AidlUtils.getOutputFile
import me.xiangning.annotation.processor.AidlUtils.packageName
import me.xiangning.annotation.processor.AidlUtils.save
import me.xiangning.annotation.processor.SourceGenerator
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Created by xiangning on 2021/7/11.
 */
object ParcelableAidlGenerator : SourceGenerator {

    private lateinit var parcelable: TypeElement

    override fun start(service: TypeElement) {
        this.parcelable = service
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