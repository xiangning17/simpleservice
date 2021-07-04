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
            val generated = "generated" + File.separator + "simpleservice" + File.separator + variant.dirName

            val aidlDir = generated + File.separator + "aidl"
            val absoluteAidl = File(project.buildDir, aidlDir)
            processorOptions.arguments["simpleservice.outdir.aidl"] = absoluteAidl.absolutePath

            val recompileAidl = RecompileAidlAction(absoluteAidl, sourceSet, aidlCompile)

            val compileKotlin = tasks.findByName("compile" + variant.name.capitalize() + "Kotlin")
            compileKotlin?.doFirst {
                recompileAidl.recompileAidl("before compileKotlin")
            }

            val compileJavac = variant.javaCompileProvider.get()
            compileJavac.doLast {
                recompileAidl.recompileAidl("after compileJavac")
            }
//
//            val target = compileKotlin ?: compileJavac
//
//            // 在编译之前插入重新编译aidl的任务
//            target.doFirst {
//                android.sourceSets.getByName(variant.name).aidl.srcDir(aidlDir)
//                aidl.taskAction()
//            }


//            val compileAidl = tasks.findByName("compile" + name + "Aidl")
//            if (compileAidl == null) {
//                println("not found compileAidl")
//                return@onEach
//            }

//            val compileJavac = tasks.findByName("compile" + name + "JavaWithJavac")
//            if (compileJavac is JavaCompile) {
//                println("found compileJavac: $compileJavac")
//
//                compileJavac.doLast {
//
//                    variant.sourceSets.find { it.name == "main" }?.let {
//                        it.aidlDirectories.add(File())
//                    }
//                    android.sourceSets.getByName("main") {
//                        it.aidl.srcDir("build/generated/aidl")
//                    }
//
//                    println("after compileJavac compile aidl")
//                    compileAidl.actions.forEach { action ->
//                        action.execute(compileAidl)
//                    }
//                }
//
//            }
        }
    }
}