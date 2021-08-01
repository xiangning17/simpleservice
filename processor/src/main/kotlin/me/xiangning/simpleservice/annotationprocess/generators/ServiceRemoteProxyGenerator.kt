package me.xiangning.simpleservice.annotationprocess.generators

import me.xiangning.simpleservice.IRemoteServiceProxy
import me.xiangning.simpleservice.ServiceManager
import me.xiangning.simpleservice.annotationprocess.ProcessUtils
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.DATE_FORMAT
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.INDENTS
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.getOutSourceDir
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.isAidlService
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.normalizeName
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.packageName
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.save
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.simpleName
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.transformService
import me.xiangning.simpleservice.annotationprocess.ProcessUtils.transformServiceName
import me.xiangning.simpleservice.annotationprocess.ServiceSourceGenerator
import me.xiangning.simpleservice.methoderror.IMethodErrorHandler
import org.gradle.internal.impldep.org.apache.commons.lang.text.StrBuilder
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.NoType

/**
 * Created by xiangning on 2021/7/11.
 */
object ServiceRemoteProxyGenerator : ServiceSourceGenerator {

    private lateinit var service: TypeElement

    private val content = StrBuilder()

    override fun start(service: TypeElement) {
        ServiceRemoteProxyGenerator.service = service
        content.clear()

        // public class MusicServiceRemoteProxy implements MusicService, IRemoteServiceProxy {
        //    private ServiceManager sm;
        //    private MusicServiceBinder remote;
        //    private IMethodErrorHandler handler;
        //
        //    public MusicServiceRemoteProxy(ServiceManager sm, IBinder binder) {
        //        this.sm = sm;
        //        this.handler = sm.getMethodErrorHandler(MusicService.class);
        //        setBinder(binder);
        //    }
        //
        //    @Override
        //    public IBinder asBinder() {
        //        return this.remote.asBinder();
        //    }
        //
        //    @Override
        //    public void setBinder(IBinder binder) {
        //        this.remote = MusicServiceBinder.Stub.asInterface(binder);
        //    }

        val serviceType = service.simpleName.toString()
        val serviceAidlType = serviceType + ProcessUtils.AIDL_SUFFIX
        val serviceLocalType = serviceType + ProcessUtils.REMOTE_PROXY_SUFFIX
        content.append("\n\npublic class ")
            .append(serviceLocalType)
            .append(" implements ").append(service.simpleName)
            .append(", ${IRemoteServiceProxy::class.java.simpleName}")
            .append(" {\n")

        content.append(INDENTS).append("private ServiceManager sm;\n\n")
        content.append(INDENTS).append("private ").append(serviceAidlType).append(" remote;\n")
        content.append(INDENTS).append("private IMethodErrorHandler handler;\n\n")

        content.append(INDENTS).append("public ").append(serviceLocalType)
            .append("(ServiceManager sm, IBinder binder) {\n")
            .append(INDENTS).append(INDENTS).append("this.sm = sm;\n")
            .append(INDENTS).append(INDENTS)
            .append("this.handler = sm.getMethodErrorHandler(${serviceType}.class);\n")
            .append(INDENTS).append(INDENTS).append("setBinder(binder);\n")
            .append(INDENTS).append("}\n\n")

        content.append(INDENTS).append("@Override\n")
            .append(INDENTS).append("public IBinder asBinder() {\n")
            .append(INDENTS).append(INDENTS).append("return this.remote.asBinder();\n")
            .append(INDENTS).append("}\n\n")

        content.append(INDENTS).append("@Override\n")
            .append(INDENTS).append("public void setBinder(IBinder binder) {\n")
            .append(INDENTS).append(INDENTS).append("this.remote = ")
            .append(serviceAidlType).append(".Stub.asInterface(binder);\n")
            .append(INDENTS).append("}\n\n")
    }

    override fun onMethod(method: ExecutableElement) {
        //    @Override
        //    public boolean play(List<String> names) {
        //        try {
        //            return remote.play(names);
        //        } catch (RemoteException e) {
        //            return (boolean) this.handler.onMethodError(e, boolean.class, "play", names);
        //        }
        //    }

        // 返回类型
        val ret = method.returnType
        val needReturn = ret !is NoType
        val returnType = ret.transformService.simpleName()

        // 参数列表
        val parameters = method.parameters.joinToString(", ", "(", ")") { param ->
            param.asType().simpleName + " " + param.simpleName
        }

        content.append(INDENTS).append("@Override\n")
            .append(INDENTS) // 缩进
            .append(method.modifiers.filter { it != Modifier.ABSTRACT }.joinToString(" "))
            .append(" ")
            .append(returnType).append(" ") // 返回值
            .append(method.simpleName) // 方法名
            .append(parameters) // 参数列表
            .append(" {\n") // 结束换行

        content.append(INDENTS).append(INDENTS)
            .append("try {\n")
            .append(INDENTS).append(INDENTS).append(INDENTS)
            .append(if (needReturn) "return " else "")
            .append("this.remote.").append(method.simpleName)
            .append(method.parameters.joinToString(", ", "(", ");\n") { param ->
                val normalizeName = param.asType().normalizeName
                if (normalizeName.isAidlService()) {
                    "this.sm.getServiceRemote(${param.simpleName})"
                } else {
                    param.simpleName
                }
            })
            .append(INDENTS).append(INDENTS)
            .append("} catch (Throwable e) {\n")
            .append(INDENTS).append(INDENTS).append(INDENTS)
            .append(if (needReturn) "return ($returnType) " else "")
            .append("this.handler.onMethodError(e, $returnType.class")
            .append(", \"").append(method.simpleName).append("\"")
            .append(", ").append(method.parameters.joinToString(", "))
            .append(");\n")
            .append(INDENTS).append(INDENTS).append("}\n")

        content.append(INDENTS).append("}\n\n")
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
            add("android.os.IBinder")
            add(IRemoteServiceProxy::class.java.canonicalName)
            add(IMethodErrorHandler::class.java.canonicalName)
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
            "${service.simpleName.toString() + ProcessUtils.REMOTE_PROXY_SUFFIX}.java"
        ).save(header.append(content).toString())
    }

}