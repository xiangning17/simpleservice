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
object ServiceLocalWrapperGenerator : SourceGenerator {

    private lateinit var service: TypeElement

    private val content = StrBuilder()

    override fun start(service: TypeElement) {
        this.service = service
        content.clear()

        // public class MusicServiceLocalWrapper implements MusicService {
        //    private MusicServiceBinder remote;
        //
        //    public MusicServiceLocal(IBinder binder) {
        //        this.remote = MusicServiceBinder.Stub.asInterface(binder);
        //    }
        //
        //    public MusicServiceLocal(MusicServiceBinder binder) {
        //        this.remote = binder;
        //    }

        val serviceType = service.simpleName.toString()
        val serviceAidlType = serviceType + AidlUtils.AIDL_SUFFIX
        val serviceLocalType = serviceType + AidlUtils.LOCAL_WRAPPER_SUFFIX
        content.append("\n\npublic class ")
            .append(serviceLocalType)
            .append(" implements ").append(service.simpleName)
            .append(" {\n")

        content.append(INDENTS).append("private ").append(serviceAidlType).append(" remote;\n\n")

        content.append(INDENTS).append("public ").append(serviceLocalType)
            .append("(IBinder binder) {\n")
            .append(INDENTS).append(INDENTS).append("this.remote = ")
            .append(serviceAidlType).append(".Stub.asInterface(binder);\n")
            .append(INDENTS).append("}\n\n")

        content.append(INDENTS).append("public ").append(serviceLocalType)
            .append("(").append(serviceAidlType).append(" binder) {\n")
            .append(INDENTS).append(INDENTS).append("this.remote = binder;\n")
            .append(INDENTS).append("}\n\n")
    }

    override fun onMethod(method: ExecutableElement) {
        //    @Override
        //    public boolean play(List<String> names) {
        //        try {
        //            return remote.play(names);
        //        } catch (RemoteException e) {
        //            onRemoteException(e);
        //        }
        //        return false;
        //    }

        // 返回类型
        val ret = method.returnType
        val retDefault = ret.getDefaultValue()

        // 参数列表
        val parameters = method.parameters.joinToString(", ", "(", ")") { param ->
            param.asType().simpleName + " " + param.simpleName
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
            .append("try {\n")
            .append(INDENTS).append(INDENTS).append(INDENTS)
            .append(if (retDefault != null) "return " else "")
            .append("this.remote.").append(method.simpleName)
            .append(method.parameters.joinToString(", ", "(", ");\n") { param ->
                val normalizeName = param.asType().normalizeName
                if (normalizeName.isAidlService()) {
                    "new " + normalizeName.simpleName() + AidlUtils.REMOTE_WRAPPER_SUFFIX + "(" + param.simpleName + ")"
                } else {
                    param.simpleName
                }
            })
            .append(INDENTS).append(INDENTS)
            .append("} catch (RemoteException e) {\n")
            .append(INDENTS).append(INDENTS).append(INDENTS)
            .append("onRemoteException(e);\n")
            .append(INDENTS).append(INDENTS).append("}\n")

        if (retDefault != null) {
            content.append(INDENTS).append(INDENTS)
                .append("return ").append(retDefault).append(";\n")
        }

        content.append(INDENTS).append("}\n\n")
    }

    override fun generate(imports: List<String>) {
        //    public void onRemoteException(Throwable exception) {
        //
        //    }
        content.append(INDENTS)
            .append("public void onRemoteException(Throwable exception) {\n")
            .append(INDENTS).append("}\n\n")
        // 主题结束
        content.append("}")

        // 头部拼接
        val header = StringBuilder()
            .append("// generate by aidl processor at ${DATE_FORMAT.format(System.currentTimeMillis())}\n")
            .append("\npackage ${service.packageName};\n")

        // import声明
        val imported = mutableListOf<String>().apply {
            addAll(imports)
            add("android.os.IBinder")
            add("android.os.RemoteException")
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
        AidlUtils.getOutputFile(
            getOutSourceDir(),
            service.packageName,
            "${service.simpleName.toString() + AidlUtils.LOCAL_WRAPPER_SUFFIX}.java"
        ).save(header.append(content).toString())
    }

}