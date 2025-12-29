package com.example.live_wallpaper_md3.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import com.example.live_wallpaper_md3.ui.screens.PlaylistScreen
import com.example.live_wallpaper_md3.ui.screens.VideoDetailScreen

enum class Screen { PlaylistList, VideoDetail }

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.PlaylistList) }
    var selectedPlaylistName by remember { mutableStateOf("") }

    BackHandler(enabled = currentScreen == Screen.VideoDetail) {
        currentScreen = Screen.PlaylistList
    }

    if (currentScreen == Screen.PlaylistList) {
        PlaylistScreen(
            onPlaylistClick = { name ->
                selectedPlaylistName = name
                currentScreen = Screen.VideoDetail
            }
        )
    } else {
        VideoDetailScreen(
            playlistName = selectedPlaylistName,
            onBack = { currentScreen = Screen.PlaylistList }
        )
    }
}
