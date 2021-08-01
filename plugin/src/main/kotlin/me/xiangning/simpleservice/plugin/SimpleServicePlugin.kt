package me.xiangning.simpleservice.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import java.io.File

/**
 * Created by xiangning on 2021/7/3.
 */
class SimpleServicePlugin : Plugin<Project> {

    private lateinit var project: Project
    private lateinit var tasks: TaskContainer
    private lateinit var android: BaseExtension

    private val outDirsByVariant = mutableMapOf<String, File>()

    private val File.aidl: File
        get() = File(this, "aidl")

    private val File.source: File
        get() = File(this, "source")

    override fun apply(project: Project) {
        this.project = project
        tasks = project.tasks
        android = project.extensions.getByName("android") as BaseExtension
        config()
        project.afterEvaluate {
            configAfterEvaluate()
        }
    }

    private fun getVariants(): DomainObjectSet<BaseVariant> {
        val android = this.android
        if (android is AppExtension) {
            return android.applicationVariants as DomainObjectSet<BaseVariant>
        } else if (android is LibraryExtension) {
            return android.libraryVariants as DomainObjectSet<BaseVariant>
        } else {
            throw RuntimeException("找不到‘android’扩展，请确保只在Android模块中启用SimpleService插件")
        }
    }

    private fun config() {
        getVariants().all { variant ->
            // app/build/generated/simpleservice/debug
            // app/build/generated/simpleservice/release
            val generated =
                "generated" + File.separator + "simpleservice" + File.separator + variant.dirName
            outDirsByVariant[variant.name] = File(project.buildDir, generated).also { outDir ->
                // 添加source子目录到源码路径
                android.sourceSets.getByName(variant.name).java.srcDir(outDir.source)
            }
        }
    }

    private fun configAfterEvaluate() {
        getVariants().onEach { variant ->
            val sourceSet = android.sourceSets.getByName(variant.name)

            val processorOptions = variant.javaCompileOptions.annotationProcessorOptions
            val aidlCompile = variant.aidlCompileProvider.get()

            val outDir = outDirsByVariant[variant.name]
            if (outDir == null) {
                println("SimpleServicePlugin Error: outDir[${variant.name}] is null")
                return@onEach
            }
            processorOptions.arguments["simpleservice.outdir"] = outDir.absolutePath
            processorOptions.arguments["simpleservice.outdir.aidl"] = outDir.aidl.absolutePath
            processorOptions.arguments["simpleservice.outdir.source"] = outDir.source.absolutePath

            val recompileAidl = RecompileAidlAction(outDir.aidl, sourceSet, aidlCompile)

            val compileKotlin = tasks.findByName("compile" + variant.name.capitalize() + "Kotlin")
            compileKotlin?.doFirst {
                recompileAidl.recompileAidl("before compileKotlin")
            }

            val compileJavac = variant.javaCompileProvider.get()
            compileJavac.doLast {
                recompileAidl.recompileAidl("after compileJavac")
            }

        }
    }
}