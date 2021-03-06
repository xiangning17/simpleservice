package me.xiangning.simpleservice.demo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.xiangning.simpleservice.OnRemoteServiceBind
import me.xiangning.simpleservice.SimpleService
import me.xiangning.simpleservice.demo.music.MusicService
import kotlin.concurrent.thread

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        findViewById<Button>(R.id.button).setOnClickListener {
            thread {
                val service =
                    SimpleService.getRemoteServiceWait(MusicService::class.java) ?: return@thread
                service.play("同步获取！")
            }
        }

        Log.e("MainActivity2", "start bind")
        SimpleService.bindRemoteService(
            MusicService::class.java,
            object : OnRemoteServiceBind<MusicService> {

                override fun onBindSuccess(service: MusicService) {
                    Log.e("MainActivity2", "onBindSuccess: $service")
                    GlobalScope.launch {
                        service.play("千里之外")
                        delay(2000)
                        service.download("城里的月光") {
                            Log.e("MainActivity2", "progress: $it")
                        }

                    }
                }

                override fun onBindFailed(error: Throwable?) {
                    Log.e("MainActivity2", "onBindFailed: $error", error)
                }
            })
    }

}