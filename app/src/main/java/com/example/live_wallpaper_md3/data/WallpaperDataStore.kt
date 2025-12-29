package com.example.live_wallpaper_md3.data

import android.content.Context
import android.content.SharedPreferences

/**
 * 专门负责数据存储的类
 * 所有的 SharedPreferences 操作都在这里完成，其他地方只管调用
 */
class WallpaperDataStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)

    // --- 基础配置 ---
    fun saveSettings(volume: Float, isRandom: Boolean) {
        prefs.edit()
            .putFloat("video_volume", volume)
            .putBoolean("is_random", isRandom)
            .apply()
    }

    fun getVolume(): Float = prefs.getFloat("video_volume", 1.0f)
    fun isRandom(): Boolean = prefs.getBoolean("is_random", false)

    // --- 播放列表管理 ---
    fun getAllPlaylists(): List<String> {
        val savedString = prefs.getString("all_playlists", "")
        return if (savedString.isNullOrEmpty()) emptyList() else savedString.split("###")
    }

    fun saveAllPlaylists(list: List<String>) {
        prefs.edit().putString("all_playlists", list.joinToString("###")).apply()
    }

    fun getActivePlaylistName(): String = prefs.getString("active_playlist_name", "") ?: ""

    fun setActivePlaylistName(name: String) {
        prefs.edit().putString("active_playlist_name", name).apply()
    }

    // --- 单个列表内的视频管理 ---
    fun getVideosInPlaylist(playlistName: String): List<String> {
        val key = "video_list_$playlistName"
        val savedString = prefs.getString(key, "")
        return if (savedString.isNullOrEmpty()) emptyList() else savedString.split("###")
    }

    fun saveVideosToPlaylist(playlistName: String, videos: List<String>) {
        val key = "video_list_$playlistName"
        prefs.edit().putString(key, videos.joinToString("###")).apply()
    }

    fun removePlaylistData(playlistName: String) {
        val key = "video_list_$playlistName"
        prefs.edit().remove(key).apply()
    }
}
