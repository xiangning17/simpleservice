package me.xiangning.annotation.processor

import com.google.auto.service.AutoService
import me.xiangning.annotation.Aidl
import me.xiangning.annotation.InOut
import me.xiangning.annotation.OneWay
import me.xiangning.annotation.Out
import java.io.File
import java.text.SimpleDateFormat
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.*
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@AutoService(Processor::class)
class AidlProcess : AbstractProcessor() {

    private lateinit var messenger: Messager
    private lateinit var elements: Elements
    private lateinit var types: Types
    private lateinit var filer: Filer
    private lateinit var options: Map<String, String>

    private lateinit var stringType: TypeMirror
    private lateinit var listType: TypeMirror
    private lateinit var mapType: TypeMirror
    private lateinit var basicTypes: List<TypeMirror>

    private val currentServices = mutableSetOf<String>()

    companion object {
        const val INDENTS = "    "
        private val REX_TO_SIMPLE = Regex("[_a-zA-Z0-9.]+\\.([_a-zA-Z0-9]+)")
        private val REX_REMOVE_WILDCARD = Regex("\\? (extends|super) ")
        private val DATE_FORMAT = SimpleDateFormat("YYYY/MM/dd HH:mm:ss")

        private val AIDL_SUFFIX = "Binder"
    }

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messenger = processingEnv.messager
        elements = processingEnv.elementUtils
        types = processingEnv.typeUtils
        filer = processingEnv.filer
        options = processingEnv.options

        messenger.printMessage(
            Diagnostic.Kind.WARNING,
            "\noptions = ${processingEnv.options}\n"
        )

