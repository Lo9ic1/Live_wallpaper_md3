package com.example.live_wallpaper_md3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.live_wallpaper_md3.ui.AppNavigation
import com.example.live_wallpaper_md3.ui.theme.Live_wallpaper_md3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Live_wallpaper_md3Theme {
                AppNavigation() // 只负责开启导航
            }
        }
    }
}
