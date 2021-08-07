package me.xiangning.simpleservice.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
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
            SimpleService.publishRemoteService(MusicService::class.java, MusicServiceImpl()) {
                Toast.makeText(
                    this,
                    "music service 1 publish ${if (it == null) "success" else "failed"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        findViewById<Button>(R.id.service2).setOnClickListener {
            SimpleService.publishRemoteService(MusicService::class.java, MusicServiceImpl2()) {
                Toast.makeText(
                    this,
                    "music service 2 publish ${if (it == null) "success" else "failed"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        findViewById<Button>(R.id.jump).setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))

            it.postDelayed({
                findViewById<Button>(R.id.service2).performClick()
            }, 5000)
        }
    }
}