        loadBasicTypes()
    }

    private fun loadBasicTypes() {
        stringType = String::class.java.asType()
        listType = List::class.java.asType()
        mapType = Map::class.java.asType()

        basicTypes = mutableListOf(
            stringType,
            types.getPrimitiveType(TypeKind.BOOLEAN),
            types.getPrimitiveType(TypeKind.BYTE),
            types.getPrimitiveType(TypeKind.SHORT),
            types.getPrimitiveType(TypeKind.INT),
            types.getPrimitiveType(TypeKind.LONG),
            types.getPrimitiveType(TypeKind.CHAR),
            types.getPrimitiveType(TypeKind.FLOAT),
            types.getPrimitiveType(TypeKind.DOUBLE),
        ).apply {
            addAll(this.map { types.getArrayType(it) })
        }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Aidl::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        val annotated = roundEnv.getElementsAnnotatedWith(Aidl::class.java)
        val services = annotated.filter { it.kind.isInterface }
            .filterIsInstance<TypeElement>()
        val parcelables = annotated.filterIsInstance<TypeElement>()
            .filter { type ->
                type.kind.isClass && type.interfaces.find {
                    it.simpleName == "Parcelable"
                } != null
            }

        if (roundEnv.rootElements.isEmpty()) {
            return false
        }

        // 暂存当前服务
        currentServices.addAll(services.map { it.qualifiedName.toString() })

        messenger.printMessage(Diagnostic.Kind.WARNING, "\nroot element: ${roundEnv.rootElements}\n")
        messenger.printMessage(Diagnostic.Kind.WARNING, "\nannotations: $annotations\n")
        messenger.printMessage(Diagnostic.Kind.WARNING, "\nservices: $services\n")
        messenger.printMessage(Diagnostic.Kind.WARNING, "\nparcelables: $parcelables\n")

        // 为service生成aidl
        services.forEach { generateAidl(it) }

        // 为parcelable生成aidl文件
        parcelables.forEach { parcelable ->
            val pkg = parcelable.packageName
            val name = parcelable.asType().simpleName
            val content = StringBuilder()
                .append("// generate by aidl processor at ${DATE_FORMAT.format(System.currentTimeMillis())}\n\n")
                .append("package $pkg;\n")
                .append("\nparcelable ").append(name)
                .append(";\n")

            getOutputFile(
                options["simpleservice.outdir.aidl"],
                pkg, "${name}.aidl"
            ).save(content.toString())
        }

        currentServices.clear()
        return true
    }

    private fun generateAidl(service: TypeElement) {
        // 收集import类型
        val imported = mutableSetOf<String>()
        fun recordImport(type: TypeMirror) {
//            if (type.needImport()) {
                val norName = type.normalizeName
                messenger.printMessage(Diagnostic.Kind.WARNING, "\nrecord type: $type -> $norName")
                REX_TO_SIMPLE.findAll(norName).forEach {
                    val cls = it.value
                    if (needImport(cls)) {
                        imported.add(cls)
                    }
                }
//                if (type is ArrayType) {
//                    recordImport(type.componentType)
//                } else if (type is DeclaredType) {
//                    (type.asElement() as? TypeElement)?.let {
//                        imported.add(it.qualifiedName.toString())
//                    }
//
//                    type.typeArguments.forEach { recordImport(it) }
//                } else if (type is Type.CapturedType) {
//                }
//            }
        }

        // 主体内容拼接
        val body = StringBuilder()
        // 接口声明头部
        body.append("\n\ninterface ")
            .append(service.simpleName)
            .append("$AIDL_SUFFIX {\n")

        // 拼接方法
        service.enclosedElements
            .filter { it.kind == ElementKind.METHOD }
            .map { it as ExecutableElement }
            .forEach { method ->
                // 是否是oneway
                val oneway =
                    if (method.getAnnotationsByType(OneWay::class.java)
                            .isNotEmpty()
                    ) "oneway " else ""

                // 返回类型
                val ret = method.returnType.also { recordImport(it) }.simpleName

                // 参数列表
                val parameters = method.parameters.joinToString(", ", "(", ")") { param ->
                    recordImport(param.asType())
                    when {
                        !param.asType().needInOutQualifier() -> ""
                        param.getAnnotationsByType(InOut::class.java).isNotEmpty() -> "inout "
                        param.getAnnotationsByType(Out::class.java).isNotEmpty() -> "out "
                        else -> "in "
                    } + param.asType().simpleName + " "  + param.simpleName
                }

                body.append(INDENTS) // 缩进
                    .append(oneway) // oneway
                    .append(ret).append(" ") // 返回值
                    .append(method.simpleName) // 方法名
                    .append(parameters) // 参数列表
                    .append(";\n\n") // 结束换行
            }

        // 主体结束
        body.append("}")

        // 头部拼接
        val header = StringBuilder()
            .append("// generate by aidl processor at ${DATE_FORMAT.format(System.currentTimeMillis())}\n")
            .append("\npackage ${service.packageName};\n")

        // import声明
        imported.asSequence()
            .sorted() // 给import排个序
            .forEach { element ->
                header.append("\nimport ")
                    .append(element)
                    .append(";")
            }

        // 写入文件
        getOutputFile(
            options["simpleservice.outdir.aidl"],
            service.packageName, "${service.simpleName}$AIDL_SUFFIX.aidl"
        ).save(header.append(body).toString())
    }

    private fun transformParameterElement(parameter: Element) {
//        val new = elements.
    }

    private fun getOutputFile(outputDir: String?, pkg: String, name: String): File {
        val path = if (outputDir != null) {
            File(outputDir, pkg.replace(".", File.separator) + File.separator + name).absolutePath
        } else {
            filer.getResource(StandardLocation.SOURCE_OUTPUT, pkg, name).name
        }
        return File(path)
    }
    /*=============================Type相关扩展====================================*/

    private val TypeElement.packageName: String
        get() = elements.getPackageOf(this).toString()

    private val TypeMirror.normalizeName: String
        get() = REX_REMOVE_WILDCARD.replace(toString(), "")
            .transformServiceName()

    private val TypeMirror.simpleName: String
        get() = REX_TO_SIMPLE.replace(normalizeName, "$1")
            // 防止‘List<List<String>>’中的‘>>’导致的编译错误
            .replace(">>", "> >")

    private fun String.transformServiceName(): String {
        return REX_TO_SIMPLE.replace(this) {
            if (currentServices.contains(it.value)) {
                it.value + AIDL_SUFFIX
            } else {
                it.value
            }
        }
    }

    private fun TypeMirror.toSimpleName(): String {
        val builder = StringBuilder()
        visitTypeWithTypeArgs { types ->
            when (types.size) {
                1 -> {
                    val type = types[0]
                    if (type is ArrayType) {
                        builder.append("[]")
                    } else {
                        if (builder.isEmpty()) {
                            builder.append(type.toSimpleName())
                        }
                    }

                }
                else -> {
                    val type = types[0]
                }
            }
            if (types.first() is ArrayType) {
                builder.append("[]")
                return@visitTypeWithTypeArgs
            }


        }
        return ""
    }

    private fun needImport(cls: String): Boolean {
        return cls != "void" && !basicTypes.any {
            it.toString() == cls
        }
    }

    private fun TypeMirror.needInOutQualifier(): Boolean {
        return this is ArrayType || needImport(toString())
    }

    private fun TypeMirror.visitTypeWithTypeArgs(visitor: (List<TypeMirror>) -> Unit) {
        when (this) {
            is ArrayType -> {
                visitor(listOf(this.componentType))
            }
            is DeclaredType -> {
                if (typeArguments.isNotEmpty()) {
                    visitor(typeArguments)
                }
            }
        }

        visitor(listOf(this))
    }

    private fun Class<*>.asType(): TypeMirror =
        elements.getTypeElement(this.canonicalName).asType()

    private fun TypeMirror.asElement() = types.asElement(this)

}