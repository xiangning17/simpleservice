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
        println("recompile aidl $scene, ${aidlDir.listFiles()?.toList()}")
        if (!done && aidlDir.exists() && !aidlDir.listFiles().isNullOrEmpty()) {
            sourceSet.aidl.srcDir(aidlDir)
            aidlTask.taskAction()
            println("recompile aidl done: $scene")
            done = true
        }
    }
}