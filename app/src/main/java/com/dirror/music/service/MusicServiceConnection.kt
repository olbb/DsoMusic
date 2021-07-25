package com.dirror.music.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.dirror.music.MyApp
import com.dirror.music.MyApp.Companion.musicController
import com.dirror.music.music.standard.data.StandardSongData
import com.dirror.music.room.toSongList
import com.dirror.music.util.Config
import com.dirror.music.util.runOnMainThread
import kotlin.concurrent.thread

/**
 * 音乐服务连接
 */
class MusicServiceConnection : ServiceConnection {

    /**
     * 服务连接后
     */
    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        if (p1 !is MusicService.MusicController) {
            return
        }
        musicController.value = p1
        thread {
            // 恢复 SongData
            val recoverSong = MyApp.mmkv.decodeParcelable(Config.SERVICE_CURRENT_SONG, StandardSongData::class.java)
            val recoverProgress = MyApp.mmkv.decodeInt(Config.SERVICE_RECOVER_PROGRESS, 0)
            val recoverPlayQueue = MyApp.appDatabase.playQueueDao().loadAll().toSongList()
            recoverSong?.let { song ->
                // recover = true
                if (recoverSong in recoverPlayQueue) {
                    runOnMainThread {
                        musicController.value?.let {
                            val resumePlay = MyApp.mmkv.decodeBool(Config.RESUME_PLAY, false)
                            it.setRecover(!resumePlay)
                            it.setRecoverProgress(recoverProgress)
                            it.setPlaylist(recoverPlayQueue)
                            it.playMusic(song)
                        }
                    }
                }
            }
        }
    }

    /**
     * 服务意外断开连接
     */
    override fun onServiceDisconnected(p0: ComponentName?) {
        musicController.value = null
    }

}