package com.example.live_wallpaper_md3.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.live_wallpaper_md3.data.WallpaperDataStore
import com.example.live_wallpaper_md3.ui.components.VideoItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(playlistName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val dataStore = remember { WallpaperDataStore(context) }
    
    var videoList by remember { mutableStateOf(listOf<String>()) }
    var volume by remember { mutableStateOf(1.0f) }
    var isRandom by remember { mutableStateOf(false) }

    LaunchedEffect(playlistName) {
        videoList = dataStore.getVideosInPlaylist(playlistName)
        volume = dataStore.getVolume()
        isRandom = dataStore.isRandom()
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val newList = videoList + uri.toString()
            videoList = newList
            dataStore.saveVideosToPlaylist(playlistName, newList)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlistName) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { pickVideoLauncher.launch(arrayOf("video/*")) }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // 全局设置卡片
            ElevatedCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("音量")
                        Slider(value = volume, onValueChange = { volume = it }, onValueChangeFinished = { dataStore.saveSettings(volume, isRandom) })
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("随机")
                        Switch(checked = isRandom, onCheckedChange = { isRandom = it; dataStore.saveSettings(volume, isRandom) })
                    }
                }
            }

            if (videoList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("请添加视频") }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(videoList) { uriString ->
                        VideoItemCard(
                            uriString = uriString,
                            onDelete = {
                                val newList = videoList.toMutableList().apply { remove(uriString) }
                                videoList = newList
                                dataStore.saveVideosToPlaylist(playlistName, newList)
                            }
                        )
                    }
                }
            }
        }
    }
}
