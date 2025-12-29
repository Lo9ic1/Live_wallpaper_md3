package com.example.live_wallpaper_md3.ui.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.live_wallpaper_md3.data.WallpaperDataStore
import com.example.live_wallpaper_md3.service.VideoWallpaperService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(onPlaylistClick: (String) -> Unit) {
    val context = LocalContext.current
    // 使用我们封装好的 DataStore
    val dataStore = remember { WallpaperDataStore(context) }
    
    var playlists by remember { mutableStateOf(listOf<String>()) }
    var activePlaylist by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // 初始化数据
    LaunchedEffect(Unit) {
        playlists = dataStore.getAllPlaylists()
        activePlaylist = dataStore.getActivePlaylistName()
    }

    fun updatePlaylists() {
        playlists = dataStore.getAllPlaylists()
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("新建列表") },
            text = {
                OutlinedTextField(value = newPlaylistName, onValueChange = { newPlaylistName = it }, label = { Text("名称") })
            },
            confirmButton = {
                Button(onClick = {
                    if (newPlaylistName.isNotEmpty() && !playlists.contains(newPlaylistName)) {
                        val newList = playlists + newPlaylistName
                        dataStore.saveAllPlaylists(newList)
                        // 如果是第一个，自动激活
                        if (newList.size == 1) {
                            dataStore.setActivePlaylistName(newPlaylistName)
                            activePlaylist = newPlaylistName
                        }
                        updatePlaylists()
                    }
                    newPlaylistName = ""
                    showCreateDialog = false
                }) { Text("创建") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("取消") } }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("我的壁纸库") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("新建列表") }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // 重启壁纸按钮区
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp).clickable {
                    try {
                        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(context, VideoWallpaperService::class.java))
                        context.startActivity(intent)
                    } catch (e: Exception) { Toast.makeText(context, "无法打开设置", Toast.LENGTH_SHORT).show() }
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("应用 / 重启壁纸")
                }
            }

            if (playlists.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无列表") }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(playlists) { name ->
                        val isActive = (activePlaylist == name)
                        ElevatedCard(
                            onClick = { onPlaylistClick(name) },
                            modifier = Modifier.height(160.dp).fillMaxWidth(),
                            colors = if (isActive) CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.elevatedCardColors()
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.Folder, null, modifier = Modifier.size(80.dp).align(Alignment.Center).padding(bottom=20.dp), tint = if(isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha=0.5f))
                                Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                                    Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                    if (isActive) Text("使用中", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(modifier = Modifier.align(Alignment.TopEnd), onClick = {
                                    val newList = playlists.toMutableList().apply { remove(name) }
                                    dataStore.saveAllPlaylists(newList)
                                    dataStore.removePlaylistData(name)
                                    updatePlaylists()
                                }) { Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error) }
                                
                                if (!isActive) {
                                    IconButton(modifier = Modifier.align(Alignment.BottomEnd), onClick = {
                                        dataStore.setActivePlaylistName(name)
                                        activePlaylist = name
                                        Toast.makeText(context, "已激活: $name", Toast.LENGTH_SHORT).show()
                                    }) { Icon(Icons.Default.Check, null) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
