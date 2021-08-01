package me.xiangning.simpleservice.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.xiangning.simpleservice.ServiceManager
import me.xiangning.simpleservice.demo.music.MusicServiceImpl

class MainActivity : AppCompatActivity() {

    private lateinit var sm: ServiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val musicService = MusicServiceImpl()

//        sm.publishService(
//            "music",
//            MusicServiceRemote(musicService)
//        )
//        sm.getService("music")!!
    }
}