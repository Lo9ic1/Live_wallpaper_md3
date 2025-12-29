package com.example.live_wallpaper_md3.service

import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.example.live_wallpaper_md3.data.WallpaperDataStore
import kotlin.random.Random

class VideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = VideoEngine()

    inner class VideoEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null
        private var videoList: List<String> = emptyList()
        private var currentVideoIndex = 0
        private lateinit var dataStore: WallpaperDataStore // 使用 DataStore

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            dataStore = WallpaperDataStore(applicationContext) // 初始化
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setSurface(holder.surface)
            mediaPlayer?.setOnCompletionListener { playNextVideo() }
            mediaPlayer?.setOnErrorListener { _, _, _ -> playNextVideo(); true }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                refreshState()
                if (videoList.isNotEmpty() && mediaPlayer?.isPlaying == false) {
                    playVideo(videoList[currentVideoIndex])
                } else if (mediaPlayer?.isPlaying == true) {
                    val vol = dataStore.getVolume()
                    mediaPlayer?.setVolume(vol, vol)
                }
            } else {
                mediaPlayer?.pause()
            }
        }

        private fun refreshState() {
            // 所有的 SharedPreferences 逻辑都被 DataStore 接管了，这里看起来很清爽
            val activeName = dataStore.getActivePlaylistName()
            val volume = dataStore.getVolume()
            
            // 重新加载列表逻辑...
            val newVideoList = dataStore.getVideosInPlaylist(activeName)
            
            // 简单的列表比对逻辑 (实际开发可以用更高效的 DiffUtil，这里简化)
            if (newVideoList != videoList) {
                videoList = newVideoList
                currentVideoIndex = 0
                if (mediaPlayer?.isPlaying == true) playNextVideo()
            }
        }

        private fun playNextVideo() {
            if (videoList.isEmpty()) return
            
            if (dataStore.isRandom()) {
                if (videoList.size > 1) {
                    var newIndex = currentVideoIndex
                    while (newIndex == currentVideoIndex) newIndex = Random.nextInt(videoList.size)
                    currentVideoIndex = newIndex
                }
            } else {
                currentVideoIndex = (currentVideoIndex + 1) % videoList.size
            }
            playVideo(videoList[currentVideoIndex])
        }

        private fun playVideo(uriString: String) {
            try {
                mediaPlayer?.reset()
                mediaPlayer?.setSurface(surfaceHolder.surface)
                mediaPlayer?.setDataSource(applicationContext, Uri.parse(uriString))
                val vol = dataStore.getVolume()
                mediaPlayer?.setVolume(vol, vol)
                mediaPlayer?.isLooping = (videoList.size == 1)
                mediaPlayer?.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
            } catch (e: Exception) {
                e.printStackTrace()
                playNextVideo() // 出错播下一个
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}
