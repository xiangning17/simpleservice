package me.xiangning.simpleservice.plugin

import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.tasks.AidlCompile
import java.io.File

/**
 * Created by xiangning on 2021/7/4.
 */
class RecompileAidlAction(
    private val aidlDir: File,
    private val sourceSet: AndroidSourceSet,
    private val aidlTask: AidlCompile
) {

    private var done = false

    fun recompileAidl(scene: String) {
        if (!done && aidlDir.exists() && !aidlDir.listFiles().isNullOrEmpty()) {
            sourceSet.aidl.srcDir(aidlDir)
            try {
                aidlTask.taskAction()
                println("recompile aidl $scene, with taskAction")
            } catch (e: Throwable) {
                aidlTask.javaClass.methods.find { mtd ->
                    mtd.annotations.find {
                        it.annotationClass.simpleName == "TaskAction"
                    } != null
                }?.let { method ->
                    method.isAccessible = true
                    method.invoke(aidlTask)
                    println("recompile aidl $scene, with ${method.name}")
                }
            }

            println("recompile aidl done: $scene")
            done = true
        }
    }
}