package me.xiangning.simpleservice.demo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import me.xiangning.simpleservice.SimpleService
import me.xiangning.simpleservice.demo.music.MusicService
import me.xiangning.simpleservice.demo.music.MusicServiceImpl

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val musicService = MusicServiceImpl()
        Log.e("MainActivity", "publish service")
        SimpleService.publishRemoteService(MusicService::class.java, musicService)
        startActivity(Intent(this, MainActivity2::class.java))
    }
}