package me.xiangning.simpleservice.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import me.xiangning.simpleservice.OnRemoteServiceBind
import me.xiangning.simpleservice.SimpleService
import me.xiangning.simpleservice.demo.music.MusicService
import kotlin.concurrent.thread

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        Log.e("MainActivity2", "start bind")
        SimpleService.bindRemoteService(
            MusicService::class.java,
            object : OnRemoteServiceBind<MusicService> {

                override fun onBindSuccess(service: MusicService) {
                    Log.e("MainActivity2", "onBindSuccess: $service")
                    thread {
                        service.play("千里之外")
                        Thread.sleep(2000)
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