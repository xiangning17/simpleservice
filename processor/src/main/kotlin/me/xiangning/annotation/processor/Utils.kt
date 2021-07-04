package me.xiangning.annotation.processor

import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Created by xiangning on 2021/7/3.
 */
@Throws(IOException::class)
fun File.save(content: String) {
    if (!this.exists()) {
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        createNewFile()
    }

    try {
        FileWriter(this).use { writer ->
            writer.write(content)
            writer.flush()
        }
    } catch (e: Exception) {
        try {
            this.delete()
        } catch (ignored: Exception) {
        }
        throw e
    }
}