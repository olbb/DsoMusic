package com.dirror.music.service

import android.content.ContentUris
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.dirror.music.MyApp
import com.dirror.music.data.LyricViewData
import com.dirror.music.music.kuwo.SearchSong
import com.dirror.music.music.netease.SongUrl
import com.dirror.music.music.qq.PlayUrl
import com.dirror.music.music.standard.SearchLyric
import com.dirror.music.music.standard.data.*
import com.dirror.music.util.Api
import com.dirror.music.util.runOnMainThread
import com.dirror.music.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 获取歌曲 URL
 */
object ServiceSongUrl {

    const val TAG = "ServiceSongUrl"

    inline fun getUrlProxy(song: StandardSongData, crossinline success: (Any?) -> Unit) {
        getUrl(song) {
            GlobalScope.launch { withContext(Dispatchers.Main) {
                success.invoke(it)
            } }

        }
    }

    inline fun getUrl(song: StandardSongData, crossinline success: (Any?) -> Unit) {
        when (song.source) {
            SOURCE_NETEASE -> {
                GlobalScope.launch {
                    var url = ""
                    Api.getFromKuWo(song)?.let { kuwo ->
                        Log.d(TAG, "search from kuwo get $kuwo")
                        url = SearchSong.getUrl(kuwo.id?:"")
                        if (url.isNotEmpty()) {
                            toast("${song.name} 替换酷我成功")
                        }
                    }
                    if (url.isEmpty()) url = SongUrl.getSongUrlN(song.id?:"")
                    withContext(Dispatchers.Main) {
                        success.invoke(url)
                    }
                }
            }
            SOURCE_QQ -> {
                PlayUrl.getPlayUrl(song.id?:"") {
                    success.invoke(it)
                }
            }
            SOURCE_LOCAL -> {
                val id = song.id?.toLong() ?: 0
                val contentUri: Uri =
                    ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                success.invoke(contentUri)
            }
            SOURCE_DIRROR -> {
                song.dirrorInfo?.let {
                    success.invoke(it.url)
                }
            }
            SOURCE_KUWO -> {
                GlobalScope.launch {
                    val url = SearchSong.getUrl(song.id?:"")
                    withContext(Dispatchers.Main) {
                        success.invoke(url)
                    }
                }
            }
            SOURCE_NETEASE_CLOUD -> {
                SongUrl.getSongUrlCookie(song.id?:"") {
                    success.invoke(it)
                }
            }
            else -> success.invoke(null)
        }
    }

    fun getLyric(song: StandardSongData, success: (LyricViewData) -> Unit) {
        if (song.source == SOURCE_NETEASE) {
            MyApp.cloudMusicManager.getLyric(song.id?.toLong() ?: 0) { lyric ->
                runOnMainThread {
                    val l = LyricViewData(lyric.lrc?.lyric?:"", lyric.tlyric?.lyric?:"")
                    success.invoke(l)
                }
            }
        } else {
            SearchLyric.getLyricString(song) { string ->
                runOnMainThread {
                    success.invoke(LyricViewData(string, ""))
                }
            }
        }
    }

}