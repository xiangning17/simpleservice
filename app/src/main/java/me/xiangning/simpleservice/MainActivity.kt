package me.xiangning.simpleservice

//import me.xiangning.simpleservice.music.asRemoteWrapper
//import me.xiangning.simpleservice.music.asMusicService
//import me.xiangning.simpleservice.music.asRemoteWrapper
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.xiangning.simpleservice.music.MusicServiceImpl
import me.xiangning.simpleservice.service.ServiceManager

class MainActivity : AppCompatActivity() {

    private lateinit var sm: ServiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val musicService = MusicServiceImpl()

//        sm.publishService("music", musicService.asRemoteWrapper())
//        sm.getService("music")!!
    }
}