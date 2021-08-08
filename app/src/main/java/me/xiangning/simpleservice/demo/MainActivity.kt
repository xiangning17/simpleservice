package me.xiangning.simpleservice.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import me.xiangning.simpleservice.SimpleService
import me.xiangning.simpleservice.demo.music.MusicService
import me.xiangning.simpleservice.demo.music.MusicServiceImpl
import me.xiangning.simpleservice.demo.music.MusicServiceImpl2

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.service1).setOnClickListener {
            SimpleService.publishRemoteService(MusicService::class.java, MusicServiceImpl())
        }

        findViewById<Button>(R.id.service2).setOnClickListener {
            SimpleService.publishRemoteService(MusicService::class.java, MusicServiceImpl2())
        }

        findViewById<Button>(R.id.jump).setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))

            it.postDelayed({
                findViewById<Button>(R.id.service2).performClick()
            }, 5000)
        }
    }
}