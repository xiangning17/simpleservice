package me.xiangning.simpleservice.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import java.io.File

/**
 * Created by xiangning on 2021/7/3.
 */
class SimpleServicePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val tasks = project.tasks
        val android = project.extensions.getByName("android") as AppExtension
        project.afterEvaluate {
            configAfterEvaluate(project, android, tasks)
        }
    }

    private fun configAfterEvaluate(project: Project, android: AppExtension, tasks: TaskContainer) {
        android.applicationVariants.onEach { variant ->
            val sourceSet = android.sourceSets.getByName(variant.name)

            val processorOptions = variant.javaCompileOptions.annotationProcessorOptions
            val aidlCompile = variant.aidlCompileProvider.get()

            // app/build/generated/simpleservice/debug
            // app/build/generated/simpleservice/release
            val generated =
                "generated" + File.separator + "simpleservice" + File.separator + variant.dirName

            val outDir = File(project.buildDir, generated)
            val aidlDir = File(outDir, "aidl")
            val sourceDir = File(outDir, "source")

            processorOptions.arguments["simpleservice.outdir"] = outDir.absolutePath
            processorOptions.arguments["simpleservice.outdir.aidl"] = aidlDir.absolutePath
            processorOptions.arguments["simpleservice.outdir.source"] = sourceDir.absolutePath

            // 添加source到java
            sourceSet.java.srcDir(sourceDir)

            val recompileAidl = RecompileAidlAction(aidlDir, sourceSet, aidlCompile)

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