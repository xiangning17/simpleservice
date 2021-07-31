package me.xiangning.simpleservice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.xiangning.simpleservice.music.MusicServiceImpl
import me.xiangning.simpleservice.music.MusicServiceRemote
import me.xiangning.simpleservice.service.ServiceManager

class MainActivity : AppCompatActivity() {

    private lateinit var sm: ServiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val musicService = MusicServiceImpl()

        sm.publishService(
            "music",
            MusicServiceRemote(musicService)
        )
        sm.getService("music")!!
    }
}